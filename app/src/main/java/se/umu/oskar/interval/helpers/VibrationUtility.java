package se.umu.oskar.interval.helpers;

import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public abstract class VibrationUtility {
    private VibrationUtility() {}


    public static void vibrate(Vibrator vibrator, long milli) {
        if (vibrator == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int amp = VibrationEffect.DEFAULT_AMPLITUDE;
            VibrationEffect effect = VibrationEffect.createOneShot(milli, amp);
            vibrator.vibrate(effect);
        } else {
            vibrationApiLessThan26(vibrator, milli);
        }
    }

    @SuppressWarnings("deprecation")
    private static void vibrationApiLessThan26(Vibrator vibrator, long milli) {
        vibrator.vibrate(milli);
    }
}
