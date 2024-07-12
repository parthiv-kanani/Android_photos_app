

package com.example.android22.Base;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android22.R;

public class LeftSwipeDelete<T extends RecyclerView.Adapter> extends ItemTouchHelper.SimpleCallback{
    private T tAdapter;
    private SwipeToDeleteCallbackListener tListener;
    public interface SwipeToDeleteCallbackListener {
        void deleteItem(int position);
    }

    public LeftSwipeDelete(T tAdapter, SwipeToDeleteCallbackListener tListener) {
        super(0, ItemTouchHelper.LEFT);
        this.tAdapter = (T)tAdapter;
        this.tListener = tListener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        tListener.deleteItem(position);
    }

    @Override
    public void onChildDraw(
            @NonNull Canvas c,
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            float dX,
            float dY,
            int actionState,
            boolean isCurrentlyActive) {

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        Drawable background = ContextCompat.getDrawable(recyclerView.getContext(), R.color.swipe_delete_background);

        int left = viewHolder.itemView.getRight() + (int) dX;
        int right = viewHolder.itemView.getRight();

        background.setBounds(left, viewHolder.itemView.getTop(), right, viewHolder.itemView.getBottom());

        background.draw(c);
    }
}

