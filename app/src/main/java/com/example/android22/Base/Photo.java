package com.example.android22.Base;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;


public class Photo implements Serializable {

    private ArrayList<Tag> tagList;


    private String photoUriString;


    public Photo(Uri uri) {
        tagList = new ArrayList<>();
        this.photoUriString = uri.toString();
    }

    public ArrayList<Tag> getTags() {
        return tagList;
    }


    public void addTag(Tag tag) {
        tagList.add(tag);
    }


    public void removeTag(Tag tag) {
        tagList.removeIf(t -> t.equals(tag));
    }


    public boolean hasTag(Tag tag) {
        for (Tag t : tagList) {
            if (t.equals(tag)) return true;
        }
        return false;
    }


    public String getPhotoUriString() {
        return photoUriString;
    }



    public Uri getPhotoUri() {
        return Uri.parse(photoUriString);
    }


    public void setPhotoUri(Uri uri) {
        photoUriString = uri.toString();
    }
}
