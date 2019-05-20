package se.umu.oskar.interval.controller;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import se.umu.oskar.interval.controller.adapters.BlockAdapter;
import se.umu.oskar.interval.model.Block;
import se.umu.oskar.interval.view.ViewUtilities;

/**
 * Sample code from example by Paul Burke
 * https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf
 */

@SuppressWarnings("SpellCheckingInspection")
public class ItemTouchCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter adapter;
    private boolean isElevated = false;

    public ItemTouchCallback(
            ItemTouchHelperAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return adapter.onItemMove(viewHolder.getAdapterPosition(),
                target.getAdapterPosition());
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder
            , float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        final BlockAdapter.MyViewHolder myViewHolder = (BlockAdapter.MyViewHolder) viewHolder;

        final Block block = myViewHolder.block();

        if (block != null) {
            View view = viewHolder.itemView;

            view.setAlpha(alphaFromDx(view.getWidth(), dX));

            int color = block.color();

            if (isCurrentlyActive && !isElevated) {
                final float newElevation = 5f + ViewCompat.getElevation(viewHolder.itemView);
                ViewCompat.setElevation(viewHolder.itemView, newElevation);
                isElevated = true;
            }

            if (isCurrentlyActive) {
                color = ViewUtilities.darken(color, .8f);
            }

            viewHolder.itemView.setBackgroundColor(color);
        }
    }

    private float alphaFromDx(int width, float dX) {
        return 1 - (float) Math.sqrt(Math.abs(dX) / width);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView,@NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        isElevated = false;
    }
}
