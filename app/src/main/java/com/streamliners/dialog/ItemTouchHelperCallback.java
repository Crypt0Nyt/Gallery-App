package com.streamliners.dialog;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.streamliners.dialog.Adapter.ItemTouchHelperAdapter;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    public ItemTouchHelperAdapter itemAdapter;

    public ItemTouchHelperCallback(ItemTouchHelperAdapter itemAdapter) {
        this.itemAdapter = itemAdapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        itemAdapter.onItemMove(viewHolder.getAbsoluteAdapterPosition(),
                target.getAbsoluteAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        itemAdapter.onItemDelete(viewHolder.getAbsoluteAdapterPosition());
    }
}
