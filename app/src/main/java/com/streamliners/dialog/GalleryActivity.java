package com.streamliners.dialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.streamliners.dialog.databinding.ActivityGalleryBinding;
import com.streamliners.dialog.databinding.ItemCardBinding;
import com.streamliners.dialog.model.Item;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    ActivityGalleryBinding b;
    List<Item> items = new ArrayList<>();
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());


        prefs = getPreferences(MODE_PRIVATE);
        getDataFromSharedPreferences();
    }

//    Actions Menu Method---------------------------------------------------------------------------

    /**
     * To inflate optionsMenu
     * @param menu : Action Menu layout
     * @return true : It signifies that we handled this event
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }

    /**
     * Handle Click Events
     * @param item : item (addImage icon) present in the menu
     * @return true if the addImage icon is pressed else false
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_image){
            showAddImageDialog();

            return true;                                                    //return true signifies that we have handled this event.
        }
        return false;
    }

    /**
     * To show addImage Dialog
     */
    @SuppressLint("SourceLockedOrientationActivity")
    private void showAddImageDialog() {
        if(this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
//            To set the screen orientation in portrait mode
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        new AddImageDialog()
                .show(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        items.add(item);
                        inflateViewForItem(item);
                    }

                    @Override
                    public void onError(String error) {
                        new MaterialAlertDialogBuilder(GalleryActivity.this)
                                .setTitle("Error")
                                .setMessage(error)
                                .show();
                    }
                });
    }


    /**
     * To inflate view for the item
     * @param item to be added in the gallery activity in a card view
     */
    private void inflateViewForItem(Item item) {

//        Inflate Layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

//        Bind data
        Glide.with(this)
                .load(item.url)
                .into(binding.imageView);
        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);

//        add it to the list
        b.list.addView(binding.getRoot());

        b.homeTextView.setVisibility(View.GONE);

    }


    /**
     * TO get json for the item
     * we use the GSON library to convert the model class Object to JSON String and we save the JSON String into the SharedPreferences.
     * @param item to be added in the gallery activity in a card view
     * @return json representation of the item as a string
     */
    private String itemToJason(Item item){
        Gson json = new Gson();
        return json.toJson(item);
    }


    /**
     * To get item from json
     * We read back that JSON String and convert it back to the object when we want to read it.
     * @param string from which the object is to be deserialized into an object of the  specified class which is Item Class here.
     * @return the object we are getting from the string
     */
    private Item jsonToItem(String string){
        Gson json2 = new Gson();
        return json2.fromJson(string, Item.class);
    }


    /**
     * To get data back from SharedPreferences.
     */
    public void getDataFromSharedPreferences(){
        int itemCount = prefs.getInt(Constants.NO_OF_IMAGES, 0);

        for(int i = 1; i <= itemCount; i++){

//            Make a new item and get object from json
            Item item = jsonToItem(prefs.getString(Constants.ITEMS + i, ""));

            items.add(item);
            inflateViewForItem(item);
        }
    }


    /**
     * To save the data when the activity is in the paused state
     */
    @Override
    protected void onPause() {
        super.onPause();

//        Putting all the objects in the SharedPreferences
        int itemCount = 0;
        for(Item item : items){

//            Checking for the item
            if(item != null){
//                increment the index
                itemCount++;

//                Save the item in the shared Preferences
                prefs.edit()
                        .putString(Constants.ITEMS +itemCount, itemToJason(item))
                        .apply();
            }
        }
        prefs.edit()
                .putInt(Constants.NO_OF_IMAGES, itemCount)
                .apply();
    }

}






