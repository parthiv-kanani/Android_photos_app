package com.example.android22.Main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.example.android22.R;
import com.example.android22.album.AlbumActivity;
import com.example.android22.Base.*;
import com.example.android22.Base.SearchUtil;
import com.google.android.material.textfield.TextInputEditText;

public class AlbumsActivity extends AppCompatActivity implements AlbumsAdapter.OnItemClickListener {
    private RecyclerView albumsRecyclerView;
    private TextInputEditText albumNameEditText;
    private TextInputEditText currentlyClicked;
    private AlbumsAdapter albumsAdapter;

    private Button addbutton;
    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, AlbumsActivity.class);
        AppData.saveToFile(context);
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albums_view);
        AppData.loadFromFile(getApplicationContext());
        SearchUtil.setupSearchButton(this);
        albumsRecyclerView = findViewById(R.id.albumsRecyclerView);
        albumNameEditText = findViewById(R.id.albumName);
        albumNameEditText.setOnClickListener(v -> {
            if(currentlyClicked != null) currentlyClicked.clearFocus();
            currentlyClicked = (TextInputEditText) v;
        });
        addbutton = findViewById(R.id.addButton);
        albumsAdapter = new AlbumsAdapter(this);
        albumsRecyclerView.setAdapter(albumsAdapter);
        albumsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new LeftSwipeDelete<>(albumsAdapter, albumsAdapter));
        itemTouchHelper.attachToRecyclerView(albumsRecyclerView);

        View rootView = getWindow().getDecorView().getRootView();
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!isTouchInsideView(event, albumNameEditText)) {
                    refocus(null);
                }
            }
            v.performClick();
            return false;
        });

        addbutton.setOnClickListener(view -> {
            String albumName = (albumNameEditText.getText() != null) ? albumNameEditText.getText().toString().trim() : null;
            if (!TextUtils.isEmpty(albumName) && AppData.getInstance().getAllAlbums().stream().noneMatch(a -> a.getName().equals(albumName.trim()))) {
                AppData.getInstance().getAllAlbums().add(new Album(albumName.trim()));
                albumNameEditText.setText("");
                albumsAdapter.notifyItemInserted(AppData.getInstance().getAllAlbums().size() - 1);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(albumNameEditText.getWindowToken(), 0);
            } else {
                albumNameEditText.setText("");
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(albumNameEditText.getWindowToken(), 0);
                Toast.makeText(this, "Already Exists", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onItemClick(int position) {
        AlbumsAdapter.AlbumViewHolder viewHolder = (AlbumsAdapter.AlbumViewHolder) albumsRecyclerView.findViewHolderForAdapterPosition(position);
        refocus(viewHolder.textAlbumItemName);
    }


    private void refocus(TextInputEditText t){
        if(currentlyClicked != null){
            currentlyClicked.clearFocus();
            if(currentlyClicked != albumNameEditText) {
                currentlyClicked.setEnabled(false);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(albumNameEditText.getWindowToken(), 0);
            }
        }
        currentlyClicked = t;
        if(t != null) t.requestFocus();
    }

    @Override
    public void onItemDoubleClick(int position) {
        Album selectedAlbum = AppData.getInstance().getAllAlbums().get(position);
        startActivity(AlbumActivity.newIntent(this, position, selectedAlbum));
    }


    @Override
    public void onItemLongClick(int position) {
        AlbumsAdapter.AlbumViewHolder viewHolder = (AlbumsAdapter.AlbumViewHolder) albumsRecyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            TextInputEditText albumItemNameEditText = viewHolder.textAlbumItemName;
            albumItemNameEditText.setEnabled(true);
            albumItemNameEditText.requestFocus();

            albumItemNameEditText.setOnKeyListener((view, keyCode, event) -> {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    handleEnterKey(position, albumItemNameEditText);
                    return true;
                }
                return false;
            });
        }
    }


    private void handleEnterKey(int position, TextInputEditText albumItemNameEditText) {
        String oldName = AppData.getInstance().getAllAlbums().get(position).getName();
        String albumName = albumItemNameEditText.getText().toString();
        if ((!TextUtils.isEmpty(albumName) || !albumName.equals("")) &&
                !AppData.getInstance().getAllAlbums()
                        .stream()
                        .anyMatch(a -> a.getName().equals(albumName))) {
            AppData.getInstance().getAllAlbums().get(position).setName(albumName);
            albumItemNameEditText.setEnabled(false);
        } else {
            // Restore the previous name since invalid
            albumItemNameEditText.setText(oldName);
            albumItemNameEditText.setEnabled(false);
            Toast.makeText(this, "Incorrect Name", Toast.LENGTH_LONG).show();
        }
    }


    private boolean isTouchInsideView(MotionEvent event, View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        float x = event.getRawX();
        float y = event.getRawY();
        return x > location[0] && x < location[0] + view.getWidth() && y > location[1] && y < location[1] + view.getHeight();
    }



    @Override
    protected void onPause() {
        super.onPause();
        AppData.saveToFile(getApplicationContext());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppData.saveToFile(getApplicationContext());
    }
}
