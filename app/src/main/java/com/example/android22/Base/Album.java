package com.example.android22.Base;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Album implements Serializable {
    private List<Photo> photoList;
    private String albumName;

    public Album(String albumName) {
        this.albumName = albumName;
        this.photoList = new ArrayList<>();
    }

    public int getNumPhotos() {
        return photoList.size();
    }

    public String getName() {
        return albumName;
    }

    public void setName(String newName) {
        this.albumName = newName;
    }

    public void addPhoto(Photo photo) {
        if (photo != null) {
            photoList.add(photo);
        }
    }

    public void removePhoto(Photo photo) {
        photoList.removeIf(p -> Objects.equals(p, photo));
    }

    public List<Photo> getPhotos() {
        return new ArrayList<>(photoList);
    }

    public boolean hasPhoto(Uri photoUri) {
        String normalizedUri = normalizeUri(photoUri);
        for (Photo photo : photoList) {
            if (Objects.equals(normalizeUri(photo.getPhotoUri()), normalizedUri)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeUri(Uri uri) {
        String uriPath = uri.getPath();
        String reg = "\\/ORIGINAL\\/NONE\\/image\\/.*\\/[0-9]*";
        return uriPath.replaceAll(reg, "");
    }


    @Override
    public String toString() {
        return albumName;
    }
}
