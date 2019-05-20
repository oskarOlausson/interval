package se.umu.oskar.interval.controller.adapters;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.controller.activities.EditWorkout;
import se.umu.oskar.interval.controller.activities.PlayWorkout;
import se.umu.oskar.interval.controller.activities.ShowQr;
import se.umu.oskar.interval.helpers.BiConsumer;
import se.umu.oskar.interval.model.Workout;
import se.umu.oskar.interval.view.ViewUtilities;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.MyViewHolder> {

    private final BiConsumer<Class, Workout> workoutListener;
    private final List<Workout> workouts;
    private boolean editMode;

    private static final String deleteKey = "key_delete";
    private static final String editModeKey = "key_edit_mode";
    private static final String expandKey = "key_expand";

    private ArrayList<Workout> expanded;
    private ArrayList<Workout> toDelete;

    public WorkoutAdapter(List<Workout> workouts
            , BiConsumer<Class, Workout> workoutListener) {

        this.workouts = workouts;

        this.workoutListener = workoutListener;

        this.editMode = false;
        this.toDelete = new ArrayList<>();
        this.expanded = new ArrayList<>();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(editModeKey, editMode);
        savedInstanceState.putParcelableArrayList(deleteKey, toDelete);
        savedInstanceState.putParcelableArrayList(expandKey, expanded);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        editMode = savedInstanceState.getBoolean(editModeKey);
        toDelete = savedInstanceState.getParcelableArrayList(deleteKey);
        expanded = savedInstanceState.getParcelableArrayList(expandKey);
        notifyDataSetChanged();
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;

        if (editMode) {
            expanded.clear();
            notifyDataSetChanged();
        }

        notifyDataSetChanged();
    }

    public void deleteSelected() {
        workouts.removeAll(toDelete);
        notifyDataSetChanged();
        toDelete.clear();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.item_workout, parent, false);

        RecyclerView blocksView = view.findViewById(R.id.blocks);
        blocksView.setLayoutManager(ViewUtilities.verticalLayoutManager(view.getContext()));

        SubAdapter subAdapter = new SubAdapter();
        blocksView.setAdapter(subAdapter);

        return new MyViewHolder(view, subAdapter);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.setWorkout(workout);

        View view = holder.itemView;
        TextView titleView = view.findViewById(R.id.workout);
        titleView.setText(workout.name());

        TextView totalTimeView = view.findViewById(R.id.totalTime);
        totalTimeView.setText(ViewUtilities.timeToString(view.getResources(), workout.totalTimeInSeconds()));

        CheckBox checkBox = view.findViewById(R.id.checkbox_delete);
        checkBox.setVisibility(editMode ? View.VISIBLE : View.GONE);
        checkBox.setChecked(editMode && toDelete.contains(workout));

        View details = view.findViewById(R.id.details_container);

        if (expanded.contains(workout)) {
            details.setVisibility(View.VISIBLE);
            int nrOfRepetitions = workout.repetitions();
            int defaultPause = workout.pauseBetween();

            Resources res = view.getResources();

            String timeString = ViewUtilities.timeToString(res, defaultPause);

            TextView pauseView = view.findViewById(R.id.pauseTime);
            pauseView.setText(String.format(res.getString(R.string.pause_time), timeString));

            TextView repView = view.findViewById(R.id.numberOfRepetitions);
            repView.setText(String.format(res.getString(R.string.repetitions_amount), nrOfRepetitions));

            holder.getSubAdapter().setBlocks(workout.rawBlocks());
        } else {
            details.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public boolean isInEditMode() {
        return editMode;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private final View visibleRow;
        private final SubAdapter subAdapter;
        private final View expandButton;
        private Workout workout = null;


        MyViewHolder(View view, SubAdapter subAdapter) {
            super(view);
            this.subAdapter = subAdapter;

            CheckBox checkBox = view.findViewById(R.id.checkbox_delete);
            expandButton = view.findViewById(R.id.button_down);
            visibleRow = view.findViewById(R.id.visible_always);
            visibleRow.setOnClickListener(l -> {
                if (editMode) {
                    toggleChecked(checkBox);
                } else {
                    toggleExpanded();
                }
            });

            checkBox.setOnClickListener(l -> toggleChecked(checkBox));

            expandButton.setOnClickListener(l -> toggleExpanded());

            ImageButton editButton = view.findViewById(R.id.button_edit);
            hookUpButton(editButton, EditWorkout.class);

            ImageButton qrButton = view.findViewById(R.id.button_qr);
            hookUpButton(qrButton, ShowQr.class);

            ImageButton playButton = view.findViewById(R.id.button_play);
            hookUpButton(playButton, PlayWorkout.class);
        }

        private void toggleChecked(CheckBox checkBox) {
            if (checkBox.getVisibility() == View.VISIBLE) {
                if (workout != null) {
                    if (toDelete.contains(workout)) {
                        checkBox.setChecked(false);
                        toDelete.remove(workout);
                    } else {
                        checkBox.setChecked(true);
                        toDelete.add(workout);
                    }
                }
            }
        }

        private void hookUpButton(View imageButton, Class classToStart) {
            imageButton.setOnClickListener(l -> {
                if (workout != null) {
                    workoutListener.accept(classToStart, workout);
                }
            });
        }

        private void toggleExpanded() {
            if (workout != null) {
                if (expanded.contains(workout)) {
                    collapse();
                } else {
                    expand();
                }
                notifyDataSetChanged();
            }
        }

        private void expand() {
            expanded.add(workout);
        }

        private void collapse() {
            expanded.remove(workout);
        }

        private void setWorkout(Workout workout) {
            this.workout = workout;
        }

        private SubAdapter getSubAdapter() {
            return subAdapter;
        }
    }
}