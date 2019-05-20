package se.umu.oskar.interval.controller.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.helpers.VibrationUtility;
import se.umu.oskar.interval.model.Workout;

public class ScanQr extends AppCompatActivity {

    private static final int REQUEST_CAMERA_CODE = 1;

    private SurfaceView cameraPreview;
    private TextView textView;

    private CameraSource cameraSource;
    private View warning;

    private String lastInvalidQr = null;
    private BarcodeDetector barcodeDetector;

    private Vibrator vibrator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        setUpToolBar();

        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        warning = findViewById(R.id.warning);

        ImageView closeButton = findViewById(R.id.button_close);
        closeButton.setOnClickListener(l -> closeErrorMessage());

        textView = findViewById(R.id.text_warning);
        cameraPreview = findViewById(R.id.preview);

        cameraPreview.post(() -> {
            setUpBarcodeDetection();
            startDetection();
        });
    }

    private void closeErrorMessage() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.error_move_down);
        lastInvalidQr = null;

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (lastInvalidQr == null) warning.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        warning.startAnimation(animation);
    }


    private void setUpBarcodeDetection() {
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(24)
                .setAutoFocusEnabled(true)
                .build();

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                parseDetection(detections);
            }
        });
    }

    private void startDetection() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ScanQr.this
                    , new String[]{Manifest.permission.CAMERA}, 1);

            return;
        }

        try {
            cameraSource.start(cameraPreview.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
            errorToast(R.string.err_camera_setup);
        }
    }

    private synchronized void parseDetection(Detector.Detections<Barcode> detections) {
        SparseArray<Barcode> qrCodes = detections.getDetectedItems();

        if (qrCodes.size() != 0) {
            Barcode qr = qrCodes.valueAt(0);

            if (!alreadyChecked(qr)) {
                vibrate();

                if (isCodeAnIntervalCode(qr)) {
                    textView.post(() -> {
                        Workout workout = Workout.parseFromQR(qr);

                        if (workout != null) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(Workout.class.toString(), workout);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } else {
                            postError("That Interval code is improperly formatted");
                            lastInvalidQr = qr.rawValue;
                        }
                    });
                } else {
                    postError("That code (" + qr.displayValue + ") is not a Interval-code");
                    lastInvalidQr = qr.rawValue;
                }
            }
        }
    }

    private void vibrate() {
        VibrationUtility.vibrate(vibrator, 200);
    }

    private boolean alreadyChecked(Barcode qr) {
        return lastInvalidQr != null && lastInvalidQr.equals(qr.rawValue);
    }

    private void postError(String s) {
        textView.post(() -> {
            warning.setVisibility(View.VISIBLE);
            textView.setText(s);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.stop();
        barcodeDetector.release();
    }

    private boolean isCodeAnIntervalCode(Barcode qr) {
        return qr.rawValue.startsWith(Workout.topLineInQr);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            errorToast(R.string.err_camera_not_allowed);
                            finish();
                            return;
                        }
                        cameraSource.start(cameraPreview.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                } else {
                    errorToast(R.string.err_camera_not_allowed);
                    finish();
                }
                break;
        }
    }

    private void errorToast(int stringId) {
        Toast.makeText(this, stringId, Toast.LENGTH_LONG).show();
    }
}

