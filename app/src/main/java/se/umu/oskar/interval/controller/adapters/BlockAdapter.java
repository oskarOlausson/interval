package se.umu.oskar.interval.controller.adapters;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.controller.ItemTouchHelperAdapter;
import se.umu.oskar.interval.model.Block;
import se.umu.oskar.interval.helpers.Consumer;
import se.umu.oskar.interval.model.Workout;
import se.umu.oskar.interval.view.ViewUtilities;

public class BlockAdapter extends RecyclerView.Adapter implements ItemTouchHelperAdapter {

    private final Workout workout;
    private final Resources resources;
    private final Runnable changeNotifier;
    private Consumer<Block> onTime;

    public BlockAdapter(Workout workout, Resources resources, Runnable changeNotifier) {
        this.workout = workout;
        this.resources = resources;
        this.changeNotifier = changeNotifier;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.item_block, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Block block = workout.atPosition(position);

        View view = holder.itemView;
        TextView title = view.findViewById(R.id.interval_text);
        TextView timeText = view.findViewById(R.id.text_time);

        view.setBackgroundColor(block.color());

        final String timeString = ViewUtilities.timeToString(resources, block.timeInSeconds());

        timeText.setText(timeString);
        title.setText(block.name());
    }

    @Override
    public int getItemCount() {
        return workout.size();
    }

    public void onTimeListener(Consumer<Block> onTime) {
        this.onTime = onTime;
    }

    public void notifyItemChanged(Block b) {
        notifyItemChanged(workout.indexOf(b));
    }

    @Override
    public void onItemDismiss(int position) {
        workout.remove(position);
        notifyItemRemoved(position);
        changeNotifier.run();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        workout.move(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        changeNotifier.run();
        return true;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        MyViewHolder(View view) {
            super(view);
            ImageView timeButton = view.findViewById(R.id.button_time);
            timeButton.setOnClickListener(v -> onTime.accept(block()));
        }

        public Block block() {
            if (getLayoutPosition() == -1) return null;
            return workout.atPosition(getLayoutPosition());
        }
    }
}
