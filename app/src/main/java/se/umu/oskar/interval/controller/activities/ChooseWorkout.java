package se.umu.oskar.interval.controller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.helpers.Tags;
import se.umu.oskar.interval.controller.adapters.WorkoutAdapter;
import se.umu.oskar.interval.files.WorkoutCatalog;
import se.umu.oskar.interval.helpers.BiConsumer;
import se.umu.oskar.interval.model.Workout;
import se.umu.oskar.interval.view.DialogMaker;
import se.umu.oskar.interval.view.ViewUtilities;

public class ChooseWorkout extends AppCompatActivity {

    private static final int REQUEST_QR = 1;
    private WorkoutCatalog workoutCatalog;
    private WorkoutAdapter workoutAdapter;
    private View deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundResource(R.color.white);

        workoutCatalog = initiateWorkoutCatalog();

        BiConsumer<Class, Workout> workoutListener = (c, w) -> {
            Intent intent = new Intent(this, c);
            intent.putExtra(Workout.class.toString(), w);
            startActivity(intent);
        };

        workoutAdapter = new WorkoutAdapter(workoutCatalog.workouts, workoutListener);

        RecyclerView recyclerView = findViewById(R.id.list_workout);
        recyclerView.setLayoutManager(ViewUtilities.verticalLayoutManager(this));
        recyclerView.setAdapter(workoutAdapter);

        deleteButton = findViewById(R.id.button_delete);
        updateEditMode();

        deleteButton.setOnClickListener(l -> {
            workoutAdapter.deleteSelected();
            updateEditMode(false);
            try {
                workoutCatalog.writeContentsToFile();
            } catch (IOException e) {
                Log.w(Tags.oskarTag, e.getMessage());
                displayLongToast(R.string.could_not_sync);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            workoutCatalog.reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
        workoutAdapter.notifyDataSetChanged();
    }

    private WorkoutCatalog initiateWorkoutCatalog() {
        try {
            return new WorkoutCatalog(this);
        } catch (IOException e) {
            displayLongToast(R.string.err_could_not_load_workouts);
            Log.e(Tags.oskarTag, "Could not load workouts at all, closing down");
            throw new RuntimeException("Could not load workout catalog");
        }
    }

    public void displayLongToast(int stringId) {
        Toast.makeText(this, stringId, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        if (workoutAdapter.isInEditMode()) {
            menuInflater.inflate(R.menu.cancel, menu);
        } else {
            menuInflater.inflate(R.menu.main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                addNewWorkout();
                return true;
            case R.id.delete:
                updateEditMode(true);
                return true;
            case R.id.qr:
                Intent intent = new Intent(this, ScanQr.class);
                startActivityForResult(intent, REQUEST_QR);
                return true;
            case R.id.cancel:
                updateEditMode(false);
                return true;
            default:
                return false;
        }
    }

    private void addNewWorkout() {
        DialogMaker.stringDialog(this, R.string.create_new_workout, R.string.create, R.string.hint_workout, this::addNewWorkout);
    }

    private void addNewWorkout(String name) {
        name = cleanUpName(name);
        if (workoutCatalog.hasName(name)) {
            displayLongToast(R.string.already_used);
        } else {
            Intent intent = new Intent(this, EditWorkout.class);
            intent.putExtra(String.class.toString(), name);
            startActivity(intent);
        }
    }

    private static String cleanUpName(String name) {
        return name.replace("\n", "").trim();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_QR) {
            if (resultCode == RESULT_OK) {
                Workout workout = data.getParcelableExtra(Workout.class.toString());

                if (workout != null) {
                    assureNoNameCollision(workout);

                    workoutCatalog.workouts.add(workout);
                    try {
                        workoutCatalog.writeContentsToFile();
                        if (!workoutCatalog.workouts.isEmpty()) {
                            int newIndex = workoutCatalog.workouts.size() - 1;
                            workoutAdapter.notifyItemInserted(newIndex);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.w(Tags.oskarTag, "Could not save to file");
                        displayLongToast(R.string.error_could_not_save);
                    }
                }
            }
        }
    }

    private void assureNoNameCollision(Workout workout) {
        workout.setName(cleanUpName(workout.name()));
        String name = workout.name();

        for (int i = 1; workoutCatalog.hasName(name); i++) {
            name = workout.name() + " (" + i + ")";
        }

        workout.setName(name);
    }

    @Override
    public void onBackPressed() {
        if (workoutAdapter.isInEditMode()) {
            updateEditMode(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        workoutAdapter.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        workoutAdapter.onRestoreInstanceState(savedInstanceState);
        updateEditMode();
    }

    private void updateEditMode(boolean editMode) {
        workoutAdapter.setEditMode(editMode);
        updateEditMode();
    }

    private void updateEditMode() {
        deleteButton.setVisibility(workoutAdapter.isInEditMode() ? View.VISIBLE : View.GONE);
        invalidateOptionsMenu();
    }
}
