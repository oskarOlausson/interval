package se.umu.oskar.interval.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.lang.reflect.Method;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.helpers.Consumer;

public class DialogMaker {

    public static void timePicker(Context context, String title, int startTime, Consumer<Integer> valueListener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_time);

        TextView textView = dialog.findViewById(R.id.dialog_title);
        textView.setText(title);

        Button doneButton = dialog.findViewById(R.id.button_done);

        NumberPicker secondPicker = dialog.findViewById(R.id.seconds);
        {
            final int currentSeconds = startTime % 60;

            secondPicker.setFormatter(i -> i + " sec");
            secondPicker.setMinValue(0);
            secondPicker.setMaxValue(59);
            secondPicker.setValue(currentSeconds);
            fixNumberPickerRenderBug(secondPicker);
        }

        NumberPicker minutesPicker = dialog.findViewById(R.id.minutes);
        {
            final int currentMinutes = (startTime / 60); // integer division
            minutesPicker.setFormatter(i -> i + " min");
            minutesPicker.setMinValue(0);
            minutesPicker.setMaxValue(59);
            minutesPicker.setValue(currentMinutes);
            fixNumberPickerRenderBug(minutesPicker);
        }

        Runnable set = () ->
                valueListener.accept(secondPicker.getValue() + minutesPicker.getValue() * 60);

        doneButton.setOnClickListener(l -> {
            set.run();
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * This codes fixes a bug in the number-picker which would make it so that the formatting
     * on numbers did not work on the currently selected number
     * @param picker The number picker to fix
     */
    private static void fixNumberPickerRenderBug(NumberPicker picker) {
        try {
            @SuppressLint("PrivateApi")
            Method method = picker.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
            method.setAccessible(true);
            method.invoke(picker, true);
        } catch (Exception e) {
            Log.w(DialogMaker.class.toString(), "Could not fix current render on number picker, no worries it is a small bug anyways");
        }
    }

    public static void numberPicker(Context context, String title, int startValue, Consumer<Integer> valueListener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_number);

        TextView textView = dialog.findViewById(R.id.dialog_title);
        textView.setText(title);

        NumberPicker picker = dialog.findViewById(R.id.picker);
        picker.setMinValue(1);
        picker.setMaxValue(20);
        picker.setValue(startValue);
        fixNumberPickerRenderBug(picker);

        Button doneButton = dialog.findViewById(R.id.button_done);
        doneButton.setOnClickListener(l -> {
            valueListener.accept(picker.getValue());
            dialog.dismiss();
        });

        dialog.show();
    }

    public static void stringDialog(Context context
            , int titleStringId, int posButtonStringId
            , int hintId
            , Consumer<String> nameListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleStringId);
        final EditText input = new EditText(context);
        input.setSelectAllOnFocus(true);
        builder.setView(input);
        input.setHint(hintId);

        builder.setPositiveButton(posButtonStringId, (dialog, which) ->
                nameListener.accept(input.getText().toString())
        );

        builder.setCancelable(true);
        builder.show();

        showKeyboard(context, input);
    }

    private static void showKeyboard(Context context, EditText input) {
        input.setOnFocusChangeListener((l, hasFocus) -> {
            if (hasFocus) {
                input.post(() -> {
                    InputMethodManager imm = (InputMethodManager) context
                            .getSystemService(Context.INPUT_METHOD_SERVICE);

                    if (imm != null) {
                        imm.showSoftInput(input, 0);
                    }
                });
            }
        });
    }
}
