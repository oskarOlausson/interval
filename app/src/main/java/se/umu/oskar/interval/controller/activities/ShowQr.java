package se.umu.oskar.interval.controller.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Hashtable;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.files.WorkoutCatalog;
import se.umu.oskar.interval.model.Workout;

public class ShowQr extends AppCompatActivity {
    private final BarcodeFormat format = BarcodeFormat.QR_CODE;
    private Workout workout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr);

        setUpToolBar();

        if (savedInstanceState != null) {
            workout = savedInstanceState.getParcelable(Workout.class.toString());
        } else {
            Intent intent = getIntent();
            if (intent != null) {
                workout = intent.getParcelableExtra(Workout.class.toString());
            }
        }

        if (workout == null) {
            Toast.makeText(this, "This is a mistake, returning to last activity", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int warnSize = 40;
        if (workout.rawBlocks().size() > warnSize) {
            String warn = getString(R.string.warn_many_blocks_qr, warnSize);
            Snackbar.make(findViewById(android.R.id.content), warn, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.dismiss, l -> {})
                    .show();
        }

        TextView textView = findViewById(R.id.text_workout_name);
        textView.setText(workout.name());

        createAndDisplayQr(workout);
    }

    private void createAndDisplayQr(Workout workout) {
        ImageView imageView = findViewById(R.id.qrImageView);

        imageView.post(() -> {
            int w = imageView.getWidth();
            int h = imageView.getHeight();

            int size = Math.min(w, h);

            new Thread(() -> {
                try {
                    String content = Workout.topLineInQr + WorkoutCatalog.formatWorkout(workout);
                    Bitmap bitmap = textToImageEncode(content, size);
                    displayBitmap(imageView, bitmap);
                } catch (WriterException e) {
                    Toast.makeText(this, R.string.error_could_not_generate_qr, Toast.LENGTH_LONG).show();
                    finish();
                }
            }).start();
        });
    }

    private void displayBitmap(ImageView imageView, Bitmap bitmap) {
        runOnUiThread(() -> {
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
        });
    }

    private void setUpToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundResource(R.color.white);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private Bitmap textToImageEncode(String text, int qrCodeSize) throws WriterException {

        BitMatrix bitMatrix;

        Hashtable<EncodeHintType, String> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        bitMatrix = new MultiFormatWriter().encode(
                text,
                format,
                qrCodeSize, qrCodeSize, hints
        );

        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        int black = getColor(R.color.black);
        int white = getColor(R.color.white);

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y)
                        ? black
                        : white;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, qrCodeSize, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Workout.class.toString(), workout);
    }
}
