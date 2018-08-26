package dev.danielholmberg.improve.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Objects;

import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.NoteInputValidator;

public class NoteDetailsDialogFragment extends DialogFragment implements View.OnClickListener{
    public static final String TAG = NoteDetailsDialogFragment.class.getSimpleName();

    public static final String NOTE_PARENT_FRAGMENT_KEY = "parentFragment";
    public static final String NOTE_ADAPTER_POS_KEY = "adapterItemPos";
    public static final String NOTE_KEY = "note";

    private Improve app;
    private FirebaseStorageManager storageManager;

    private Context context;
    private View view;

    private Toolbar toolbar;
    private LinearLayout marker;
    private int markerColor;
    private TextView noteDetailTimestamp;

    private Bundle noteBundle;
    private Note note;
    private int parentFragment;

    private boolean editMode = false;

    private View targetView;

    // **** [START] EditMode variables ****

    private View inputLayout;
    private NoteInputValidator validator;

    private EditText inputTitle, inputInfo;
    private String noteId, noteTitle, noteColor, noteInfo, noteTimestamp;
    private int notePosition;
    private AlertDialog colorPickerDialog;

    public static NoteDetailsDialogFragment newInstance() {
        return new NoteDetailsDialogFragment();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();

        noteBundle = getArguments();

        if(noteBundle != null) {
            parentFragment = noteBundle.getInt(NOTE_PARENT_FRAGMENT_KEY);
            note = (Note) noteBundle.getSerializable(NOTE_KEY);
        } else {
            Toast.makeText(context, "Failed to show Note details, please try again",
                    Toast.LENGTH_SHORT).show();
            dismissDialog();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_details, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar_note_activity);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(editMode) {
                    switch (item.getItemId()) {
                        case android.R.id.home:
                            editMode = false;
                            createOptionsMenu();
                            toggleMode(editMode);
                            return true;
                        case R.id.chooseMarkerColor:
                            chooseMarkerColor();
                            return true;
                        case R.id.noteDone:
                            if (validator.formIsValid()) {
                                updateNote();
                            }
                            return true;
                        default:
                            return true;
                    }
                } else {
                    switch (item.getItemId()) {
                        case R.id.noteUnarchive:
                            unarchiveNote();
                            return true;
                        case R.id.noteArchive:
                            showArchiveDialog();
                            return true;
                        case R.id.noteDelete:
                            showDeleteNoteDialog();
                            return true;
                        case R.id.noteEdit:
                            editMode = true;
                            createOptionsMenu();
                            toggleMode(editMode);
                            return true;
                        default:
                            return true;
                    }
                }
            }
        });
        createOptionsMenu();

        inputLayout = (TextInputLayout) view.findViewById(R.id.input_layout);

        noteDetailTimestamp = (TextView) view.findViewById(R.id.footer_note_timestamp_tv);
        inputTitle = (TextInputEditText) view.findViewById(R.id.input_title);
        inputInfo = (TextInputEditText) view.findViewById(R.id.input_info);

        validator = new NoteInputValidator(context, inputLayout);

        toggleMode(editMode);

        marker = (LinearLayout) view.findViewById(R.id.include_marker);
        markerColor = getResources().getColor(R.color.colorPickerDeepOrange);
        ((GradientDrawable) marker.getBackground()).setColor(markerColor);

        if(note != null) {
            populateNoteDetails();
        } else {
            Toast.makeText(context, "Unable to show Note details", Toast.LENGTH_SHORT).show();
            dismissDialog();
        }

        marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editMode)
                    chooseMarkerColor();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getDialog().getWindow())
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void createOptionsMenu() {
        toolbar.getMenu().clear();
        if(editMode) {
            ((TextView) toolbar.findViewById(R.id.toolbar_note_activity_title_tv)).setText(R.string.title_edit_note);
            toolbar.inflateMenu(R.menu.activity_note_mode_edit);
            toolbar.findViewById(R.id.close_dialog_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editMode = false;
                    toggleMode(editMode);
                    populateNoteDetails();
                    createOptionsMenu();
                }
            });
        } else {
            ((TextView) toolbar.findViewById(R.id.toolbar_note_activity_title_tv)).setText(R.string.note_activity_details);
            toolbar.inflateMenu(R.menu.activity_note_mode_show);
            toolbar.findViewById(R.id.close_dialog_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismissDialog();
                }
            });
            if(parentFragment == R.integer.NOTES_FRAGMENT) {
                toolbar.getMenu().findItem(R.id.noteArchive).setVisible(true);
                toolbar.getMenu().findItem(R.id.noteUnarchive).setVisible(false);
            } else if(parentFragment == R.integer.ARCHIVED_NOTES_FRAGMENT){
                toolbar.getMenu().findItem(R.id.noteUnarchive).setVisible(true);
                toolbar.getMenu().findItem(R.id.noteArchive).setVisible(false);
            }
        }
    }

    private void toggleMode(boolean editMode) {
        inputTitle.setEnabled(editMode);
        inputInfo.setEnabled(editMode);

        if(editMode) {
            inputInfo.setVisibility(View.VISIBLE);
            inputTitle.requestFocus();
            ((AppCompatActivity)context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } else {
            ((AppCompatActivity)context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            if(TextUtils.isEmpty(noteInfo)) {
                inputInfo.setVisibility(View.GONE);
            }
        }
    }

    private void populateNoteDetails() {
        noteId = note.getId();
        noteTitle = note.getTitle();
        noteInfo = note.getInfo();
        noteColor = note.getColor();
        noteTimestamp = tranformMillisToDateSring(Long.parseLong(note.getTimestamp()));

        noteDetailTimestamp.setText(noteTimestamp);
        inputTitle.setText(noteTitle);
        inputInfo.setText(noteInfo);

        if (noteColor != null && !noteColor.isEmpty()) {
            GradientDrawable marker_shape = (GradientDrawable) marker.getBackground();
            marker_shape.setColor(Color.parseColor(noteColor));
            markerColor = Color.parseColor(noteColor);
        }

        if(TextUtils.isEmpty(noteInfo)) {
            inputInfo.setVisibility(View.GONE);
        } else {
            inputInfo.setVisibility(View.VISIBLE);
        }
    }

    private String tranformMillisToDateSring(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);

        return DateFormat.getDateTimeInstance().format(calendar.getTime());
    }

    private void showArchiveDialog() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context).setTitle("Archive Note")
                        .setMessage("Do you want to archive this note?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                archiveNote();
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

    private void showDeleteNoteDialog() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context).setTitle("Permanently Delete Note")
                        .setMessage("Do you want to delete this note?")
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

    private void deleteNote(final Note note) {
        storageManager.deleteNote(note, note.getArchived(), new FirebaseStorageCallback() {
            @Override
            public void onSuccess() {
                boolean error = false;

                if(parentFragment == R.integer.ARCHIVED_NOTES_FRAGMENT) {
                    targetView = app.getArchivedNotesFragmentRef().getView().findViewById(R.id.archivednote_fragment_container);
                } else if(parentFragment == R.integer.NOTES_FRAGMENT){
                    targetView = app.getNotesFragmentRef().getView().findViewById(R.id.note_fragment_container);
                } else {
                    error = true;
                }

                if(!error) {
                    Snackbar.make(targetView,
                            "Deleted note: " + note.getTitle(), Snackbar.LENGTH_LONG)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    storageManager.writeNoteToFirebase(note, note.getArchived(), new FirebaseStorageCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d(TAG, "*** Successfully undid 'Delete note' ***");
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            Log.e(TAG, "Failed to undo 'Delete note': " + errorMessage);
                                        }
                                    });
                                }
                            }).show();
                } else {
                    Toast.makeText(context, "Failed to delete note",
                            Toast.LENGTH_SHORT).show();
                    dismissDialog();
                }

            }

            @Override
            public void onFailure(String errorMessage) {
                boolean error = false;

                if(parentFragment == R.integer.ARCHIVED_NOTES_FRAGMENT) {
                    targetView = app.getArchivedNotesFragmentRef().getView().findViewById(R.id.archivednote_fragment_container);
                } else if(parentFragment == R.integer.NOTES_FRAGMENT){
                    targetView = app.getNotesFragmentRef().getView().findViewById(R.id.note_fragment_container);
                } else {
                    error = true;
                }

                if(!error) {
                    Snackbar.make(targetView,
                            "Failed to delete note", Snackbar.LENGTH_LONG)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    deleteNote(note);
                                }
                            }).show();
                } else {
                    dismissDialog();
                }
            }
        });

        dismissDialog();

    }

    private void unarchiveNote() {
        storageManager.writeNoteToFirebase(note, false, new FirebaseStorageCallback() {
            @Override
            public void onSuccess() {
                boolean error = false;
                View targetView = null;

                if (parentFragment == R.integer.ARCHIVED_NOTES_FRAGMENT) {
                    targetView = app.getArchivedNotesFragmentRef().getView().findViewById(R.id.archivednote_fragment_container);
                } else if (parentFragment == R.integer.NOTES_FRAGMENT) {
                    targetView = app.getNotesFragmentRef().getView().findViewById(R.id.note_fragment_container);
                } else {
                    error = true;
                }

                if (!error) {
                    Snackbar.make(targetView, "Unarchived note", Snackbar.LENGTH_SHORT)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    storageManager.writeNoteToFirebase(note, true, new FirebaseStorageCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d(TAG, "*** Successfully undid 'Move note from archive' ***");
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            note.setArchived(false);
                                            Log.e(TAG, "Failed to undo 'Move note from archive': " + errorMessage);
                                        }
                                    });
                                }
                            }).show();
                }

            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(context, "Failed to unarchive note", Toast.LENGTH_LONG).show();
            }
        });

        dismissDialog();

    }

    private void archiveNote() {
        storageManager.writeNoteToFirebase(note, true, new FirebaseStorageCallback() {
            @Override
            public void onSuccess() {
                boolean error = false;
                View targetView = null;

                if(parentFragment == R.integer.ARCHIVED_NOTES_FRAGMENT) {
                    targetView = app.getArchivedNotesFragmentRef().getView().findViewById(R.id.archivednote_fragment_container);
                } else if(parentFragment == R.integer.NOTES_FRAGMENT){
                    targetView = app.getNotesFragmentRef().getView().findViewById(R.id.note_fragment_container);
                } else {
                    error = true;
                }

                if(!error) {
                    Snackbar.make(targetView, "Archived note", Snackbar.LENGTH_SHORT)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    note.setArchived(false);

                                    storageManager.writeNoteToFirebase(note, false, new FirebaseStorageCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d(TAG, "*** Successfully undid 'Move note to archive' ***");
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            note.setArchived(true);

                                            Log.e(TAG, "Failed to undo 'Move note to archive': " + errorMessage);
                                        }
                                    });
                                }
                            }).show();
                }

            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(context, "Failed to archive note", Toast.LENGTH_LONG).show();
            }
        });

        dismissDialog();

    }

    public void updateNote(){
        String id = noteId;
        boolean archived = note.getArchived();
        String newTitle = inputTitle.getText().toString();
        String newInfo = inputInfo.getText().toString();
        String newColor = "#" + Integer.toHexString(markerColor);
        String updatedTimestamp = Long.toString(System.currentTimeMillis());

        if(TextUtils.isEmpty(newInfo.trim())) {
            newInfo = "";
        }

        Note updatedNote = new Note(id, newTitle, newInfo, newColor, updatedTimestamp);
        updatedNote.setArchived(archived);

        storageManager.writeNoteToFirebase(updatedNote, updatedNote.getArchived(), new FirebaseStorageCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(app, "Note successfully updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(app, "Failed to update note, please try again", Toast.LENGTH_SHORT).show();
            }
        });

        dismissDialog();

    }

    private void dismissDialog() {
        this.dismiss();

        // TODO - Finish dialog trough SharedElementTransition...
    }

    private void chooseMarkerColor() {
        LinearLayout colorPickerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.color_picker, null, false);

        // First row
        colorPickerLayout.findViewById(R.id.buttonColorGreen).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorLightGreen).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorAmber).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorDeepOrange).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorBrown).setOnClickListener(this);

        // Second row
        colorPickerLayout.findViewById(R.id.buttonColorBlueGrey).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorTurquoise).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorPink).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorDeepPurple).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorDarkGrey).setOnClickListener(this);

        // Third row
        colorPickerLayout.findViewById(R.id.buttonColorRed).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorPurple).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorBlue).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorDarkOrange).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorBabyBlue).setOnClickListener(this);

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context).setTitle("Marker color")
                        .setMessage("Assign a specific color to your Note")
                        .setCancelable(true)
                        .setView(colorPickerLayout);
        colorPickerDialog = alertDialogBuilder.create();
        colorPickerDialog.show();
    }

    @Override
    public void onClick(View view) {
        GradientDrawable marker_shape = (GradientDrawable) marker.getBackground();
        switch (view.getId()) {
            case R.id.buttonColorGreen:
                markerColor = getResources().getColor(R.color.colorPickerGreen);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorLightGreen:
                markerColor = getResources().getColor(R.color.colorPickerLightGreen);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorAmber:
                markerColor = getResources().getColor(R.color.colorPickerAmber);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorDeepOrange:
                markerColor = getResources().getColor(R.color.colorPickerDeepOrange);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorBrown:
                markerColor = getResources().getColor(R.color.colorPickerBrown);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorBlueGrey:
                markerColor = getResources().getColor(R.color.colorPickerBlueGrey);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorTurquoise:
                markerColor = getResources().getColor(R.color.colorPickerTurquoise);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorPink:
                markerColor = getResources().getColor(R.color.colorPickerPink);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorDeepPurple:
                markerColor = getResources().getColor(R.color.colorPickerDeepPurple);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorDarkGrey:
                markerColor = getResources().getColor(R.color.colorPickerDarkGrey);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorRed:
                markerColor = getResources().getColor(R.color.colorPickerRed);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorPurple:
                markerColor = getResources().getColor(R.color.colorPickerPurple);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorBlue:
                markerColor = getResources().getColor(R.color.colorPickerBlue);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorDarkOrange:
                markerColor = getResources().getColor(R.color.colorPickerDarkOrange);
                marker_shape.setColor(markerColor);
                break;
            case R.id.buttonColorBabyBlue:
                markerColor = getResources().getColor(R.color.colorPickerBabyBlue);
                marker_shape.setColor(markerColor);
                break;
            default:
                markerColor = getResources().getColor(R.color.colorPickerDeepOrange);
                marker_shape.setColor(markerColor);
                break;
        }
        colorPickerDialog.dismiss();
    }
}
