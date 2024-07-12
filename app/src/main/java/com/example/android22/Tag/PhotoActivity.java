package com.example.android22.Tag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android22.R;
import com.example.android22.album.AlbumActivity;
import com.example.android22.Base.*;
import com.example.android22.Base.SearchUtil;

import java.util.List;
import java.util.stream.Collectors;

public class PhotoActivity extends AppCompatActivity implements SwipeGestureListener.OnSwipeListener, ReturnUtilInterface {
    private static final String CURRENT_PHOTO = "currPhoto";
    private static final String CURRENT_ALBUM = "currAlbum";
    private static final String RESULT_ALBUM = "resultAlbum";
    private ImageView photoDisplayImageView;
    private TextView photoTitleTextView;
    private GestureDetector gestureDetector;
    private ImageButton addTagButton;
    private RecyclerView photoTagsRecyclerView;
    private TagAdapter tagAdapter;
    private Photo currPhoto;
    private int currPhotoPosition;
    private Album currAlbum;
    private int currAlbumPosition;

    public static Intent newIntent(Context context, int currPhotoPosition, int currAlbumPosition, Album resultAlbum) {
        Intent intent = new Intent(context, PhotoActivity.class);
        intent.putExtra(CURRENT_PHOTO, currPhotoPosition);
        intent.putExtra(CURRENT_ALBUM, currAlbumPosition);
        if(currAlbumPosition == -1){
            intent.putExtra(RESULT_ALBUM, resultAlbum);
        }
        AppData.saveToFile(context);
        return intent;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_view);
        currAlbumPosition = (int) getIntent().getSerializableExtra(CURRENT_ALBUM);
        if(currAlbumPosition > -1){currAlbum = AppData.getInstance().getAllAlbums().get(currAlbumPosition);}
        else currAlbum = (Album) getIntent().getSerializableExtra(RESULT_ALBUM);
        currPhotoPosition = (int) getIntent().getSerializableExtra(CURRENT_PHOTO);
        currPhoto = currAlbum.getPhotos().get(currPhotoPosition);

        if (currPhoto == null) {
            Toast.makeText(this, "Photo doesnt exist", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return;
        }

        SearchUtil.setupSearchButton(this);
        photoDisplayImageView = findViewById(R.id.photoDisplayImageView);
        photoTitleTextView = findViewById(R.id.photoTitleTextView);
        addTagButton = findViewById(R.id.addTagButton);
        photoTagsRecyclerView = findViewById(R.id.photoTagsRecyclerView);

        tagAdapter = new TagAdapter(this, currPhoto);
        photoTagsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        photoTagsRecyclerView.setAdapter(tagAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new LeftSwipeDelete<>(tagAdapter, tagAdapter));
        itemTouchHelper.attachToRecyclerView(photoTagsRecyclerView);

        gestureDetector = new GestureDetector(this, new SwipeGestureListener(this));
        photoDisplayImageView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        photoDisplayImageView.setOnClickListener(v -> {}); // Just so swipe works

        photoTitleTextView.setText(currPhoto.getPhotoUriString());
        photoDisplayImageView.setImageBitmap(AppData.bitmapFromUri(this,currPhoto.getPhotoUri()));

        addTagButton.setOnClickListener(view -> showTagDialog(this));
        findViewById(R.id.returnButton).setOnClickListener(view -> returnActivity(this));
    }

    public void showTagDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.add_tag_util, null);
        builder.setView(view);

        Spinner spinnerTagType = view.findViewById(R.id.spinnerTagType);
        AutoCompleteTextView autoCompleteTagValue = view.findViewById(R.id.autoCompleteTagValue);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(context, R.array.tag_types, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTagType.setAdapter(spinnerAdapter);

        List<String> tagValueList = AppData.getInstance().getAllTags().stream().map(Tag::getTagValue).collect(Collectors.toList());
        ArrayAdapter<String> tagValueAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tagValueList);
        autoCompleteTagValue.setAdapter(tagValueAdapter);
        autoCompleteTagValue.setThreshold(1);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String tagType = spinnerTagType.getSelectedItem().toString().trim();
            String tagValue = autoCompleteTagValue.getText().toString().trim();
            boolean singularity = tagType.equalsIgnoreCase("Location");
            if(!TextUtils.isEmpty(tagValue)
                    && ((!singularity && currPhoto.getTags().stream().noneMatch(t->t.getTagValue().equalsIgnoreCase(tagValue)))
                    || (singularity && currPhoto.getTags().stream().noneMatch(t -> t.getTagType().equalsIgnoreCase("Location"))))){
                Tag addTag = new Tag(tagType, tagValue, singularity);
                currPhoto.addTag(addTag);
                if(!AppData.getInstance().getAllTags().contains(addTag)) AppData.getInstance().getAllTags().add(addTag);
            }else{
                Toast.makeText(context, "Tag already exists", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onSwipeLeft() {
        if (currPhotoPosition < currAlbum.getPhotos().size() - 1) {
            currPhotoPosition++;
            updatePhotoDetails();
        } else {
            Toast.makeText(this, "Photo Final", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSwipeRight() {
        if (currPhotoPosition > 0) {
            currPhotoPosition--;
            updatePhotoDetails();
        } else {
            Toast.makeText(this, "Photo#1", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePhotoDetails() {
        currPhoto = currAlbum.getPhotos().get(currPhotoPosition);
        photoTitleTextView.setText(currPhoto.getPhotoUriString());
        photoDisplayImageView.setImageBitmap(AppData.bitmapFromUri(this, currPhoto.getPhotoUri()));
        tagAdapter.updatePhoto(currPhoto);
    }

    @Override
    public void returnActivity(Context context) {
        startActivity(AlbumActivity.newIntent(context, currAlbumPosition, currAlbum));
    }
}
