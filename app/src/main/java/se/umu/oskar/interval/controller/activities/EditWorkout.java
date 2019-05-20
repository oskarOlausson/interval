package se.umu.oskar.interval.controller.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.controller.ItemTouchCallback;
import se.umu.oskar.interval.helpers.Tags;
import se.umu.oskar.interval.view.TimeLine;
import se.umu.oskar.interval.controller.adapters.BlockAdapter;
import se.umu.oskar.interval.files.WorkoutCatalog;
import se.umu.oskar.interval.model.Block;
import se.umu.oskar.interval.files.BlockTimes;
import se.umu.oskar.interval.model.Pause;
import se.umu.oskar.interval.model.Workout;
import se.umu.oskar.interval.view.DialogMaker;
import se.umu.oskar.interval.view.ViewUtilities;

public class EditWorkout extends AppCompatActivity {

    private Workout workout;
    private View timeLineView = null;
    private TextView totalTimeView = null;
    private TextView pauseView = null;
    private TextView nrOfRepetitionsView = null;

    private WorkoutCatalog workoutCatalog;

    private BlockAdapter blockAdapter;
    private final int RequestExerciseFlag = 1;

    private BlockTimes blockTimes;

    private final int[] colors =
            {R.color.item_1
                    , R.color.item_2
                    , R.color.item_3
                    , R.color.item_4
                    , R.color.item_5
            };

    private int randomColor() {
        int i = (int) (Math.random() * colors.length);
        return getColor(colors[i]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_workout);

        int defaultTime = 50;
        int defaultPauseTime = 10;
        blockTimes = new BlockTimes(this, defaultTime, defaultPauseTime);

        workout = restoreWorkoutOrInitDefault(savedInstanceState);
        workoutCatalog = initWorkoutCatalog();
        save();

        setUpToolBar();
        bindView();
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Workout.class.toString(), workout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestExerciseFlag && resultCode == Activity.RESULT_OK) {
            String name = data.getStringExtra(String.class.toString());
            if (name != null) {
                int time = blockTimes.getOrDefault(name);
                workout.add(new Block(name, time, randomColor()));
                blockAdapter.notifyItemInserted(workout.size() - 1);
                save();
                drawTimeLine();
            } else {
                Log.w(Tags.oskarTag, "We did not get an exercise back even though it said it was successful");
            }
        }
    }

    private void bindView() {
        timeLineView = findViewById(R.id.time_line);
        totalTimeView = findViewById(R.id.time_line_text);
        pauseView = findViewById(R.id.time_line_pause);
        nrOfRepetitionsView = findViewById(R.id.time_line_repetitions);

        // this draws as early as possible
        timeLineView.post(this::drawTimeLine);

        initializeRecycleViewer();

        setWorkoutSettingsListeners();
        setWorkoutSettingsFields();

        bindButtons();
    }

    private void setWorkoutSettingsFields() {
        int pauseTime = workout.pauseBetween();
        int repetitions = workout.repetitions();

        String pauseTimeAsString = ViewUtilities.timeToString(getResources(), pauseTime);

        pauseView.setText(String.format(getString(R.string.pause_between), pauseTimeAsString));

        nrOfRepetitionsView.setText(String.format(getString(R.string.times), repetitions));
    }

    private void setWorkoutSettingsListeners() {

        String pauseBetweenActivities = getString(R.string.pause_between_exercises);

        pauseView.setOnClickListener(l ->
                DialogMaker.timePicker(this, pauseBetweenActivities,
                        workout.pauseBetween(), sec -> {
                            workout.setPauseBetween(sec);
                            drawTimeLine();
                            setWorkoutSettingsFields();
                            save();
                        })
        );

        String repetitionsText = getString(R.string.repetitions);

        nrOfRepetitionsView.setOnClickListener(l ->
                DialogMaker.numberPicker(this, repetitionsText, workout.repetitions()
                        , repetitions -> {
                            workout.setNrOfRepetitions(repetitions);
                            drawTimeLine();
                            setWorkoutSettingsFields();
                            save();
                        })
        );
    }

    private void bindButtons() {
        Button pauseButton = findViewById(R.id.button_add_pause);
        pauseButton.setOnClickListener(l -> {
            workout.add(new Pause(blockTimes.pauseTime(), this));
            blockAdapter.notifyDataSetChanged();
            save();
            drawTimeLine();
        });

        Button addButton = findViewById(R.id.button_add_empty);
        addButton.setOnClickListener(l -> {
            String name = getString(R.string.untitled);
            workout.add(new Block(name, blockTimes.getOrDefault(name), randomColor()));
            blockAdapter.notifyDataSetChanged();
            save();
            drawTimeLine();
        });

        Button addSpecificButton = findViewById(R.id.button_add_specific);
        addSpecificButton.setOnClickListener(l -> {
            Intent myIntent = new Intent(this, ChooseExercise.class);
            startActivityForResult(myIntent, RequestExerciseFlag);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        ImageButton playButton = findViewById(R.id.button_play);
        playButton.setOnClickListener(l -> {
            Intent intent = new Intent(this, PlayWorkout.class);
            intent.putExtra(Workout.class.toString(), workout);
            startActivity(intent);
            save();
        });
    }

    private void save() {
        try {
            workoutCatalog.writeContentsToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeRecycleViewer() {
        blockAdapter = new BlockAdapter(workout, getResources(), () -> {
            drawTimeLine();
            save();
        });

        blockAdapter.onTimeListener(b ->
                DialogMaker.timePicker(this, b.name(), b.timeInSeconds(),
                        seconds -> {
                            b.setTime(seconds);
                            blockAdapter.notifyItemChanged(b);
                            if (b.isPause()) {
                                blockTimes.setPauseTime(seconds);
                            } else {
                                blockTimes.setTime(b.name(), seconds);
                                blockTimes.setDefaultTime(seconds);
                            }
                            save();
                            drawTimeLine();
                        })
        );

        RecyclerView recyclerView = findViewById(R.id.intervals);
        recyclerView.setAdapter(blockAdapter);
        recyclerView.setLayoutManager(ViewUtilities.verticalLayoutManager(this));

        ItemTouchHelper.Callback it = new ItemTouchCallback(blockAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(it);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    private WorkoutCatalog initWorkoutCatalog() {
        try {
            return new WorkoutCatalog(this, workout);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.err_could_not_load_workouts, Toast.LENGTH_LONG)
                    .show();
            Log.e(Tags.oskarTag, "Could not load workout catalog");
            throw new RuntimeException("Could not load workout catalog");
        }
    }

    private Workout restoreWorkoutOrInitDefault(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String name = intent.getStringExtra(String.class.toString());

            if (name != null) {
                return new Workout(name);
            }

            Workout workout = intent.getParcelableExtra(Workout.class.toString());

            if (workout != null) {
                return workout;
            }

            Log.e(Tags.oskarTag, "Warning: no intent information");

            return null;
        } else {
            return savedInstanceState.getParcelable(Workout.class.toString());
        }
    }

    private void drawTimeLine() {
        int time = workout.totalTimeInSeconds();

        if (time > 0) {
            String totalTimeAsString = ViewUtilities.timeToString(getResources(), time);
            totalTimeView.setText(String.format(getString(R.string.total_time), totalTimeAsString));
        } else {
            totalTimeView.setText("");
        }

        new TimeLine(timeLineView, workout.blocksWithPausesAndRepetitions(this)).drawTimeLineWithCursor();
    }
}
