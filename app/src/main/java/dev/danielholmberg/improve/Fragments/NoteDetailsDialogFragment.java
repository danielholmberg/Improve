package dev.danielholmberg.improve.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileWriter;
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
    private static final String EXPORTED_FILES_DIRECTORY_PATH = "exported_notes";

    private Improve app;
    private FirebaseStorageManager storageManager;

    private Context context;
    private View view;

    private Toolbar toolbar;
    private LinearLayout boarderMarker;
    private int markerColor;

    private Bundle noteBundle;
    private Note note;
    private int parentFragment;

    private ProgressDialog exportDialog;

    private boolean editMode = false;

    private View targetView;

    // **** [START] EditMode variables ****

    private View inputLayout;
    private NoteInputValidator validator;

    private EditText inputTitle, inputInfo;
    private String noteId, noteTitle, noteColor, noteInfo, noteTimestampAdded, noteTimestampUpdated;
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

        toolbar = (Toolbar) view.findViewById(R.id.toolbar_note_details_fragment);
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
                        case R.id.noteInfo:
                            showInfoDialog();
                            return true;
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
                        case R.id.noteExport:
                            showExportProgressDialog();
                            exportNoteToFile(note);
                            return true;
                        default:
                            return true;
                    }
                }
            }
        });
        createOptionsMenu();

        inputLayout = (TextInputLayout) view.findViewById(R.id.input_layout);

        inputTitle = (TextInputEditText) view.findViewById(R.id.input_title);
        inputInfo = (TextInputEditText) view.findViewById(R.id.input_info);

        validator = new NoteInputValidator(context, inputLayout);

        toggleMode(editMode);

        boarderMarker = (LinearLayout) view.findViewById(R.id.note_details_layout);
        markerColor = getResources().getColor(R.color.colorPickerDeepOrange);
        boarderMarker.setBackgroundColor(markerColor);

        if(note != null) {
            populateNoteDetails();
        } else {
            Toast.makeText(context, "Unable to show Note details", Toast.LENGTH_SHORT).show();
            dismissDialog();
        }
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
            toolbar.inflateMenu(R.menu.fragment_note_details_edit);
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
            toolbar.inflateMenu(R.menu.fragment_note_details_show);
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
        noteTimestampAdded = tranformMillisToDateSring(Long.parseLong(note.getTimestampAdded()));
        noteTimestampUpdated = tranformMillisToDateSring(Long.parseLong(note.getTimestampUpdated()));

        inputTitle.setText(noteTitle);
        inputInfo.setText(noteInfo);

        if (noteColor != null && !noteColor.isEmpty()) {
            boarderMarker.setBackgroundColor(Color.parseColor(noteColor));
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

    private void showInfoDialog() {
        LinearLayout noteInfoLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_note_info, null);
        TextView noteAddedTimestamp = noteInfoLayout.findViewById(R.id.note_info_added_timestamp_tv);
        TextView noteUpdatedTimestamp = noteInfoLayout.findViewById(R.id.note_info_updated_timestamp_tv);

        String added = "Added: " + noteTimestampAdded;
        String updated = "Last updated: " + noteTimestampUpdated;

        noteAddedTimestamp.setText(added);
        noteUpdatedTimestamp.setText(updated);

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context).setTitle(R.string.dialog_info_note_title)
                        .setIcon(R.drawable.ic_menu_info_primary)
                        .setView(noteInfoLayout)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    private void exportNoteToFile(Note note) {
        try{
            File root = new File(context.getFilesDir(), EXPORTED_FILES_DIRECTORY_PATH);
            if(!root.exists()) {
                root.mkdirs();
            }

            File noteFile = new File(root, note.getId() + ".txt");
            FileWriter writer = new FileWriter(noteFile);
            writer.append(note.getExportJSONFormat());
            writer.flush();
            writer.close();

            Toast.makeText(context, "Exported Note to " + noteFile.getPath(), Toast.LENGTH_SHORT).show();
            exportDialog.dismiss();

        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(context, "Export failed, please try again", Toast.LENGTH_SHORT).show();
            exportDialog.dismiss();
        }
    }

    private void showExportProgressDialog() {
        exportDialog = ProgressDialog.show(context, "Exporting Note to Text-file",
                "Working. Please wait...", true);
    }

    private void showArchiveDialog() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context).setTitle(R.string.dialog_archive_note_title)
                        .setMessage(R.string.dialog_archive_note_msg)
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
                new AlertDialog.Builder(context).setTitle(R.string.dialog_delete_note_title)
                        .setMessage(R.string.dialog_delete_note_msg)
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
        String timestampAdded = note.getTimestampAdded();
        String timestampUpdated = Long.toString(System.currentTimeMillis());

        if(TextUtils.isEmpty(newInfo.trim())) {
            newInfo = "";
        }

        Note updatedNote = new Note(id, newTitle, newInfo, newColor, timestampAdded);
        updatedNote.setArchived(archived);
        updatedNote.setTimestampUpdated(timestampUpdated);

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

        editMode = false;
        createOptionsMenu();
        toggleMode(editMode);
    }

    private void dismissDialog() {
        this.dismiss();
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
                new AlertDialog.Builder(context).setTitle(R.string.choose_marker_color_note_title)
                        .setMessage(R.string.choose_marker_color_note_msg)
                        .setCancelable(true)
                        .setView(colorPickerLayout)
                        .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        colorPickerDialog = alertDialogBuilder.create();
        colorPickerDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonColorGreen:
                markerColor = getResources().getColor(R.color.colorPickerGreen);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorLightGreen:
                markerColor = getResources().getColor(R.color.colorPickerLightGreen);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorAmber:
                markerColor = getResources().getColor(R.color.colorPickerAmber);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorDeepOrange:
                markerColor = getResources().getColor(R.color.colorPickerDeepOrange);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorBrown:
                markerColor = getResources().getColor(R.color.colorPickerBrown);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorBlueGrey:
                markerColor = getResources().getColor(R.color.colorPickerBlueGrey);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorTurquoise:
                markerColor = getResources().getColor(R.color.colorPickerTurquoise);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorPink:
                markerColor = getResources().getColor(R.color.colorPickerPink);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorDeepPurple:
                markerColor = getResources().getColor(R.color.colorPickerDeepPurple);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorDarkGrey:
                markerColor = getResources().getColor(R.color.colorPickerDarkGrey);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorRed:
                markerColor = getResources().getColor(R.color.colorPickerRed);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorPurple:
                markerColor = getResources().getColor(R.color.colorPickerPurple);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorBlue:
                markerColor = getResources().getColor(R.color.colorPickerBlue);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorDarkOrange:
                markerColor = getResources().getColor(R.color.colorPickerDarkOrange);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            case R.id.buttonColorBabyBlue:
                markerColor = getResources().getColor(R.color.colorPickerBabyBlue);
                boarderMarker.setBackgroundColor(markerColor);
                break;
            default:
                markerColor = getResources().getColor(R.color.colorPickerDeepOrange);
                boarderMarker.setBackgroundColor(markerColor);
                break;
        }
        colorPickerDialog.dismiss();
    }
}
