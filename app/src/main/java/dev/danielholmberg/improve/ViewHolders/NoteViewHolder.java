package dev.danielholmberg.improve.ViewHolders;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Models.Note;
import dev.danielholmberg.improve.Fragments.NoteDetailsDialogFragment;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Models.Tag;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.CircleTransform;

public class NoteViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = NoteViewHolder.class.getSimpleName();

    private Context context;
    private View itemView;
    private ViewGroup parent;
    private Note note;
    private TextView title, info;
    private RelativeLayout footer;

    public NoteViewHolder(Context context, View itemView, ViewGroup parent) {
        super(itemView);
        this.context = context;
        this.itemView = itemView;
        this.parent = parent;
    }

    /**
     * Binds data from Note (Model) object to related View.
     *
     * OBS! Because the RecyclerView reuses old ViewHolders in the list, we therefore
     * need to define ALL Views of each Note to make them display specific information
     * for targeted Note!
     *
     * @param note - Target Note (Model)
     */
    public void bindModelToView(final Note note) {
        if(note == null) return;

        this.note = note;

        title = (TextView) itemView.findViewById(R.id.item_note_title_tv);
        info = (TextView) itemView.findViewById(R.id.item_note_info_tv);
        footer = (RelativeLayout) itemView.findViewById(R.id.footer_note);
        RelativeLayout vipImageViewContainer = (RelativeLayout) itemView.findViewById(R.id.vip_image_view_container);
        ProgressBar vipImagePlaceholder = (ProgressBar) itemView.findViewById(R.id.vip_image_progressBar);
        ImageView vipImageView = itemView.findViewById(R.id.vip_image_view_thumbnail);
        FlexboxLayout tagsList = (FlexboxLayout) itemView.findViewById(R.id.footer_note_tags_list);

        // Title
        if(note.getTitle() != null) title.setText(note.getTitle());

        // Information
        if(note.getInfo() != null && !TextUtils.isEmpty(note.getInfo())) {
            info.setText(note.getInfo());
            info.setVisibility(View.VISIBLE);
        } else {
            info.setVisibility(View.GONE);
        }

        // Tags
        if(!note.getTags().isEmpty()) {
            footer.setVisibility(View.VISIBLE);
        } else {
            footer.setVisibility(View.GONE);
        }

        // Reset Tag list view in Footer.
        tagsList.removeAllViews();

        // Populate Tag list view in Footer.
        for(String tagId: note.getTags().keySet()) {
            View tagView = LayoutInflater.from(this.context).inflate(R.layout.item_tag, this.parent, false);

            // Create a Tag View and add it to the Tag list view.
            TagViewHolder tagViewHolder = new TagViewHolder(tagView);
            Tag tag = Improve.getInstance().getTagsAdapter().getTag(tagId);
            tagViewHolder.bindModelToView(tag);
            tagsList.addView(tagView);

        }

        // Stared
        if(note.isStared()) {
            itemView.setBackground(Improve.getInstance().getDrawable(R.drawable.background_note_stared));
        } else {
            itemView.setBackground(Improve.getInstance().getDrawable(R.drawable.background_note));
        }

        // Note has VIP image or not
        if(note.hasImage()) {
            vipImagePlaceholder.setVisibility(View.VISIBLE);
            vipImageViewContainer.setVisibility(View.VISIBLE);

            // Retrieve image from local storage
            File cachedImage = new File(Improve.getInstance().getImageDir().getPath() +
                    File.separator + note.getImageId());

            int thumbnailSize = (int) Improve.getInstance().getResources().getDimension(R.dimen.vip_image_view_thumbnail_size);

            // If an image has previously been downloaded to local storage
            if(cachedImage.exists()) {
                Log.d(TAG, "Loading image from Local Filesystem with file at path: " + cachedImage.getPath());

                Picasso.get()
                        .load(cachedImage)
                        .centerCrop()
                        .transform(new CircleTransform())
                        .resize(thumbnailSize, thumbnailSize)
                        .into(vipImageView);

                vipImagePlaceholder.setVisibility(View.GONE);
                vipImageView.setVisibility(View.VISIBLE);

            } else {
                // Download image from Firebase to a local file
                Log.d(TAG, "Loading image from Firebase with image id: " + note.getImageId());

                Improve.getInstance().getFirebaseStorageManager().downloadImageToLocalFile(note.getImageId(), new FirebaseStorageCallback() {
                    @Override
                    public void onSuccess(File file) {
                        Picasso.get()
                                .load(file)
                                .centerCrop()
                                .transform(new CircleTransform())
                                .resize(thumbnailSize, thumbnailSize)
                                .into(vipImageView);

                        vipImagePlaceholder.setVisibility(View.GONE);
                        vipImageView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(String errorMessage) {}

                    @Override
                    public void onProgress(int progress) {}
                });
            }

        } else {
            // Remove VIP ImageView from layout
            vipImagePlaceholder.setVisibility(View.GONE);
            vipImageView.setVisibility(View.GONE);
            vipImageViewContainer.setVisibility(View.GONE);
        }

        // Set OnClickListener to display a DialogFragment to show all the details.
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoteDetailDialog();
            }
        });
    }

    /**
     * Triggers a DialogFragment to show detailed content of the target Note.
     */
    private void showNoteDetailDialog() {
        FragmentManager fm = Improve.getInstance().getNotesFragmentRef().getFragmentManager();
        NoteDetailsDialogFragment noteDetailsDialogFragment = NoteDetailsDialogFragment.newInstance();
        noteDetailsDialogFragment.setArguments(createBundle(note, getAdapterPosition()));

        noteDetailsDialogFragment.show(fm, NoteDetailsDialogFragment.TAG);
    }

    /**
     * Creates a Bunde object to be passed to the Note details DialogFragment.
     * @param note - Target Note
     * @param itemPos - Position of targeted Note in the SortedList
     * @return Bundle with all important data to display details of targeted Note.
     */
    private Bundle createBundle(Note note, int itemPos) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(NoteDetailsDialogFragment.NOTE_KEY, note);
        bundle.putInt(NoteDetailsDialogFragment.NOTE_PARENT_FRAGMENT_KEY, R.integer.NOTES_FRAGMENT);
        bundle.putInt(NoteDetailsDialogFragment.NOTE_ADAPTER_POS_KEY, itemPos);
        return bundle;
    }
}
