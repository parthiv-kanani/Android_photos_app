package com.example.android22.Base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;

public class AppData implements Serializable {
    private static final String FILENAME = "appdata.ser";
    private static AppData instance;
    private ArrayList<Album> allAlbums;
    private ArrayList<Tag> allTags;

    private AppData() {
        allAlbums = new ArrayList<>();
        allTags = new ArrayList<>();
    }

    public static AppData getInstance() {
        if (instance == null) {
            instance = new AppData();
        }
        return instance;
    }

    public ArrayList<Album> getAllAlbums() {
        return allAlbums;
    }

    public ArrayList<Tag> getAllTags() {
        return allTags;
    }

    // Save the AppData instance to a file
    public static void saveToFile(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getInstance());
            fos.close();
            oos.close();
        } catch (IOException e) {
            Log.e("MainActivity", "Error during serialization", e);
        }
    }

    public static void loadFromFile(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            instance = (AppData) ois.readObject();
            fis.close();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap bitmapFromUri(Context context, Uri photoUri) {
        try {
            context.getContentResolver().takePersistableUriPermission(photoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(photoUri, "r");
            if (parcelFileDescriptor != null) {
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor());
                parcelFileDescriptor.close();
                return bitmap;
            }
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
