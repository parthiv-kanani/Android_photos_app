package com.example.android22.album;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android22.R;
import com.example.android22.Base.Album;
import com.example.android22.Base.AppData;
import com.example.android22.Base.Photo;
import com.example.android22.Tag.PhotoActivity;

import java.io.IOException;

public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_PHOTO = 0;
    private static final int VIEW_TYPE_ADD_PHOTO = 1;
    private static final int GALLERY_REQUEST_CODE = 1001;
    private static final long DOUBLE_CLICK_TIME_THRESHOLD = 300; // in milliseconds
    private long lastClickTime = 0;
    private Album currAlbum;
    private int currAlbumPosition;
    private Context context;
    private ImageView deleteIcon;
    private ImageView moveIcon;
    private PhotoViewHolder currentlyClicked;

    public AlbumAdapter(int currentAlbumPosition, Album resultAlbum,Context context, ImageView deleteIcon, ImageView moveIcon) {
        this.currAlbumPosition = currentAlbumPosition;
        if(currAlbumPosition > -1){
            this.currAlbum = AppData.getInstance().getAllAlbums().get(currentAlbumPosition);
        }else{
            this.currAlbum = resultAlbum;
        }
        this.context = context;
        this.deleteIcon = deleteIcon;
        this.moveIcon = moveIcon;
    }


    public Photo getCurrentlyClickedPhoto(){
        return currAlbum.getPhotos().get(currentlyClicked.getAdapterPosition());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_PHOTO) {
            View itemView = inflater.inflate(R.layout.album_item, parent, false);
            return new PhotoViewHolder(itemView);
        } else if (viewType == VIEW_TYPE_ADD_PHOTO) {
            View itemView = inflater.inflate(R.layout.add_photo_item, parent, false);
            return new AddPhotoViewHolder(itemView);
        }
        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        if (position < currAlbum.getNumPhotos()) {
            if (holder instanceof PhotoViewHolder) {
                PhotoViewHolder photoViewHolder = (PhotoViewHolder) holder;
                try {handlePhotoViewHolder(photoViewHolder, position, screenWidth);
                } catch (IOException e) {throw new RuntimeException(e);}
            }
        } else if (holder instanceof AddPhotoViewHolder) {
            AddPhotoViewHolder addPhotoViewHolder = (AddPhotoViewHolder) holder;
            handleAddPhotoViewHolder(addPhotoViewHolder, screenWidth);
        }
    }

    private void handlePhotoViewHolder(PhotoViewHolder photoViewHolder, int position, int screenWidth) throws IOException {
        ViewGroup.LayoutParams layoutParams = photoViewHolder.photoImageView.getLayoutParams();
        layoutParams.width = screenWidth / 3;
        layoutParams.height = layoutParams.width;
        photoViewHolder.photoImageView.setLayoutParams(layoutParams);
        Photo photo = currAlbum.getPhotos().get(position);
        photoViewHolder.photoImageView.setImageBitmap(AppData.bitmapFromUri(context, photo.getPhotoUri()));
        photoViewHolder.photoImageView.setOnClickListener(v -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_THRESHOLD) {
                context.startActivity(PhotoActivity.newIntent(context, position, currAlbumPosition, currAlbum));
            }
            else {
                deleteIcon.setVisibility(View.VISIBLE);
                moveIcon.setVisibility(View.VISIBLE);
                if(currentlyClicked != null && currentlyClicked != photoViewHolder) {
                    currentlyClicked.photoImageView.setForeground(null);
                }
                currentlyClicked = photoViewHolder;
                currentlyClicked.photoImageView.setForeground(ContextCompat.getDrawable(context, R.drawable.album_item_foreground));
                lastClickTime = System.currentTimeMillis();
            }
        });
    }

    private void handleAddPhotoViewHolder(AddPhotoViewHolder addPhotoViewHolder, int screenWidth) {
        ViewGroup.LayoutParams layoutParams = addPhotoViewHolder.addPhotoImageView.getLayoutParams();
        layoutParams.width = screenWidth / 3;
        layoutParams.height = layoutParams.width;
        addPhotoViewHolder.addPhotoImageView.setLayoutParams(layoutParams);

        addPhotoViewHolder.addPhotoImageView.setImageResource(R.drawable.add_photo);
        addPhotoViewHolder.addPhotoImageView.setVisibility(View.VISIBLE);
        addPhotoViewHolder.addPhotoImageView.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
            galleryIntent.setType("image/*");
            ((Activity) v.getContext()).startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri selectedImageUri = data.getData();
            context.getContentResolver().takePersistableUriPermission(selectedImageUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if(!(currAlbum.hasPhoto(selectedImageUri))){
                Photo newPhoto =
                    AppData.getInstance().getAllAlbums().stream()
                        .flatMap(album -> album.getPhotos().stream())
                        .filter(p -> p.getPhotoUri().equals(selectedImageUri))
                        .findAny()
                        .orElse(new Photo(selectedImageUri));
                currAlbum.addPhoto(newPhoto);
                notifyDataSetChanged();
            } else {
                Toast.makeText(context.getApplicationContext(), "Photo already added.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public int getItemCount() {
        return currAlbum.getNumPhotos() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < currAlbum.getNumPhotos()) ? VIEW_TYPE_PHOTO : VIEW_TYPE_ADD_PHOTO;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
        }
    }

    public static class AddPhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView addPhotoImageView;

        public AddPhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            addPhotoImageView = itemView.findViewById(R.id.addPhotoImageView);
        }
    }
}
