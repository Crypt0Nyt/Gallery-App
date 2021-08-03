package com.streamliners.dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemHelper {
    private Context context;
    private OnCompleteListener listener;
    private String rectangularImageURL = "https://picsum.photos/%d/%d";
    private String squareImageURL = "https://picsum.photos/%d";

    private Bitmap bitmap;
    private Set<Integer> colors;
    private String redirectUrl;

    //    Triggers--------------------------------------------------------------------------------------

    //    For Rectangular Image
    void fetchData(int x, int y, Context context, OnCompleteListener listener) throws IOException {
        this.context = context;

        this.listener = listener;
        fetchUrl(String.format(rectangularImageURL, x, y));
    }

    /**
     * For Square image
     * @param x side of square
     * @param context context of the current state of the application
     *                As Glide needs context, hence we defined it
     * @param listener  for creating asynchronous callback
     *                  Listeners are used for any type of asynchronous event
     *                  in order to implement the code to run when an event occurs
     */
    public void fetchData(int x, Context context, OnCompleteListener listener) throws IOException {

        this.context = context;
        this.listener = listener;

        //...fetch here & when done,
        //Call listener.onFetched(image, colors, labels);

        fetchUrl(String.format(squareImageURL, x));
    }

    /**
     * Fetch data to edit
     * @param url of image
     * @param context context of the current state of the application
     *              As Glide needs context, hence we defined it
     * @param listener for creating asynchronous callback
     *                 Listeners are used for any type of asynchronous event
     *                 in order to implement the code to run when an event occurs
     */
    public void fetchData(String url,Context context,OnCompleteListener listener ){
        this.context = context;
        this.listener = listener;
        redirectUrl = url;
        fetchImage(url);
    }



    //  For Fetching Url--------------------------------------------------------------------------------
    void fetchUrl(String url) throws IOException {
        new RedirectURLHelper().fetchRedirectURL(new RedirectURLHelper.OnCompleteListener() {
            @Override
            public void OnFetchedRedURL(String url) {
                redirectUrl = url;
                fetchImage(redirectUrl);
            }
        }).execute(url);
    }


//  Image Fetcher-----------------------------------------------------------------------------------
    void fetchImage(String URL){
        Glide.with(context)
                .asBitmap()
                .load(URL)
               /* .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)*/
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmap = resource;
                        extractPaletteFromBitmap();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        listener.onError("Image load failed!");
                    }
                });
    }

//    Palette Helper--------------------------------------------------------------------------------
    private void extractPaletteFromBitmap() {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                colors = getColorFromPalette(p);

                labelImage();
            }
        });
    }

    private Set<Integer> getColorFromPalette(Palette p) {
        Set<Integer> colors = new HashSet<>();

        colors.add(p.getVibrantColor(0));
        colors.add(p.getLightVibrantColor(0));
        colors.add(p.getDarkVibrantColor(0));

        colors.add(p.getMutedColor(0));
        colors.add(p.getLightMutedColor(0));
        colors.add(p.getDarkMutedColor(0));

        colors.add(p.getVibrantColor(0));

        colors.remove(0);

        return colors;
    }

//    Label helper----------------------------------------------------------------------------------

    private void labelImage() {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        List<String> strings = new ArrayList<>();
                        for(ImageLabel label: labels){
                            strings.add(label.getText());
                        }
                        listener.onFetched(redirectUrl , colors, strings);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        listener.onError(e.toString());
                    }
                });
    }

    public void editCard(String imageUrl, Context context, OnCompleteListener listener) {
        this.context = context;
        this.redirectUrl = imageUrl;
        this.listener = listener;
        Glide.with(context)
                .asBitmap()
                .onlyRetrieveFromCache(true)
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmap = resource;
                        extractPaletteFromBitmap();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    //    Listener--------------------------------------------------------------------------------------
    interface OnCompleteListener{
        void onFetched(String url, Set<Integer>colors, List<String> labels);
        void onError(String error);
    }
}

