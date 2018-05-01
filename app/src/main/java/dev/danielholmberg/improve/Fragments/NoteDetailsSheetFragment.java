package dev.danielholmberg.improve.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import dev.danielholmberg.improve.Activities.AddNoteActivity;
import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;

/**
 * Created by Daniel Holmberg.
 */

public class NoteDetailsSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener{
    private static final String TAG = NoteDetailsSheetFragment.class.getSimpleName();

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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_note_details, container, false);
        parentLayout = getActivity().findViewById(R.id.main_fragment_container);

        RelativeLayout toolbar = (RelativeLayout) view.findViewById(R.id.note_details_toolbar);
        View layout = (View) view.findViewById(R.id.note_details_container);

        ImageButton edit = (ImageButton) view.findViewById(R.id.edit_note_btn);
        edit.setOnClickListener(this);
        ImageButton archive = (ImageButton) view.findViewById(R.id.archive_note_btn);
        archive.setOnClickListener(this);

        TextView title = (TextView) view.findViewById(R.id.note_details_title_tv);
        TextView info = (TextView) view.findViewById(R.id.note_details_info_tv);
        TextView timestamp = (TextView) view.findViewById(R.id.footer_note_timestamp_tv);

        // Making Text View scrollable when taking up max height.
        info.setMovementMethod(new ScrollingMovementMethod());

        noteBundle =  this.getArguments();

        if(noteBundle != null){
            note = (Note) noteBundle.getSerializable("note");
            position = noteBundle.getInt("position");
            isDone = note.getIsDone();
        }

        if(note != null) {
            int noteColor = getResources().getColor(R.color.colorPickerDeepOrange);
            if(note.getColor() != null) {
                noteColor = Color.parseColor(note.getColor());
            }
            toolbar.setBackgroundColor(noteColor);
            title.setText(note.getTitle());
            info.setText(note.getInfo());
            timestamp.setText(note.getTimestamp());

            if(isDone) {
                // Change the Note Detail layout
                edit.setImageResource(R.drawable.ic_menu_delete_white);
                edit.setOnClickListener(new View.OnClickListener() {
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
                archive.setImageResource(R.drawable.ic_menu_unarchive_white);
                archive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        note.setIsDone(false);
                        storageManager.writeNoteToFirebase(note, false, new FirebaseStorageCallback() {
                            @Override
                            public void onSuccess() {
                                Snackbar.make(parentLayout, "Moved note from archive", Snackbar.LENGTH_SHORT)
                                        .setAction("UNDO", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                note.setIsDone(true);
                                                storageManager.writeNoteToFirebase(note, true, new FirebaseStorageCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        Log.d(TAG, "*** Successfully undid 'Move note from archive' ***");
                                                    }

                                                    @Override
                                                    public void onFailure(String errorMessage) {
                                                        Log.e(TAG, "Failed to undo 'Move note from archive': "+ errorMessage);
                                                    }
                                                });
                                            }
                                        }).show();
                            }

                            @Override
                            public void onFailure(String errorMessage) {

                            }
                        });
                        detailsDialog.dismiss();
                    }
                });
            } else if(info.getText().toString().trim().isEmpty()){
                info.setVisibility(View.GONE);
            }
        } else {
            // Dismiss dialog and show Toast.
            this.dismiss();
            Toast.makeText(getActivity(), "Unable to show Note details", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void deleteNote(final Note note) {
        storageManager.deleteNote(note, true, new FirebaseStorageCallback() {
            @Override
            public void onSuccess() {
                Snackbar.make(parentLayout, "Deleted note", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                storageManager.writeNoteToFirebase(note, true, new FirebaseStorageCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "*** Successfully undid 'Delete note' ***");
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Log.e(TAG, "Failed to undo 'Delete note': "+ errorMessage);
                                    }
                                });
                            }
                        }).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Snackbar.make(parentLayout, "Failed to delete note", Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                deleteNote(note);
                            }
                        }).show();
            }
        });
        detailsDialog.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit_note_btn:
                detailsDialog.dismiss();
                Intent updateNote = new Intent(getContext(), AddNoteActivity.class);
                updateNote.putExtra("noteBundle", noteBundle);
                startActivity(updateNote);
                break;
            case R.id.archive_note_btn:
                note.setIsDone(true);
                storageManager.writeNoteToFirebase(note, true, new FirebaseStorageCallback() {
                    @Override
                    public void onSuccess() {
                        Snackbar.make(parentLayout, "Archived note", Snackbar.LENGTH_SHORT)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        note.setIsDone(false);
                                        storageManager.writeNoteToFirebase(note, false, new FirebaseStorageCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d(TAG, "*** Successfully undid 'Archived note' ***");
                                            }

                                            @Override
                                            public void onFailure(String errorMessage) {
                                                Log.e(TAG, "Failed to undo 'Archived note': "+ errorMessage);
                                            }
                                        });
                                    }
                                }).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {

                    }
                });
                detailsDialog.dismiss();
                break;
            default:
                break;
        }
    }
}
