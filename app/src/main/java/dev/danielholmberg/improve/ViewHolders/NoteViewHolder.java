package dev.danielholmberg.improve.ViewHolders;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;

import dev.danielholmberg.improve.Models.Note;
import dev.danielholmberg.improve.Fragments.NoteDetailsDialogFragment;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Models.Tag;
import dev.danielholmberg.improve.R;

public class NoteViewHolder extends RecyclerView.ViewHolder {

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
