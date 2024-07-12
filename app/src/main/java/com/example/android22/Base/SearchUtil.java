

package com.example.android22.Base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android22.R;
import com.example.android22.album.AlbumActivity;
import com.example.android22.Tag.TagAutoCompleteAdapter;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SearchUtil {

    private static AppCompatActivity activityInitialized;


    public static void setupSearchButton(AppCompatActivity activity) {
        activityInitialized = activity;
        activity.findViewById(R.id.searchButton).setOnClickListener(view -> showSearchDialog(activity));
    }

    public static void showSearchDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.search_util, null);
        builder.setView(view);

        AutoCompleteTextView autoCompleteTag1 = view.findViewById(R.id.autoCompleteTextViewTag1);
        Spinner spinnerOperator = view.findViewById(R.id.spinnerOperator);
        spinnerOperator.setVisibility(View.GONE);
        AutoCompleteTextView autoCompleteTag2 = view.findViewById(R.id.autoCompleteTextViewTag2);
        autoCompleteTag2.setVisibility(View.GONE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.operator_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOperator.setAdapter(adapter);

        List<Tag> allTags = AppData.getInstance().getAllTags();
        TagAutoCompleteAdapter tagValueAdapter = new TagAutoCompleteAdapter(context, allTags);
        autoCompleteTag1.setAdapter(tagValueAdapter);
        autoCompleteTag2.setAdapter(tagValueAdapter);

        autoCompleteTag1.setOnItemClickListener((parent, view1, position, id) -> {
            Tag selectedTag = tagValueAdapter.getItem(position);
            autoCompleteTag1.setTag(selectedTag);
            assert selectedTag != null;
            autoCompleteTag1.setText(selectedTag.toString());
            spinnerOperator.setVisibility(View.VISIBLE);
        });
        autoCompleteTag2.setOnItemClickListener((parent, view1, position, id) -> {
            Tag selectedTag = tagValueAdapter.getItem(position);
            autoCompleteTag2.setTag(selectedTag);
            autoCompleteTag2.setText(selectedTag.toString());
        });
        spinnerOperator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedOperator = spinnerOperator.getSelectedItem().toString();
                autoCompleteTag2.setVisibility((!selectedOperator.equalsIgnoreCase("N/A"))?View.VISIBLE:View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        autoCompleteTag1.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                String enteredText = autoCompleteTag1.getText().toString().trim();
                Optional<Tag> typedTag = AppData.getInstance().getAllTags().stream()
                        .filter(tag -> enteredText.equals(tag.toString())).findFirst();
                if(typedTag.isPresent()){
                    autoCompleteTag1.setTag(typedTag.get());
                    spinnerOperator.setVisibility(View.VISIBLE);
                }else{
                    autoCompleteTag1.setTag(null);
                    spinnerOperator.setVisibility(View.GONE);
                }
                return true;
            }
            return false;
        });
        autoCompleteTag2.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String enteredText = autoCompleteTag2.getText().toString().trim();
                Optional<Tag> typedTag = AppData.getInstance().getAllTags().stream()
                        .filter(tag -> enteredText.equals(tag.toString())).findFirst();
                if(typedTag.isPresent()){
                    autoCompleteTag2.setTag(typedTag.get());
                }else{
                    autoCompleteTag2.setTag(null);
                }
                return true;
            }
            return false;
        });


        builder.setPositiveButton("Search", (dialog, which) -> {
            // Retrieve selected tags and operator
            Tag tag1 = (Tag) autoCompleteTag1.getTag();
            String operator = spinnerOperator.getSelectedItem().toString();
            Tag tag2 = (Tag) autoCompleteTag2.getTag();

            boolean illegalTag1 = (tag1 == null);
            boolean illegalTag2 = !operator.equals("N/A") && (tag2 == null);
            if(!illegalTag1 && !illegalTag2){
                context.startActivity(AlbumActivity.newSearchIntent(context, tag1, operator, tag2));
            }else{
                if(illegalTag1){Toast.makeText(context, "Non Valid Tag", Toast.LENGTH_LONG).show();}
                else if (illegalTag2){Toast.makeText(context, "Non Valid Tag", Toast.LENGTH_LONG).show();}
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public static Album tagFilter(Tag tag1, String operator, Tag tag2) {
        Album result = new Album("");
        Predicate<Photo> pred;

        switch (operator) {
            case "AND":
                pred = (Photo photo) -> (photo.hasTag(tag1) && photo.hasTag(tag2));
                break;
            case "OR":
                pred = (Photo photo) -> (photo.hasTag(tag1) || photo.hasTag(tag2));
                break;
            default:
                pred = (Photo photo) -> (photo.hasTag(tag1));
                break;
        }

        AppData.getInstance().getAllAlbums().stream()
                .flatMap(album -> album.getPhotos().stream())
                .filter(currPhoto -> !(result.getPhotos().contains(currPhoto)) && pred.test(currPhoto))
                .forEach(result::addPhoto);

        return result;
    }
}
