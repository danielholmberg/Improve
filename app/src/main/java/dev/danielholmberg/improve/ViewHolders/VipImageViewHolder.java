package dev.danielholmberg.improve.ViewHolders;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;

import dev.danielholmberg.improve.Adapters.VipImagesAdapter;
import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.Models.VipImage;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.CircleTransform;

public class VipImageViewHolder extends RecyclerView.ViewHolder{
    private final static String TAG = VipImageViewHolder.class.getSimpleName();

    private Context context;
    private String noteId;
    private View itemView;
    private VipImage vipImage;
    private VipImagesAdapter vipImagesAdapter;
    private File noteImagesDir;

    public VipImageViewHolder(Context context, View itemView, String noteId, VipImagesAdapter vipImagesAdapter) {
        super(itemView);
        this.context = context;
        this.itemView = itemView;
        this.noteId = noteId;
        this.vipImagesAdapter = vipImagesAdapter;

        noteImagesDir = new File(Improve.getInstance().getImageDir(), noteId);
        if(!noteImagesDir.exists()) noteImagesDir.mkdirs();
    }

    /**
     * Binds data from VipImage (Model) object to related View.
     *
     * OBS! Because the RecyclerView reuses old ViewHolders in the list, we therefore
     * need to define ALL Views of each VipImage to make them display specific information
     * for targeted VipImage!
     *
     * @param vipImage - Target VipImage (Model)
     */
    public void bindModelToPreviewView(final VipImage vipImage) {
        if(vipImage == null) return;

        this.vipImage = vipImage;

        File image = new File(noteImagesDir, vipImage.getId() + FirebaseStorageManager.VIP_IMAGE_SUFFIX);

        int targetSize = (int) Improve.getInstance().getResources().getDimension(R.dimen.vip_image_view_size);

        ImageView vipImageView = (ImageView) this.itemView.findViewById(R.id.vip_image_view);

        if(image.exists()) {
            Log.d(TAG, "Loading Preview image from Local Filesystem at path: " + image.getPath());

            Picasso.get()
                    .load(image)
                    .centerCrop()
                    .resize(targetSize, targetSize)
                    .into(vipImageView);

            //vipImagePlaceholder.setVisibility(View.GONE);
            vipImageView.setVisibility(View.VISIBLE);

        } else if(vipImage.getOriginalFilePath() != null) {
            Log.d(TAG, "Loading Preview image from Device Filesystem at path: " + vipImage.getOriginalFilePath());

            Picasso.get()
                    .load(Uri.parse(vipImage.getOriginalFilePath()))
                    .centerCrop()
                    .resize(targetSize, targetSize)
                    .into(vipImageView);

            //vipImagePlaceholder.setVisibility(View.GONE);
            vipImageView.setVisibility(View.VISIBLE);

        } else {
            Log.d(TAG, "Downloading Preview image from Firebase for Note: " + noteId + " with image id: " + vipImage.getId());

            Improve.getInstance().getFirebaseStorageManager()
                    .downloadImageToLocalFile(noteId, vipImage.getId(), new FirebaseStorageCallback() {
                @Override
                public void onSuccess(Object file) {
                    Picasso.get()
                            .load((File) file)
                            .centerCrop()
                            .resize(targetSize, targetSize)
                            .into(vipImageView);

                    //vipImagePlaceholder.setVisibility(View.GONE);
                    vipImageView.setVisibility(View.VISIBLE);
                    vipImagesAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(String errorMessage) {}

                @Override
                public void onProgress(int progress) {}
            });
        }

        vipImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageFullscreen();
            }
        });
    }

    private void showImageFullscreen() {
        LinearLayout vipImageViewFullscreenLayout = (LinearLayout) LayoutInflater.from(context)
                .inflate(R.layout.dialog_vip_image_fullscreen, null);
        ImageView vipImageViewFull = (ImageView) vipImageViewFullscreenLayout.findViewById(R.id.vip_image_view_full);

        File image = new File(noteImagesDir, vipImage.getId() + FirebaseStorageManager.VIP_IMAGE_SUFFIX);

        if(image.exists()) {
            Log.d(TAG, "Loading Fullscreen image from Local Filesystem at path: " + image.getPath());
            Picasso.get()
                    .load(image)
                    .into(vipImageViewFull);
        } else if(vipImage.getOriginalFilePath() != null) {
            Log.d(TAG, "Loading Fullscreen image from Local Filesystem at path: " + vipImage.getOriginalFilePath());
            Picasso.get()
                    .load(Uri.parse(vipImage.getOriginalFilePath()))
                    .into(vipImageViewFull);
        } else {
            Log.d(TAG, "Loading Fullscreen image from Firebase with id: " + vipImage.getId());
            Improve.getInstance().getFirebaseStorageManager()
                    .downloadImageToLocalFile(noteId, vipImage.getId(), new FirebaseStorageCallback() {
                @Override
                public void onSuccess(Object file) {
                    Picasso.get()
                            .load((Uri) file)
                            .into(vipImageViewFull);
                }

                @Override
                public void onFailure(String errorMessage) {}

                @Override
                public void onProgress(int progress) {}
            });
        }

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context, R.style.CustomFullscreenDialogStyle)
                        .setView(vipImageViewFullscreenLayout)
                        .setCancelable(true);
        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public void bindModelToThumbnailView(VipImage vipImage) {
        if(vipImage == null) return;

        this.vipImage = vipImage;

        ImageView vipImageView = itemView.findViewById(R.id.vip_image_view_thumbnail);
        ProgressBar vipImagePlaceholder = itemView.findViewById(R.id.vip_image_progressBar);

        vipImagePlaceholder.setVisibility(View.VISIBLE);
        vipImageView.setVisibility(View.GONE);

        File image = new File(noteImagesDir, vipImage.getId() + FirebaseStorageManager.VIP_IMAGE_SUFFIX);

        int thumbnailSize = (int) Improve.getInstance().getResources().getDimension(R.dimen.vip_image_view_thumbnail_size);

        // If an image has previously been downloaded to local storage
        if(image.exists()) {
            Log.d(TAG, "Loading image from Local Filesystem with file at path: " + image.getPath());

            Picasso.get()
                    .load(image)
                    .centerCrop()
                    .transform(new CircleTransform())
                    .resize(thumbnailSize, thumbnailSize)
                    .into(vipImageView);

            vipImagePlaceholder.setVisibility(View.GONE);
            vipImageView.setVisibility(View.VISIBLE);

        } else {
            // Download image from Firebase to a local file
            Log.d(TAG, "Loading image from Firebase with image id: " + vipImage.getId());

            Improve.getInstance().getFirebaseStorageManager()
                    .downloadImageToLocalFile(noteId, vipImage.getId(), new FirebaseStorageCallback() {
                @Override
                public void onSuccess(Object file) {
                    Picasso.get()
                            .load((File) file)
                            .centerCrop()
                            .transform(new CircleTransform())
                            .resize(thumbnailSize, thumbnailSize)
                            .into(vipImageView);

                    vipImagePlaceholder.setVisibility(View.GONE);
                    vipImageView.setVisibility(View.VISIBLE);
                    vipImagesAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(String errorMessage) {}

                @Override
                public void onProgress(int progress) {}
            });
        }
    }

    public void setEditMode(boolean editMode) {
        ImageButton vipImageClearBtn = (ImageButton) this.itemView.findViewById(R.id.vip_image_clear_btn);

        if(vipImageClearBtn != null) {
            if(editMode) {
                vipImageClearBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        vipImagesAdapter.remove(vipImage);
                    }
                });
                vipImageClearBtn.setVisibility(View.VISIBLE);
            } else {
                vipImageClearBtn.setVisibility(View.GONE);
            }
        }
    }
}