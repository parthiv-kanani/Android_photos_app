package com.example.android22.Tag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android22.R;
import com.example.android22.Base.*;


public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> implements LeftSwipeDelete.SwipeToDeleteCallbackListener{

    private Photo currPhoto;
    private Context context;

    public TagAdapter(Context context, Photo p) {
        this.context = context;
        this.currPhoto = p;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tag_item, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Tag tag = currPhoto.getTags().get(position);
        holder.bind(tag.toString());

    }

    @Override
    public int getItemCount() {
        return (currPhoto.getTags() != null)? currPhoto.getTags().size() : 0;
    }

    @Override
    public void deleteItem(int position) {
        Tag removeTag = currPhoto.getTags().remove(position);
        boolean tagStillInUse = AppData.getInstance().getAllAlbums().stream()
                                    .flatMap(album -> album.getPhotos().stream())
                                        .anyMatch(p -> p.hasTag(removeTag));
        if(!tagStillInUse) AppData.getInstance().getAllTags().remove(removeTag);
        notifyItemRemoved(position);
    }

    public void updatePhoto(Photo p){
        currPhoto = p;
        notifyDataSetChanged();
    }

    public class TagViewHolder extends RecyclerView.ViewHolder {
        private TextView tagTextView;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tagTextView = itemView.findViewById(R.id.tagTextView);
        }

        public void bind(String tag) {
            tagTextView.setText(tag);
        }
    }
}
