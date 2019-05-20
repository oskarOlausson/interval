package se.umu.oskar.interval.controller.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.helpers.VibrationUtility;
import se.umu.oskar.interval.model.WorkoutTimer;
import se.umu.oskar.interval.view.TimeLine;
import se.umu.oskar.interval.model.Block;
import se.umu.oskar.interval.model.Workout;
import se.umu.oskar.interval.view.ViewUtilities;

public class PlayWorkout extends AppCompatActivity {

    private WorkoutTimer workoutTimer;
    private Animation item2ToItem1;
    private Animation item3ToItem2;
    private Animation nothingToItem3;
    private View background;
    private Toolbar toolbar;
    private Vibrator vibrator;
    private TextView item1;
    private TextView item2;
    private TextView item3;
    private TextView countdown;
    private View timeLineView;
    private int pauseColor;
    private int normalColor;
    private ArrayList<Block> all;
    private TimeLine timeLine;
    private static String shouldVibrateKey = "key_should_vibrate";

    private SharedPreferences sharedPreferences;
    private int deviceWidth;

    private void loadAnimations() {
        item2ToItem1 = AnimationUtils.loadAnimation(this, R.anim.item2_to_item1);
        item3ToItem2 = AnimationUtils.loadAnimation(this, R.anim.item3_to_item2);
        nothingToItem3 = AnimationUtils.loadAnimation(this, R.anim.to_item3);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        setUpToolBar();
        loadAnimations();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sharedPreferences = getSharedPreferences(PlayWorkout.class.toString(), Context.MODE_PRIVATE);

        Point out = new Point();
        getWindowManager().getDefaultDisplay().getSize(out);
        deviceWidth = out.x;

        background = findViewById(R.id.back_drop);
        countdown = findViewById(R.id.countdown);
        item1 = findViewById(R.id.text_exercise_1);
        item2 = findViewById(R.id.text_exercise_2);
        item3 = findViewById(R.id.text_exercise_3);
        timeLineView = findViewById(R.id.time_line);

        pauseColor = getColor(R.color.pause);
        normalColor = background.getSolidColor();

        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        all = getBlocks(savedInstanceState, getIntent());

        if (all == null) {
            Toast.makeText(this, "No workout to play", Toast.LENGTH_LONG).show();
            finish();
        } else {
            timeLine = new TimeLine(timeLineView, all);
            timeLine.drawTimeLineWithCursor(0);

            background.setBackground(getDrawable(R.drawable.rectangle));
            background.setBackgroundColor(normalColor);

            initiateWorkoutTimer(savedInstanceState);
        }
    }

    private void initiateWorkoutTimer(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            workoutTimer = WorkoutTimer.restoreInstanceState(savedInstanceState, all);
        } else {
            workoutTimer = WorkoutTimer.withCountDown(all);

            item2.setVisibility(View.INVISIBLE);
            item3.setVisibility(View.INVISIBLE);
            timeLineView.setVisibility(View.INVISIBLE);
        }

        workoutTimer.setTimeListener(this::onNewSecond);

        boolean skipFirstVibration = (savedInstanceState != null);

        workoutTimer.setBlockListener(block -> onNewBlock(skipFirstVibration, block));
        workoutTimer.setOnCountDownFinishedListener(this::onCountDownFinished);

        workoutTimer.start();

        findViewById(R.id.button_skip).setOnClickListener(l -> workoutTimer.skipToNext());
    }

    private void onCountDownFinished() {
        runOnUiThread(() -> {
            item1.setVisibility(View.VISIBLE);
            item2.setVisibility(View.VISIBLE);
            item3.setVisibility(View.VISIBLE);
            timeLineView.setVisibility(View.VISIBLE);
        });
    }

    private void onNewSecond(Integer secondsLeft) {
        runOnUiThread(() -> {
            String timeAsString = ViewUtilities.timeToString(getResources(), secondsLeft);
            countdown.setText(timeAsString);

            final float fontSize;
            if (secondsLeft > 59) {
                fontSize = getResources().getDimensionPixelSize(R.dimen.play_font_small);
            } else {
                fontSize = getResources().getDimensionPixelSize(R.dimen.play_font_big);
            }

            countdown.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            int time = endTimeOfBlock(all, workoutTimer.currentPosition()) - secondsLeft;
            timeLine.drawTimeLineWithCursor(time);
        });
    }

    private void onNewBlock(boolean skipFirstVibration, Block block) {
        AtomicBoolean skipFirstVibrationAtomic = new AtomicBoolean(skipFirstVibration);

        runOnUiThread(() -> {
            if (skipFirstVibrationAtomic.get()) {
                skipFirstVibrationAtomic.set(false);
            } else {
                vibrate();
            }

            if (block == null) {
                Intent newActivityIntent = new Intent(this, FinishedWorkout.class);
                startActivity(newActivityIntent);
                finish();
            } else {
                if (block.isPause()) {
                    background.setBackgroundColor(pauseColor);
                    toolbar.setBackgroundColor(ViewUtilities.darken(pauseColor, .8f));
                } else {
                    toolbar.setBackgroundResource(R.color.white);
                    background.setBackgroundColor(normalColor);
                }

                item1.setText(block.name());
                item1.startAnimation(item2ToItem1);

                Block nextBlock = workoutTimer.get(workoutTimer.currentPosition() + 1);

                if (nextBlock != null) {
                    item2.setText(nextBlock.name());
                    item2.startAnimation(item3ToItem2);
                } else {
                    item2.setVisibility(View.GONE);
                }

                Block twoAway = workoutTimer.get(workoutTimer.currentPosition() + 2);

                if (twoAway != null) {
                    item3.setText(twoAway.name());
                    item3.startAnimation(nothingToItem3);
                } else {
                    item3.setVisibility(View.GONE);
                }
            }
        });
    }

    private void vibrate() {
        if (shouldVibrate()) {
            VibrationUtility.vibrate(vibrator, 200);
        }
    }

    private ArrayList<Block> getBlocks(@Nullable Bundle savedInstanceState, @Nullable Intent intent) {
        ArrayList<Block> blocks = null;

        if (savedInstanceState != null) {
            blocks = savedInstanceState.getParcelableArrayList(ArrayList.class.toString());
        } else if (intent != null) {
            Workout workout = intent.getParcelableExtra(Workout.class.toString());

            if (workout != null) {
                blocks = workout.blocksWithPausesAndRepetitions(this);
            }
        }

        return blocks;
    }

    private static int endTimeOfBlock(List<Block> allBlocks, int currentPosition) {
        if (allBlocks.isEmpty()) return 0;
        int sum = 0;

        for (int i = 0; i <= currentPosition; i++) {
            sum += allBlocks.get(i).timeInSeconds();
        }

        return sum;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.vibration_toggle, menu);

        MenuItem menuItem = menu.findItem(R.id.switch_vibrate_menu);
        View view = menuItem.getActionView();

        SwitchCompat vibrateSwitch = view.findViewById(R.id.switch_vibrate);

        vibrateSwitch.setChecked(shouldVibrate());

        vibrateSwitch.setOnCheckedChangeListener((switchView, checked) -> {
            setShouldVibrate(checked);
        });

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        workoutTimer.saveInstanceState(outState);
        outState.putParcelableArrayList(ArrayList.class.toString(), all);
    }

    @Override
    protected void onPause() {
        super.onPause();
        workoutTimer.stopAndWaitForStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        workoutTimer.startAgain();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        workoutTimer.stopAndWaitForStop();
    }

    @Override
    public void finish() {
        workoutTimer.stopAndWaitForStop();
        super.finish();
    }

    private void setUpToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundResource(R.color.white);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(l -> finish());
    }

    public boolean shouldVibrate() {
        return sharedPreferences.getBoolean(shouldVibrateKey, true);
    }

    public void setShouldVibrate(boolean newValue) {
        sharedPreferences.edit().putBoolean(shouldVibrateKey, newValue).apply();
    }
}
