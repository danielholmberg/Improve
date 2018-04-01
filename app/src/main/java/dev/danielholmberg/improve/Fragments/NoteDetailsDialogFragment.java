package dev.danielholmberg.improve.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import dev.danielholmberg.improve.Activities.AddNoteActivity;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;

/**
 * Created by Daniel Holmberg.
 */

public class NoteDetailsDialogFragment extends DialogFragment implements View.OnClickListener{
    private static final String TAG = NoteDetailsDialogFragment.class.getSimpleName();

    private Improve app;
    private DialogFragment detailsDialog;
    private FirebaseStorageManager storageManager;

    private View view;
    private View parentLayout;

    private Note note;
    private Bundle noteBundle;
    private int position;
    private boolean isDone = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();

        detailsDialog = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_note_details, container, false);
        parentLayout = (View) container;

        RelativeLayout toolbar = (RelativeLayout) view.findViewById(R.id.note_details_toolbar);
        View layout = (View) view.findViewById(R.id.note_details_container);

        Button close = (Button) view.findViewById(R.id.close_note_btn);
        close.setOnClickListener(this);
        Button update = (Button) view.findViewById(R.id.update_note_btn);
        update.setOnClickListener(this);
        Button done = (Button) view.findViewById(R.id.done_note_btn);
        done.setOnClickListener(this);

        TextView title = (TextView) view.findViewById(R.id.note_details_title_tv);
        TextView info = (TextView) view.findViewById(R.id.note_details_info_tv);
        TextView timestamp = (TextView) view.findViewById(R.id.footer_note_timestamp_tv);

        noteBundle =  this.getArguments();

        if(noteBundle != null){
            note = (Note) noteBundle.getSerializable("note");
            position = noteBundle.getInt("position");
            isDone = noteBundle.getBoolean("isDone");
        }

        if(note != null) {
            toolbar.setBackgroundColor(Color.parseColor(note.getColor()));
            title.setText(note.getTitle());
            info.setText(note.getInfo());
            timestamp.setText(note.getTimestamp());

            if(isDone) {
                // Change the Note Detail layout
                update.setBackground(getResources().getDrawable(R.drawable.ic_menu_delete_white));
                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Delete Note
                        AlertDialog.Builder alertDialogBuilder =
                                new AlertDialog.Builder(getContext()).setTitle("Delete Note")
                                        .setMessage("Do you really want to delete: " + note.getTitle())
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                deleteNote(note);
                                            }
                                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        final AlertDialog dialog = alertDialogBuilder.create();
                        dialog.show();
                    }
                });
                done.setBackground(getResources().getDrawable(R.drawable.ic_menu_undo_white));
                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        note.setIsDone(false);
                        storageManager.writeNoteToFirebase(note);
                        detailsDialog.dismiss();
                    }
                });
            }
        } else {
            // Dismiss dialog and show Toast.
            this.dismiss();
            Toast.makeText(getActivity(), "Unable to show Note details", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void deleteNote(Note note) {
        storageManager.deleteNote(note);
        detailsDialog.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.close_note_btn:
                detailsDialog.dismiss();
                break;
            case R.id.update_note_btn:
                detailsDialog.dismiss();
                Intent updateNote = new Intent(getContext(), AddNoteActivity.class);
                updateNote.putExtra("noteBundle", noteBundle);
                startActivity(updateNote);
                break;
            case R.id.done_note_btn:
                note.setIsDone(true);
                storageManager.writeNoteToFirebase(note);
                detailsDialog.dismiss();
                break;
            default:
                break;
        }
    }
}
