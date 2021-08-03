package com.streamliners.dialog;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.dialog.databinding.ChipColorBinding;
import com.streamliners.dialog.databinding.ChipLabelBinding;
import com.streamliners.dialog.databinding.DialogImageOperationsBinding;
import com.streamliners.dialog.model.Item;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ImageDialogs implements ItemHelper.OnCompleteListener {
    private Context context;
    private OnCompleteListener listener;
    private DialogImageOperationsBinding b;
    private Bitmap image;
    private Set<Integer> colors;
    private List<String> labels;
    private LayoutInflater inflater;
    private boolean isCustomLabel;
    private AlertDialog dialog;
    private String imageUrl;
    private Item item;

    /**
     * To initializing the dialog
     * @param context : To show the dialog, context is needed; hence, it is passed.
     * @param listener : For creating the asynchronous callback
     */
    void show(Context context, OnCompleteListener listener){

        if(!initializingDialog(context, listener)){
            return;
        }

         handleDimensionsInput();
//         Hiding errors for edit text
        hideErrorsForET();

//        handleShareImageEvent();
    }

    /**
     * To initializing the dialog
     * @param context : To show the dialog, context is needed; hence, it is passed.
     * @param listener : For creating the asynchronous callback
     * @return boolean true
     */
    private boolean initializingDialog(Context context, OnCompleteListener listener){
        this.context = context;
        this.listener = listener;

//        Inflate Dialogs layout
        if(context instanceof GalleryActivity){
            inflater = ((GalleryActivity) context).getLayoutInflater();
            b = DialogImageOperationsBinding.inflate(inflater);
        }
        else{
            dialog.dismiss();
            listener.onError("Cast Exception");
            return false;
        }

        //    Create and show dialog
        dialog = new MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
                .setView(b.getRoot())
                .show();

        return true;
    }


    //Utils-------------------------------------------------------------------------------------------------
    /**
     * For hiding errors of the edit texts
     */
    private void hideErrorsForET() {
        b.widthET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                b.widthET.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        b.customLabelTIL.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                b.customLabelTIL.getEditText().setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * To hide the keyboard
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(b.widthTIL.getWindowToken(), 0);
    }

    //    Step 1: Input Dimensions----------------------------------------------------------------------
    /**
     * To handle user input of dimensions
     */
    private void handleDimensionsInput(){
        b.fetchImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    Get Strings from edit text
                String widthStr = b.widthET.getText().toString().trim()
                        ,heightStr = b.heightET.getText().toString().trim();

//                    Guard Code
                if(widthStr.isEmpty() && heightStr.isEmpty()){
                    b.widthET.setError("Please enter at least one dimension!");
                    return;
                }
//                    UI update
                b.inputDimensionsRoot.setVisibility(View.GONE);
                b.progressIndicatorRoot.setVisibility(View.VISIBLE);

//                    Hide Keyboard
                hideKeyboard();


//                    Square Image
                if(widthStr.isEmpty()){
                    int height = Integer.parseInt(heightStr);
                    try {
                        fetchRandomImage(height);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(heightStr.isEmpty()){
                    int width = Integer.parseInt(widthStr);
                    try {
                        fetchRandomImage(width);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

//                    Rectangular Image
                else {
                    int height = Integer.parseInt(heightStr);
                    int width = Integer.parseInt(widthStr);
                    try {
                        fetchRandomImage(width, height);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    /*private void handleShareImageEvent() {
        b.shareImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Glide.with(context)
                            .asBitmap()
                            .load(imageUrl)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    // Calling the intent to share the bitmap
                                    Bitmap icon = resource;
                                    Intent share = new Intent(Intent.ACTION_SEND);
                                    share.setType("image/jpeg");

                                    ContentValues values = new ContentValues();
                                    values.put(MediaStore.Images.Media.TITLE, "title");
                                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                    Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            values);


                                    OutputStream outputStream;
                                    try {
                                        outputStream = context.getContentResolver().openOutputStream(uri);
                                        icon.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                        outputStream.close();
                                    } catch (Exception e) {
                                        System.err.println(e.toString());
                                    }

                                    share.putExtra(Intent.EXTRA_STREAM, uri);
                                    context.startActivity(Intent.createChooser(share, "Share Image"));
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });

                } catch (Exception e) {
                    Log.e("Error on sharing", e + " ");
                    Toast.makeText(context, "App not Installed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
*/


    /**
     * Rectangular Image
     * @param width of the rectangle
     * @param height of the rectangle
     * @throws IOException
     */
    //    Step 2: Fetching Random Image
//        For Rectangular image---------------------------------------------------------------------
    private void fetchRandomImage(int width, int height) throws IOException {
        new ItemHelper()
                .fetchData(width, height, context,this);
    }

    /**
     * For Square Image
     * @param x Side of the square
     * @throws IOException
     */
    //    For Square Image
    private void fetchRandomImage(int x) throws IOException {
        new ItemHelper()
                .fetchData(x, context,this);
    }

    /**
     * Fetch image from device
     * @param url Image url
     * @param context To show the dialog, context is needed; hence, it is passed.
     * @param listener For creating asynchronous callback.
     */
    public void fetchDataFromDevice(String url,Context context,OnCompleteListener listener) throws IOException {
        this.listener = listener;
        this.context = context;

        if (context instanceof GalleryActivity) {
            inflater = ((GalleryActivity) context).getLayoutInflater();
            b = DialogImageOperationsBinding.inflate(inflater);
        } else {
            dialog.dismiss();
            listener.onError("Cast Exception");
            return;
        }

        dialog = new MaterialAlertDialogBuilder(context)
                .setView(b.getRoot())
                .show();

        b.inputDimensionsRoot.setVisibility(View.GONE);
        b.progressSubtitle.setText(R.string.fetching_image);
        b.progressIndicatorRoot.setVisibility(View.VISIBLE);

        new ItemHelper()
                .fetchData(url,context,this);

    }




//    Step 3: Show Data-----------------------------------------------------------------------------
    private void showData(String url, Set<Integer> colors, List<String> labels) {
//      Set url of the image
        this.imageUrl = url;
        b.imageView.setImageBitmap(image);
        inflateColorChips(colors);
        inflateLabelChips(labels);
        handleCustomDialogInput();
        handleAddImageEvent();

//        Setting image view in binding
        Glide.with(context)
                .load(url)
                .into(b.imageView);

        b.progressIndicatorRoot.setVisibility(View.GONE);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customLabelTIL.setVisibility(View.GONE);

    }

    //Handle edit card events----------------------------------------------------------------------------------------
    public void editFetchImage(Context context, Item item, OnCompleteListener listener){
        this.imageUrl = item.url;
        this.item = item;

        if(!initializingDialog(context,listener)){
            return;
        }

        b.dialogHeader.setText(R.string.edit_image);
        b.addBtn.setText(R.string.update);
        b.progressSubtitle.setText(R.string.loading_image);
        b.chooseLabelTitle.setText(R.string.choose_a_new_label);
        b.choosePaletteTitle.setText(R.string.choose_a_new_palette_color);
        editCard(imageUrl);
    }

    private void editCard(String imageUrl) {
        b.inputDimensionsRoot.setVisibility(View.GONE);
        b.progressIndicatorRoot.setVisibility(View.VISIBLE);

        new ItemHelper().editCard(imageUrl, context,this);
    }

    private void handleAddImageEvent() {
        b.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorChipId = b.colorChips.getCheckedChipId()
                        ,labelChipId = b.labelChips.getCheckedChipId();

//                Guard Code
                if(colorChipId == -1 || labelChipId == -1){
                    Toast.makeText(context, "Please choose color & label", Toast.LENGTH_SHORT).show();
                    return;
                }

                String label;
                if(isCustomLabel) {
                    label = b.customLabelET.getText().toString().trim();
                    if (label.isEmpty()){
                        Toast.makeText(context, "Please enter custom label!", Toast.LENGTH_SHORT).show();
//                        b.customLabelInput.setError("Please enter custom label!");
                        return;
                    }
                }
                else{
                    label = ((Chip) b.labelChips.findViewById(labelChipId)).getText().toString();
                }

//                Get Color and label
                int color = ((Chip) b.colorChips.findViewById(colorChipId)).getChipBackgroundColor().getDefaultColor();

//                Send callback
                listener.onImageAdded(new Item(imageUrl, color, label)  );
                dialog.dismiss();

            }
        });
    }

    private void handleCustomDialogInput() {
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText("Custom");
        b.labelChips.addView(binding.getRoot());

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    b.customLabelTIL.setVisibility( isChecked? View.VISIBLE : View.GONE);
                    isCustomLabel = isChecked;
            }
        });
    }

    //    Color Chips
    private void inflateColorChips(Set<Integer> colors) {
        for(int color: colors){
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            b.colorChips.addView(binding.getRoot());
        }
    }

//    Label Chips
    private void inflateLabelChips(List<String> labels) {
        for(String label: labels){
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText(label); //?
            b.labelChips.addView(binding.getRoot());
        }
    }

//    Item helper Callbacks-------------------------------------------------------------------------
    @Override
    public void onFetched(String url, Set<Integer> colors, List<String> labels) {
        showData(url, colors, labels);
    }

    @Override
    public void onError(String error) {
        listener.onError(error);
        dialog.dismiss();
    }

    interface OnCompleteListener{
        void onImageAdded(Item item);
        void onError(String error);
    }
}
