package se.umu.oskar.interval.controller.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import se.umu.oskar.interval.R;
import se.umu.oskar.interval.model.Block;
import se.umu.oskar.interval.view.ViewUtilities;

public class SubAdapter extends RecyclerView.Adapter<SubAdapter.MyViewHolder> {

    private List<Block> blocks = new LinkedList<>();

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_details_block, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        View view = holder.itemView;

        Block block = blocks.get(position);

        TextView textView = view.findViewById(R.id.text_name);
        textView.setText(block.name());

        TextView timeText = view.findViewById(R.id.text_time);
        timeText.setText(ViewUtilities.timeToString(view.getResources(), block.timeInSeconds()));
    }

    @Override
    public int getItemCount() {
        return blocks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
