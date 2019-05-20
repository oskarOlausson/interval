package se.umu.oskar.interval.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.model.Block;
import se.umu.oskar.interval.model.Workout;


public class TimeLine {
    private final static int lineWidth = 2;

    private final View view;
    private final Resources resources;
    private final List<Block> blocks;
    private final int cursorColor;
    private final int totalTime;

    private double secondWidth = 0;
    private int w = 0;
    private int h = 0;

    private final int topPos = 0;

    public TimeLine(View view, List<Block> blocks) {
        this.view = view;
        this.blocks = blocks;
        this.resources = view.getResources();

        totalTime = totalTimeOf(blocks);

        cursorColor = ViewUtilities.resToColor(resources, R.color.black);
    }

    private int totalTimeOf(List<Block> blocks) {
        int sum = 0;

        for (Block block : blocks) {
            sum += block.timeInSeconds();
        }

        return sum;
    }

    public void drawTimeLineWithCursor() {
        view.post(() -> {
            initialize();
            drawTimeLineNow(-1);
        });
    }

    public void drawTimeLineWithCursor(final int secondsIn) {
        view.post(() -> {
            initialize();
            drawTimeLineNow(secondsIn);
        });
    }

    private void initialize() {
        if (w == 0) {
            w = view.getWidth();
            h = view.getHeight();
            secondWidth = w / (double) totalTime;
        }
    }

    private void drawTimeLineNow(int secondsIn) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawBlocks(canvas);

        if (secondsIn >= 0) {
            drawCursor(secondsIn, canvas);
        }

        Drawable drawable = new BitmapDrawable(resources, bitmap);
        view.setBackground(drawable);
    }

    private void drawBlocks(Canvas canvas) {
        Paint paint = new Paint();
        Paint dividerPaint = new Paint(ViewUtilities.resToColor(resources, R.color.dividerColor));

        int leftX = 0;
        Queue<Block> blocksQue =
                new LinkedList<>(blocks);

        while (!blocksQue.isEmpty()) {
            Block block = blocksQue.poll();
            int rightX = getRightPositionOfBlock(leftX, block);

            if (!block.isPause()) {
                paint.setColor(block.color());
                canvas.drawRect(new Rect(leftX, topPos, rightX, h), paint);

                if (rightX - lineWidth > leftX) {
                    Rect rect = new Rect(rightX - lineWidth, topPos, rightX, h);
                    canvas.drawRect(rect, dividerPaint);
                }
            }

            leftX = rightX;
        }
    }

    private int getRightPositionOfBlock(int leftX, Block block) {
        return Math.min(w, leftX + (int) (block.timeInSeconds() * secondWidth));
    }

    private void drawCursor(int secondsIn, Canvas canvas) {
        int cursorPos = (int) (secondsIn * secondWidth);
        int cursorHalfWidth = 4;

        if (cursorPos < cursorHalfWidth) cursorPos = cursorHalfWidth;
        if (cursorPos > w - cursorHalfWidth) cursorPos = w - cursorHalfWidth;

        Paint paint = new Paint(cursorColor);

        int cursorLeftX = cursorPos - cursorHalfWidth;
        int cursorRightX = cursorPos + cursorHalfWidth;

        Rect rect = new Rect(cursorLeftX, topPos, cursorRightX, h);

        canvas.drawRect(rect, paint);
    }
}
