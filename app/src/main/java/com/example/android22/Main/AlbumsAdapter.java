package com.example.android22.Main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android22.Base.*;
import com.example.android22.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder> implements LeftSwipeDelete.SwipeToDeleteCallbackListener {
    private ArrayList<Album> list;
    private OnItemClickListener clickListener;
    private static final long DOUBLE_CLICK_TIME_THRESHOLD = 300; // in milliseconds
    private long lastClickTime = 0;

    public AlbumsAdapter(OnItemClickListener clickListener) {
        this.list = AppData.getInstance().getAllAlbums();
        this.clickListener = clickListener;
    }


    public void updateData() {
        this.list = AppData.getInstance().getAllAlbums();
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.albums_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album currentAlbum = list.get(position);
        holder.textAlbumItemName.setText(currentAlbum.getName());
        holder.textAlbumItemName.setEnabled(false);


        holder.itemView.setOnClickListener(v -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_THRESHOLD) {
                if (clickListener != null) {
                    clickListener.onItemDoubleClick(position);
                    holder.textAlbumItemName.setEnabled(false);
                }
            } else {
                if (clickListener != null) {
                    lastClickTime = clickTime;
                    clickListener.onItemClick(position);
                }
            }
            lastClickTime = clickTime;
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemLongClick(position);
                holder.textAlbumItemName.setEnabled(true);
                holder.textAlbumItemName.requestFocus();
                return true;
            }
            return false;
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    public void deleteItem(int position){
        list.remove(position);
        notifyItemRemoved(position);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemDoubleClick(int position);
        void onItemLongClick(int position);
    }


    public class AlbumViewHolder extends RecyclerView.ViewHolder {
        private static final long DOUBLE_CLICK_TIME_DELTA = 500; // milliseconds
        private long lastClickTime = 0;

        TextInputEditText textAlbumItemName;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            textAlbumItemName = itemView.findViewById(R.id.textAlbumItemName);
            textAlbumItemName.setEnabled(false);
        }

        public void bind(Album album) {
            textAlbumItemName.setText(album.getName());
        }
    }
}
