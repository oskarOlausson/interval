package se.umu.oskar.interval.controller.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.helpers.Tags;
import se.umu.oskar.interval.helpers.Consumer;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.MyViewHolder> {

    private final ArrayList<String> delete = new ArrayList<>();
    private final List<String> exercises;
    private final Consumer<String> onSelect;
    private boolean editMode = false;

    private static final String deletedKey = ExerciseAdapter.class.toString() + "deleted";

    public ExerciseAdapter(List<String> exercises, Consumer<String> onSelect) {
        this.exercises = exercises;
        this.onSelect = onSelect;
    }

    public void onSaveInstanceState(Bundle savedInstance) {
        ArrayList<Integer> deleteIndices = new ArrayList<>();

        for (int i = 0; i < exercises.size(); i++) {
            if (delete.contains(exercises.get(i))) {
                deleteIndices.add(i);
            }
        }

        savedInstance.putIntegerArrayList(deletedKey, deleteIndices);
        savedInstance.putBoolean(Boolean.class.toString(), editMode);
    }

    public void restoreInstanceState(Bundle savedInstance) {
        editMode = savedInstance.getBoolean(Boolean.class.toString());

        ArrayList<Integer> indices = savedInstance.getIntegerArrayList(deletedKey);

        if (indices != null) {
            for (Integer index : indices) {
                delete.add(exercises.get(index));
            }
        } else {
            Log.w(Tags.oskarTag, "Could not load deleted-indices in");
        }

        notifyDataSetChanged();
    }

    public boolean isInEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExerciseAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.item_exercise, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        View view = holder.itemView;
        holder.exercise = exercises.get(position);
        TextView textView = view.findViewById(R.id.text_exercise);

        String exercise = exercises.get(position);

        textView.setText(exercise);

        CheckBox checkBox = view.findViewById(R.id.checkbox_delete);
        if (editMode) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setChecked(delete.contains(exercise));
        } else {
            checkBox.setVisibility(View.GONE);
            checkBox.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void deleteSelected() {
        for (int i = exercises.size() - 1; i >= 0; i--) {
            if (delete.contains(exercises.get(i))) {
                exercises.remove(i);
                notifyItemRemoved(i);
            }
        }

        delete.clear();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public String exercise = null;

        MyViewHolder(View view) {
            super(view);

            CheckBox checkBox = view.findViewById(R.id.checkbox_delete);
            View visibleRow = view.findViewById(R.id.visible_always);
            visibleRow.setOnClickListener(l -> {
                if (editMode) {
                    toggleChecked(checkBox);
                } else {
                    if (exercise != null) {
                        onSelect.accept(exercise);
                    }
                }
            });
            checkBox.setOnClickListener(l -> toggleChecked(checkBox));
        }

        private void toggleChecked(CheckBox checkBox) {
            if (exercise != null) {
                if (delete.contains(exercise)) {
                    checkBox.setChecked(false);
                    delete.remove(exercise);
                } else {
                    checkBox.setChecked(true);
                    delete.add(exercise);
                }
            }
        }
    }
}
