package com.streamliners.dialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.streamliners.dialog.Adapter.ItemAdapter;
import com.streamliners.dialog.databinding.ActivityGalleryBinding;
import com.streamliners.dialog.databinding.ItemCardBinding;
import com.streamliners.dialog.model.Item;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 0;
    ActivityGalleryBinding b;
    List<Item> items = new ArrayList<>();
    SharedPreferences prefs;
    ItemCardBinding itemBinding;

    ItemAdapter adapter;

    /**
     * It initialises the activity.
     * @param savedInstanceState : reference to a Bundle object that is passed into the onCreate method of every Android Activity
     */
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

        //Search functionality
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView)menu.findItem(R.id.searchBtn).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

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
        if(item.getItemId() == R.id.addFromGallery){
            addFromGallery();
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
                        inflateViewForItem(items);
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

    private void inflateViewForItem(List<Item> item){
        adapter = new ItemAdapter(this, items);
        b.list.setLayoutManager(new LinearLayoutManager(this));
        b.list.setAdapter(adapter);

        if (items.isEmpty()) {
            b.homeTextView.setVisibility(View.VISIBLE);
        } else {
            b.homeTextView.setVisibility(View.GONE);
        }
    }



    private void addFromGallery() {
        Intent openGallery = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGallery, RESULT_LOAD_IMAGE);
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

    public static Bitmap loadBitmapFromView(View v){
        Bitmap bitmap;
        v.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void shareItem(){

        itemBinding.shareCardButton.setOnClickListener(v -> {
            Bitmap icon = loadBitmapFromView(b.list);

//                Calling the intent to share the bitmap
            Intent shareBitmap = new Intent(Intent.ACTION_SEND);
            shareBitmap.setType("image/jpeg");

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "title");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            OutputStream outputStream;
            try {
                outputStream = GalleryActivity.this.getContentResolver().openOutputStream(uri);
                icon.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
            } catch (Exception e) {
                System.err.println(e.toString());
            }

            shareBitmap.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(shareBitmap, "Share Image"));
        });
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
            inflateViewForItem(items);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data){
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null,null,null);
            cursor.moveToFirst();
            cursor.close();
            String uri = selectedImage.toString();

            new AddFromDevice().show(this, uri, new AddFromDevice.OnCompleteListener() {
                @Override 
                public void onAddCompleted(Item item){
                    items.add(item);
                    inflateViewForItem(items);
                    b.homeTextView.setVisibility(View.GONE);
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
    }
}






