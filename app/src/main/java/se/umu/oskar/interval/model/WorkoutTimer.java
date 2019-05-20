package se.umu.oskar.interval.model;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import se.umu.oskar.interval.helpers.Tags;
import se.umu.oskar.interval.helpers.Consumer;

public class WorkoutTimer {

    private ArrayList<Block> blocks;
    private Consumer<Integer> everySecond = null;
    private Consumer<Block> everyBlock = null;

    private Runnable countDownFinished = null;
    private boolean skip = false;
    private long nextTimerStop;
    private Thread timerThread = null;
    private AtomicBoolean end = new AtomicBoolean(false);
    private int position;

    private static final String nextTimerStopKey = "key_next_timer_stop";
    private static final String positionKey = "key_position";

    public static WorkoutTimer withCountDown(Collection<Block> blocks) {
        return new WorkoutTimer(blocks, -1, -1);
    }

    private WorkoutTimer(Collection<Block> blocks, long nextTarget, int position) {
        this.blocks = new ArrayList<>(blocks);
        this.nextTimerStop = nextTarget;
        this.position = position;
    }

    public void saveInstanceState(Bundle outState) {
        outState.putInt(positionKey, position);
        outState.putLong(nextTimerStopKey, nextTimerStop);
    }

    public static WorkoutTimer restoreInstanceState(Bundle bundle, Collection<Block> blocks) {
        int position = bundle.getInt(positionKey);
        long nextTarget = bundle.getLong(nextTimerStopKey);
        return new WorkoutTimer(blocks, nextTarget, position);
    }

    public void setTimeListener(Consumer<Integer> everySecond) {
        this.everySecond = everySecond;
    }

    public void setBlockListener(Consumer<Block> newBlockListener) {
        this.everyBlock = newBlockListener;
    }

    private long toMilli(int seconds) {
        return seconds * 1000L;
    }

    public void start() {
        if (!shouldEnd()) {
            timerThread = new Thread(() -> {
                long overshoot = 0;

                if (!shouldEnd()) {
                    if (position == -1) {
                        if (nextTimerStop == -1) {
                            nextTimerStop = System.currentTimeMillis() + toMilli(3);
                        }

                        overshoot = runUntil(nextTimerStop);
                        sendFinishedCountDownSignal();
                    } else {
                        onNewBlock();
                        overshoot = runUntil(nextTimerStop);
                    }
                }

                while (position < blocks.size() - 1 && !shouldEnd()) {
                    position++;
                    nextTimerStop = System.currentTimeMillis() + toMilli(get(position).timeInSeconds());

                    if (nextTimerStop - overshoot > System.currentTimeMillis()) {
                        onNewBlock();
                        overshoot = runUntil(nextTimerStop - overshoot);
                    }
                }

                sendFinishSignal();
            });
        }

        timerThread.start();
    }

    private void sendFinishedCountDownSignal() {
        if (countDownFinished != null) {
            countDownFinished.run();
        }
    }

    private synchronized void onNewBlock() {
        if (everyBlock != null && !shouldEnd()) everyBlock.accept(get(position));
    }

    private synchronized void sendFinishSignal() {
        if (everyBlock != null && !shouldEnd()) {
            everyBlock.accept(null);
        }
    }

    private int toSeconds(long milli) {
        return 1 + (int) (milli/1000);
    }

    /**
     * @param stopTimeMilliseconds The time when it stopped
     * @return the overshoot, the time passed from stop to time of return
     */
    private long runUntil(long stopTimeMilliseconds) {
        long now;

        do {
            if (shouldSkip()) {
                setSkip(false);
                return 0;
            }

            now = System.currentTimeMillis();

            if (everySecond != null) {
                int secondsLeft = toSeconds(stopTimeMilliseconds - now);
                everySecond.accept(secondsLeft);
            }

            if (shouldEnd()) {
                break;
            }

            giveTheCpuABreak();
        } while (now < stopTimeMilliseconds);

        return System.currentTimeMillis() - stopTimeMilliseconds;
    }

    public int currentPosition() {
        return position;
    }

    private void giveTheCpuABreak() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }
    }

    public void setOnCountDownFinishedListener(Runnable countDownFinished) {
        this.countDownFinished = countDownFinished;
    }

    public void skipToNext() {
        setSkip(true);
    }

    private synchronized boolean shouldSkip() {
        return skip;
    }

    public synchronized void setSkip(boolean skip) {
        this.skip = skip;
    }

    public void stopAndWaitForStop() {
        if (!shouldEnd()) {
            end();

            if (timerThread != null) {
                Log.d(Tags.oskarTag, "Sending stop signal");
                timerThread.interrupt();

                try {
                    timerThread.join();
                } catch (InterruptedException ignored) {
                }

                timerThread = null;
            }
        }
    }

    private void end() {
        end.set(true);
    }

    private synchronized boolean shouldEnd() {
        return end.get();
    }

    public Block get(int position) {
        if (position == -1) return null;
        if (position >= blocks.size()) return null;
        return blocks.get(position);
    }

    public void startAgain() {
        if (timerThread == null) {
            skip = false;
            end.set(false);
            start();
        }
    }
}
