package com.streamliners.dialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.streamliners.dialog.Adapter.ItemAdapter;
import com.streamliners.dialog.databinding.ActivityGalleryBinding;
import com.streamliners.dialog.databinding.ItemCardBinding;
import com.streamliners.dialog.model.Item;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 0;
    private static final int RESULT = 1001;
    ActivityGalleryBinding b;
    List<Item> items = new ArrayList<>();
    SharedPreferences prefs;
    ItemCardBinding itemBinding;

    ItemAdapter adapter;
    ItemTouchHelper itemTouchHelper;
    int mode = 0;


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

        enableDisableDrag();
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

        new ImageDialogs()
                .show(this, new ImageDialogs.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        items.add(item);
                        inflateViewForItem();
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

    private void inflateViewForItem(){
        adapter = new ItemAdapter(this, items);
        b.list.setLayoutManager(new LinearLayoutManager(this));
        b.list.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper1 = new ItemTouchHelper(simpleItemTouchCallback);
        adapter.setListItemAdapterHelper(itemTouchHelper);
        itemTouchHelper1.attachToRecyclerView(b.list);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        adapter.setListItemAdapterHelper(itemTouchHelper);

        dragDropButtonRestore();
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

    //Context menu events-------------------------------------------------------------------------------------------------------------------------------


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        itemBinding = adapter.itemcardBinding;

        //For Sharing image option
        if(item.getItemId() == R.id.ShareCard){
            sharePermissions();
            return true;
        }

        //For edit image option
        if(item.getItemId() == R.id.editCard){
            editImage();
            return true;
        }

        //For delete image options
        if(item.getItemId() == R.id.deleteCard){
            Toast.makeText(this, "Swipe to delete the item!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void editImage() {
        int index = adapter.index;
        itemBinding = adapter.itemcardBinding;
        new ImageDialogs().editFetchImage(this, items.get(index), new ImageDialogs.OnCompleteListener() {
            @Override
            public void onImageAdded(Item item) {
                items.set(index, item);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                new MaterialAlertDialogBuilder(GalleryActivity.this)
                        .setTitle("ERROR")
                        .setMessage(error)
                        .show();

            }
        });

    }

    //Swipe to Remove & Drag and drop Functionality---------------------------------------------------------
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAbsoluteAdapterPosition();
            items.remove(position);

            Toast.makeText(GalleryActivity.this, "Item Removed!", Toast.LENGTH_SHORT).show();
            if(items.isEmpty())
                b.list.setVisibility(View.VISIBLE);

            adapter.notifyDataSetChanged();
        }
    };

    private void enableDisableDrag(){
        b.fabDragListener.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForColorStateLists")
            @Override
            public void onClick(View view) {
                if(mode == 0){
                    mode = 1;
                    adapter.mode = 1;
                    Toast.makeText(GalleryActivity.this, "Drag Enabled!", Toast.LENGTH_SHORT).show();
                    List<ItemAdapter.ItemViewHolder> holders = adapter.holderList;
                    b.fabDragListener.setBackgroundTintList(getResources().getColorStateList(R.color.teal_200));
                    b.fabDragListener.setRippleColor(getResources().getColorStateList(R.color.teal_700));

                    b.fabDragListener.setImageResource(R.drawable.ic_drag_button);
                    for(int i = 0; i < holders.size(); i++){
                        holders.get(i).eventListenerHandler();
                    }
                    itemTouchHelper.attachToRecyclerView(b.list);
                }

                else{
                    mode = 0;
                    adapter.mode = 0;
                    Toast.makeText(GalleryActivity.this, "Drag Disabled!", Toast.LENGTH_SHORT).show();
                    List<ItemAdapter.ItemViewHolder> holders = adapter.holderList;
                    for(int i = 0; i < holders.size(); i++){
                        holders.get(i).eventListenerHandler();
                    }
                    b.fabDragListener.setBackgroundTintList(getResources().getColorStateList(R.color.teal_200));
                    b.fabDragListener.setRippleColor(getResources().getColorStateList(R.color.teal_700));
                    b.fabDragListener.setImageResource(R.drawable.ic_menu);
                    itemTouchHelper.attachToRecyclerView(null);
                }
            }
        });
    }

    void dragDropButtonRestore(){
        if(mode == 1){
            adapter.mode = 1;
            List<ItemAdapter.ItemViewHolder> holders = adapter.holderList;
            b.fabDragListener.setBackgroundTintList(getResources().getColorStateList(R.color.teal_200));
            b.fabDragListener.setRippleColor(getResources().getColorStateList(R.color.teal_700));

            b.fabDragListener.setImageResource(R.drawable.ic_drag_button);
            for(int i = 0; i < holders.size(); i++){
                holders.get(i).eventListenerHandler();
            }
            itemTouchHelper.attachToRecyclerView(b.list);
        }
        else{
            mode = 0;
            adapter.mode = 0;
            List<ItemAdapter.ItemViewHolder> holders = adapter.holderList;
            for (int i = 0; i < holders.size(); i++) {
                holders.get(i).eventListenerHandler();
            }
            b.fabDragListener.setBackgroundTintList(getResources().getColorStateList(R.color.teal_200));
            b.fabDragListener.setRippleColor(getResources().getColorStateList(R.color.teal_700));
            b.fabDragListener.setImageResource(R.drawable.ic_menu);
            itemTouchHelper.attachToRecyclerView(null);
        }
    }

    private void sharePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

                requestPermissions(permission, RESULT);
            }

            else
            {
               shareItem(itemBinding);
            }
        }
        else{
            shareItem(itemBinding);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            shareItem(itemBinding);
        }
        else
            Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
    }

    public static Bitmap loadBitmapFromView(View v){
        Bitmap bitmap;
        v.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void shareItem(ItemCardBinding itemBinding){
            Bitmap icon = loadBitmapFromView(itemBinding.getRoot());

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
            inflateViewForItem();
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

            //show data
            try {
                new ImageDialogs().fetchDataFromDevice(uri, this, new ImageDialogs.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        items.add(item);
                        inflateViewForItem();
                        b.homeTextView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String error) {
                        new MaterialAlertDialogBuilder(GalleryActivity.this)
                                .setTitle("ERROR")
                                .setMessage(error)
                                .show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}






