package com.streamliners.dialog.Adapter;

public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDelete(int position);
}
