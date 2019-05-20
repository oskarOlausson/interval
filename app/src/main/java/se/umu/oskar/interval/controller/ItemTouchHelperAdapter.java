package se.umu.oskar.interval.controller;

public interface ItemTouchHelperAdapter {
    void onItemDismiss(int position);
    boolean onItemMove(int fromPosition, int toPosition);
}
