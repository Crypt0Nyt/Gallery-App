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
import com.streamliners.dialog.databinding.DialogAddImageBinding;
import com.streamliners.dialog.model.Item;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class AddImageDialog implements ItemHelper.OnCompleteListener {
    private Context context;
    private OnCompleteListener listener;
    private DialogAddImageBinding b;
    private Bitmap image;
    private Set<Integer> colors;
    private List<String> labels;
    private LayoutInflater inflater;
    private boolean isCustomLabel;
    private AlertDialog dialog;
    private String imageUrl;

    void show(Context context, OnCompleteListener listener){
        this.context = context;
        this.listener = listener;

//        Inflate Dialogs layout
         if(context instanceof GalleryActivity){
             inflater = ((GalleryActivity) context).getLayoutInflater();
         b = DialogAddImageBinding.inflate(inflater);
         }
         else{
             dialog.dismiss();
             listener.onError("Cast Exception");
            return;
         }
        //    Create and show dialog
         dialog = new MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
            .setView(b.getRoot())
                .show();

         handleDimensionsInput();
//         Hiding errors for edit text
        hideErrorsForET();
    }

    private void hideErrorsForET() {
        b.width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                b.width.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        b.customLabelInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                b.customLabelInput.getEditText().setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

//    Step 1: Input Dimensions----------------------------------------------------------------------
    private void handleDimensionsInput(){
            b.fetchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Get Strings from edit text
                    String widthStr = b.width.getText().toString().trim()
                            ,heightStr = b.height.getText().toString().trim();

//                    Guard Code
                    if(widthStr.isEmpty() && heightStr.isEmpty()){
                        b.widthInputText.setError("Please enter at least one dimension!");
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

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(b.width.getWindowToken(), 0);
    }


    //    Step 2: Fetching Random Image
//        For Rectangular image---------------------------------------------------------------------
    private void fetchRandomImage(int width, int height) throws IOException {
        new ItemHelper()
                .fetchData(width, height, context,this);
    }

    //    For Square Image
    private void fetchRandomImage(int x) throws IOException {
        new ItemHelper()
                .fetchData(x, context,this);
    }

//    Step 3: Show Data-----------------------------------------------------------------------------
    private void showData(String url, Set<Integer> colors, List<String> labels) {
//      Set url of the image
        this.imageUrl = url;
        b.dialogImage.setImageBitmap(image);
        inflateColorChips(colors);
        inflateLabelChips(labels);
        handleCustomDialogInput();
        handleAddImageEvent();

//        Setting image view in binding
        Glide.with(context)
                .load(url)
                .into(b.dialogImage);

        b.progressIndicatorRoot.setVisibility(View.GONE);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customLabelInput.setVisibility(View.GONE);

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
                    b.customLabelInput.setVisibility( isChecked? View.VISIBLE : View.GONE);
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
