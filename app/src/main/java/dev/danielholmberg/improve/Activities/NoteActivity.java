package dev.danielholmberg.improve.Activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.text.DateFormat;
import java.util.Date;

import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.NoteInputValidator;

/**
 *  Created by DanielHolmberg
 *
 *  Activity with 2 modes:
 *      1. Show note details
 *          1.1 Active note
 *          1.2 Completed note
 *      2. Edit note details
 */

public class NoteActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = AddNoteActivity.class.getSimpleName();

    private Improve app;
    private FirebaseStorageManager storageManager;

    private Toolbar toolbar;
    private LinearLayout marker;
    private int markerColor;
    private TextView noteDetailTitle, noteDetailInfo, noteDetailTimestamp;

    private Bundle noteBundle;
    private Note note;
    private int parentFragment;

    private boolean editMode = false;
    private Menu menu;

    private View targetView;

    // **** [START] EditMode variables ****

    private View inputLayout;
    private NoteInputValidator validator;

    private ViewSwitcher titleViewSwitcher;
    private ViewSwitcher infoViewSwitcher;
    private EditText inputTitle, inputInfo;
    private String noteId, noteTitle, noteColor, noteInfo, noteTimestamp;
    private int notePosition;
    private AlertDialog colorPickerDialog;

    // **** [END] EditMode variables ****

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();

        toolbar = (Toolbar) findViewById(R.id.toolbar_note_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noteBundle = getIntent().getBundleExtra("noteBundle");

        if(noteBundle != null) {
            parentFragment = noteBundle.getInt("parentFragment");
            note = (Note) noteBundle.getSerializable("note");
        } else {
            Toast.makeText(getApplicationContext(), "Failed to show Note details, please try again",
                    Toast.LENGTH_SHORT).show();
            showParentActivity();
        }

        noteDetailTitle = (TextView) findViewById(R.id.note_activity_title_tv);
        noteDetailInfo = (TextView) findViewById(R.id.note_activity_info_tv);
        noteDetailTimestamp = (TextView) findViewById(R.id.footer_note_timestamp_tv);
        marker = (LinearLayout) findViewById(R.id.include_marker);
        markerColor = getResources().getColor(R.color.colorPickerDeepOrange);

        setUpViewSwitcher();

        if(note != null) {
            populateShowMode();
        } else {
            Toast.makeText(this, "Unable to show Note details", Toast.LENGTH_SHORT).show();
            showParentActivity();
        }

    }

    private void setUpViewSwitcher() {
        titleViewSwitcher = findViewById(R.id.view_switcher_title);
        infoViewSwitcher = findViewById(R.id.view_switcher_info);

        Animation titleIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation titleOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        titleViewSwitcher.setInAnimation(titleIn);
        titleViewSwitcher.setOutAnimation(titleOut);

        Animation infoIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation infoOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        infoViewSwitcher.setInAnimation(infoIn);
        infoViewSwitcher.setOutAnimation(infoOut);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        menu.clear();

        if(editMode) {
            ((TextView) findViewById(R.id.toolbar_note_activity_title_tv)).setText(R.string.title_edit_note);
            inflater.inflate(R.menu.activity_note_mode_edit, menu);
        } else {
            ((TextView) findViewById(R.id.toolbar_note_activity_title_tv)).setText(R.string.note_activity_details);
            inflater.inflate(R.menu.activity_note_mode_show, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // Change which Archive/Unarchive menu item that is shown
        // depending on parent fragment.
        if(parentFragment == R.integer.NOTES_FRAGMENT) {
            menu.findItem(R.id.noteArchive).setVisible(true);
            menu.findItem(R.id.noteUnarchive).setVisible(false);
        } else if(parentFragment == R.integer.ARCHIVED_NOTES_FRAGMENT){
            menu.findItem(R.id.noteUnarchive).setVisible(true);
            menu.findItem(R.id.noteArchive).setVisible(false);
        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(editMode) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    editMode = false;
                    populateShowMode();
                    titleViewSwitcher.showNext();
                    infoViewSwitcher.showNext();
                    this.onCreateOptionsMenu(menu);
                    return true;
                case R.id.chooseMarkerColor:
                    chooseMarkerColor();
                    return true;
                case R.id.noteDone:
                    if (validator.formIsValid()) {
                        editMode = false;
                        this.onCreateOptionsMenu(menu);
                        updateNote();
                    }
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
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
                    titleViewSwitcher.showNext();
                    infoViewSwitcher.showNext();
                    populateEditMode();
                    this.onCreateOptionsMenu(menu);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(editMode) {
            editMode = false;
            populateShowMode();
            titleViewSwitcher.showNext();
            infoViewSwitcher.showNext();
            this.onCreateOptionsMenu(menu);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Populates Note Details layout with the Note received through Activity bundle.
     */
    private void populateShowMode() {
        noteId = note.getId();
        noteTitle = note.getTitle();
        noteInfo = note.getInfo();
        noteColor = note.getColor();
        noteTimestamp = note.getTimestamp();

        noteDetailTitle.setText(noteTitle);
        noteDetailInfo.setText(noteInfo);
        noteDetailTimestamp.setText(noteTimestamp);
        if (noteColor != null && !noteColor.isEmpty()) {
            GradientDrawable marker_shape = (GradientDrawable) marker.getBackground();
            marker_shape.setColor(Color.parseColor(noteColor));
        }
    }

    /**
     * Populates Edit Mode layout with relevant Note information.
     */
    private void populateEditMode() {
        inputTitle = (TextInputEditText) findViewById(R.id.input_title);
        inputInfo = (TextInputEditText) findViewById(R.id.input_info);

        inputTitle.setText(noteTitle);
        inputInfo.setText(noteInfo);

        inputTitle.requestFocus();

        inputLayout = (View) findViewById(R.id.note_activity_layout);
        validator = new NoteInputValidator(this, inputLayout);
    }

    private void showArchiveDialog() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this).setTitle("Archive Note")
                        .setMessage("Do you want to archive this note? ")
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
                new AlertDialog.Builder(this).setTitle("Permanently Delete Note")
                        .setMessage("Do you want to delete this note? ")
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
                    Toast.makeText(getApplicationContext(), "Failed to delete note",
                            Toast.LENGTH_SHORT).show();
                    showParentActivity();
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
                    showParentActivity();
                }
            }
        });

        showParentActivity();

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
                    note.setArchived(false);

                    Snackbar.make(targetView, "Unarchived note", Snackbar.LENGTH_SHORT)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    note.setArchived(true);

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
                Toast.makeText(getApplicationContext(), "Failed to unarchive note", Toast.LENGTH_LONG).show();
            }
        });

        showParentActivity();

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
                    note.setArchived(true);

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
                Toast.makeText(getApplicationContext(), "Failed to archive note", Toast.LENGTH_LONG).show();
            }
        });

        showParentActivity();
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
                new AlertDialog.Builder(this).setTitle("Marker color")
                        .setMessage("Assign a specific color to your Note")
                        .setCancelable(true)
                        .setView(colorPickerLayout);
        colorPickerDialog = alertDialogBuilder.create();
        colorPickerDialog.show();
    }

    public void updateNote(){
        String id = noteId;
        String newTitle = inputTitle.getText().toString();
        String newInfo = inputInfo.getText().toString();
        String newColor = "#" + Integer.toHexString(markerColor);
        String updatedTimestamp = getCurrentTimestamp();

        Note updatedNote = new Note(id, newTitle, newInfo, newColor, updatedTimestamp);

        storageManager.writeNoteToFirebase(updatedNote, false, new FirebaseStorageCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(app, "Note successfully updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(app, "Failed to update note, please try again", Toast.LENGTH_SHORT).show();
            }
        });

        if(parentFragment == R.integer.NOTES_FRAGMENT) {
            showParentActivity();
        } else if(parentFragment == R.integer.ARCHIVED_NOTES_FRAGMENT) {
            titleViewSwitcher.showNext();
            infoViewSwitcher.showNext();
        }

    }

    private String getCurrentTimestamp() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    private void showParentActivity() {
        NavUtils.navigateUpFromSameTask(this);
        finish();
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
