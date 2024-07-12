package com.example.android22.album;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android22.R;
import com.example.android22.Main.AlbumsActivity;
import com.example.android22.Base.*;
//import com.example.android22.common.SearchUtil;

public class AlbumActivity extends AppCompatActivity implements ReturnUtilInterface{
    private Album currAlbum;
    private int currAlbumPosition;
    private static final String SELECTED_ALBUM = "selectedAlbum";
    private static final String RESULT_ALBUM = "resultAlbum";

    private static final String TAG1 = "tag1";
    private static final String TAG2 = "tag2";
    private static final String OPERATOR = "operator";
    private AlbumAdapter albumAdapter;
    private ImageView deleteIcon;
    private ImageView moveIcon;

    public static Intent newIntent(Context context, int selectedAlbumPosition, Album resultAlbum) {
        Intent intent = new Intent(context, AlbumActivity.class);
        intent.putExtra(SELECTED_ALBUM, selectedAlbumPosition);
        intent.putExtra(RESULT_ALBUM, resultAlbum);
        AppData.saveToFile(context);
        return intent;
    }

    public static Intent newSearchIntent(Context context, Tag tag1, String operator, Tag tag2) {
        Intent intent = new Intent(context, AlbumActivity.class);
        intent.putExtra(TAG1, tag1);
        intent.putExtra(OPERATOR, operator);
        intent.putExtra(TAG2,tag2);
        AppData.saveToFile(context);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_view);
        // Retrieve data passed from AlbumsActivity
        Intent intent = getIntent();
        if (intent.hasExtra(SELECTED_ALBUM)) {
            currAlbumPosition = (int) getIntent().getSerializableExtra(AlbumActivity.SELECTED_ALBUM);
            currAlbum = (currAlbumPosition == -1) ? (Album) getIntent().getSerializableExtra(RESULT_ALBUM) : AppData.getInstance().getAllAlbums().get(currAlbumPosition);
        } else if (intent.hasExtra(TAG1) && intent.hasExtra(OPERATOR) && intent.hasExtra(TAG2)) {
            Tag tag1 = (Tag) intent.getSerializableExtra(TAG1);
            String operator = intent.getStringExtra(OPERATOR);
            Tag tag2 = (Tag) intent.getSerializableExtra(TAG2);
            //currAlbum = SearchUtil.tagFilter(tag1, operator, tag2);
            currAlbumPosition = -1;
            if(operator.equals("N/A")){
                currAlbum.setName("Search Result for:: " + tag1);
            }else{
                currAlbum.setName("Search Result for:: " + tag1 + " / " + operator + " / " + tag2);
            }
        }

        TextView albumTitleTextView = findViewById(R.id.albumTitleTextView);
        albumTitleTextView.setText(currAlbum.getName());

        RecyclerView photoRecyclerView = findViewById(R.id.photoRecyclerView);
        deleteIcon = findViewById(R.id.deleteIcon);
        moveIcon = findViewById(R.id.moveIcon);
        photoRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        albumAdapter = new AlbumAdapter(currAlbumPosition, currAlbum, this, deleteIcon, moveIcon);
        photoRecyclerView.setAdapter(albumAdapter);

        findViewById(R.id.returnButton).setOnClickListener(view -> returnActivity(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (albumAdapter != null) {
            albumAdapter.onActivityResult(requestCode, resultCode, data);

        }
    }


    public void onDeleteClick(View view) {
        currAlbum.removePhoto(albumAdapter.getCurrentlyClickedPhoto());
        albumAdapter.notifyDataSetChanged();
        deleteIcon.setVisibility(View.GONE);
        moveIcon.setVisibility(View.GONE);
    }


    public void onMoveClick(View view) {
        showMovePhotoPopup(view);
        deleteIcon.setVisibility(View.GONE);
        moveIcon.setVisibility(View.GONE);
    }

    private void showMovePhotoPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        for (int i = 0; i < AppData.getInstance().getAllAlbums().size(); i++) {
            Album currentAlbum = AppData.getInstance().getAllAlbums().get(i);
            if(currentAlbum!=currAlbum){
                popupMenu.getMenu().add(0, i, i, currentAlbum.getName());
            }
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            Album selectedAlbum = AppData.getInstance().getAllAlbums().get(item.getItemId());
            Photo movePhoto = albumAdapter.getCurrentlyClickedPhoto();

            if(albumAdapter.getItemCount()==1){
                Toast.makeText(view.getContext(),"This is only album",Toast.LENGTH_SHORT).show();
            } else if(!selectedAlbum.getPhotos().contains(movePhoto)){
                currAlbum.removePhoto(movePhoto);
                selectedAlbum.addPhoto(movePhoto);
                albumAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(view.getContext(),"Photo Already Exists " + selectedAlbum.getName(),Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popupMenu.show();
    }

    @Override
    public void returnActivity(Context context) {
        startActivity(AlbumsActivity.newIntent(context));
    }
}
