package dev.danielholmberg.improve.Fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import dev.danielholmberg.improve.Adapters.TagColorListAdapter;
import dev.danielholmberg.improve.Callbacks.FirebaseDatabaseCallback;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Components.Tag;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.NoteInputValidator;

public class NoteDetailsDialogFragment extends DialogFragment {
    public static final String TAG = NoteDetailsDialogFragment.class.getSimpleName();

    public static final String NOTE_PARENT_FRAGMENT_KEY = "parentFragment";
    public static final String NOTE_ADAPTER_POS_KEY = "adapterItemPos";
    public static final String NOTE_KEY = "note";
    private static final String EXPORTED_NOTE_DIRECTORY_PATH = "Notes";
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private Improve app;
    private FirebaseDatabaseManager databaseManager;

    private Context context;
    private AppCompatActivity activity;
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

    private Tag selectedTag;
    private int tagColorInt;

    // **** [START] EditMode variables ****

    private View inputLayout;
    private NoteInputValidator validator;

    private EditText inputTitle, inputInfo;
    private String noteId, noteTitle, tagColor, noteInfo, noteTimestampAdded, noteTimestampUpdated;

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
        databaseManager = app.getFirebaseDatabaseManager();
        activity = (AppCompatActivity) getActivity();

        noteBundle = getArguments();

        if(noteBundle != null) {
            parentFragment = noteBundle.getInt(NOTE_PARENT_FRAGMENT_KEY);
            note = (Note) noteBundle.getSerializable(NOTE_KEY);

            if (note != null) {
                selectedTag = app.getTag(note.getTagId());
                if(selectedTag == null) {
                    selectedTag = app.getTag("Untagged");
                }
            } else {
                Toast.makeText(context, "Failed to show Note details, please try again",
                        Toast.LENGTH_SHORT).show();
                dismissDialog();
            }
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

        Objects.requireNonNull(this.getDialog().getWindow())
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar_note_details_fragment);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(editMode) {
                    switch (item.getItemId()) {
                        case R.id.add_new_tag:
                            showAddNewTagDialog();
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
                            checkWritePermission();
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
        markerColor = getResources().getColor(R.color.tagUntagged);
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

        // Check (if)Edit or (else)Show.
        if(editMode) {

            // Set the corresponding Menu to the Toolbar.
            ((TextView) toolbar.findViewById(R.id.toolbar_note_activity_title_tv)).setText(R.string.title_edit_note);
            toolbar.inflateMenu(R.menu.menu_edit_note);

            toolbar.findViewById(R.id.close_dialog_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDiscardChangesDialog();
                }
            });

            // Configure menu-option "Tag"
            final MenuItem noteTagMenuItem = toolbar.getMenu().findItem(R.id.noteTag);

            // Get Sub-menu which also contains the Group to show current Tags.
            Menu tagMenu = noteTagMenuItem.getSubMenu();

            // Add all current Tags to the Group in Sub-menu.
            Random r = new Random();
            for(Map.Entry<String, Tag> tagEntry: app.getTagHashMap().entrySet()) {
                final Tag tag = tagEntry.getValue();

                // Create new MenuItem related to the specified Tag.
                MenuItem tagMenuItem = tagMenu.add(
                        R.id.group_tag_list,
                        r.nextInt(),
                        0,
                        tag.getLabel()
                ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);

                // Set the correct Tag-drawable to the color of the specified Tag.
                setTagIconColor(tagMenuItem, tag.getColorInt());

                // Add MenuItem-clickListener to detect when a user chooses this specific Tag.
                tagMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        // Set the selected Tag to current specified Tag.
                        selectedTag = tag;
                        // Change the Tag-drawable related to the newly chosen Tag.
                        setTagIconColor(noteTagMenuItem, selectedTag.getColorInt());
                        // Change the boarder marker color to the color of selected Tag.
                        boarderMarker.setBackgroundColor(getResources().getColor(selectedTag.getColorInt()));
                        return true;
                    }
                });

                // Set the default selected Tag to be the Notes' existing Tag
                // if the Note already has an assigned Tag.
                if(note.getTagId() != null) {
                    if(note.getTagId().equals(tag.getTagId())){
                        selectedTag = tag;
                    }
                } else {
                    selectedTag = app.getTag("Untagged");
                }
            }

            // If the Note already has an existing Tag
            // --> Set the correct Tag-drawable to the color of the existing Tag.
            if(note.getTagId() != null) {
                setTagIconColor(noteTagMenuItem, selectedTag.getColorInt());
            } else {
                noteTagMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_tag_untagged));
            }

        } else {

            // Set the corresponding Menu to the Toolbar.
            ((TextView) toolbar.findViewById(R.id.toolbar_note_activity_title_tv)).setText(R.string.note_activity_details);
            toolbar.inflateMenu(R.menu.fragment_note_details_show);
            toolbar.findViewById(R.id.close_dialog_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismissDialog();
                }
            });

            // Depending on the parent fragment.
            // Change MenuItem "Archive" and "Unarchive".
            if(parentFragment == R.integer.NOTES_FRAGMENT) {
                toolbar.getMenu().findItem(R.id.noteArchive).setVisible(true);
                toolbar.getMenu().findItem(R.id.noteUnarchive).setVisible(false);
            } else if(parentFragment == R.integer.ARCHIVED_NOTES_FRAGMENT){
                toolbar.getMenu().findItem(R.id.noteUnarchive).setVisible(true);
                toolbar.getMenu().findItem(R.id.noteArchive).setVisible(false);
            }
        }
    }

    private void showDiscardChangesDialog() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context)
                        .setMessage(R.string.dialog_discard_changes_msg)
                        .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                editMode = false;
                                toggleMode(editMode);
                                populateNoteDetails();
                                createOptionsMenu();
                                dialogInterface.dismiss();
                            }
                        }).setNegativeButton("Keep editing", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    /**
     * Sets the drawable of the MenuItem depending on the resource color integer.
     * @param menuItem - Tag MenuItem
     * @param colorInt - Tag color
     */
    private void setTagIconColor(MenuItem menuItem, int colorInt) {
        switch (colorInt) {
            case R.color.tagRed:
                menuItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_tag_red));
                break;
            case R.color.tagPurple:
                menuItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_tag_purple));
                break;
            case R.color.tagBlue:
                menuItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_tag_blue));
                break;
            case R.color.tagDarkOrange:
                menuItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_tag_orange));
                break;
            case R.color.tagBlueGrey:
                menuItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_tag_blue_grey));
                break;
            case R.color.tagBabyBlue:
                menuItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_tag_baby_blue));
                break;
            case R.color.tagDarkGrey:
                menuItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_tag_dark_grey));
                break;
            case R.color.tagGreen:
                menuItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_tag_green));
                break;
            case R.color.tagUntagged:
                menuItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_tag_untagged));
                break;
            default:
                menuItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_tag_untagged));
        }
    }

    /**
     * Displays a dialog window to the user in order to choose a Tag color and enter a new Tag label.
     * Adds the new Tag to Firebase.
     */
    private void showAddNewTagDialog() {
        View addDialogView = getLayoutInflater().inflate(R.layout.dialog_tag, null, false);

        final EditText labelEditText = (EditText) addDialogView.findViewById(R.id.tag_label_et);
        Spinner tagColorSpinner = (Spinner) addDialogView.findViewById(R.id.tag_color_spinner);

        final TagColorListAdapter adapter = new TagColorListAdapter(context);
        tagColorSpinner.setAdapter(adapter);
        tagColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                tagColorInt = adapter.tagColors[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                tagColorInt = adapter.tagColors[0];
            }
        });

        final AlertDialog addNewTagDialog = new AlertDialog.Builder(context)
                .setView(addDialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Dummy
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create();
        addNewTagDialog.show();
        addNewTagDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String tagId = databaseManager.getTagRef().push().getKey();
                        String label = labelEditText.getText().toString().toUpperCase();
                        String colorHex = "#" + Integer.toHexString(getResources().getColor(tagColorInt));
                        int colorInt = tagColorInt;

                        if(!label.isEmpty()) {
                            final Tag newTag = new Tag(tagId, label, colorHex, colorInt);
                            databaseManager.addTag(newTag, new FirebaseDatabaseCallback() {
                                @Override
                                public void onSuccess() {}

                                @Override
                                public void onFailure(String errorMessage) {
                                    if (tagId != null) {
                                        databaseManager.getTagRef().child(tagId).removeValue();
                                    }
                                    Toast.makeText(context, "Failed to add new Tag, please try again", Toast.LENGTH_SHORT).show();
                                }
                            });

                            app.addTagToList(newTag);

                            selectedTag = newTag;
                            note.setTagId(selectedTag.getTagId());
                            populateNoteDetails();
                            createOptionsMenu();
                            boarderMarker.setBackgroundColor(getResources().getColor(selectedTag.getColorInt()));

                            addNewTagDialog.dismiss();
                        } else {
                            labelEditText.setError("Please enter a label");
                            labelEditText.requestFocus();
                        }
                    }
                });
    }

    /**
     * Changes the NoteDetails layout depending on the incoming editMode.
     * @param editMode - True; Edit, False; Show
     */
    private void toggleMode(boolean editMode) {
        inputTitle.setEnabled(editMode);
        inputInfo.setEnabled(editMode);

        if(editMode) {
            inputInfo.setVisibility(View.VISIBLE);
            inputTitle.requestFocus();
            getDialog().setCancelable(false);
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } else {
            getDialog().setCancelable(true);
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            if(TextUtils.isEmpty(noteInfo)) {
                inputInfo.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Sets all the necessary detailed information about the Note.
     */
    private void populateNoteDetails() {
        noteId = note.getId();
        noteTitle = note.getTitle();
        noteInfo = note.getInfo();
        noteTimestampAdded = tranformMillisToDateSring(Long.parseLong(note.getTimestampAdded()));
        noteTimestampUpdated = tranformMillisToDateSring(Long.parseLong(note.getTimestampUpdated()));

        inputTitle.setText(noteTitle);
        inputInfo.setText(noteInfo);

        if(note.getTagId() != null) {
            Tag tag = app.getTag(note.getTagId());

            if(tag != null) {
                tagColor = tag.getColorHex();
                boarderMarker.setBackgroundColor(Color.parseColor(tagColor));
                markerColor = getResources().getColor(tag.getColorInt());
            } else {
                tagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagUntagged));
                boarderMarker.setBackgroundColor(getResources().getColor(R.color.tagUntagged));
                markerColor = getResources().getColor(R.color.tagUntagged);
            }

        } else {
            tagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagUntagged));
            boarderMarker.setBackgroundColor(getResources().getColor(R.color.tagUntagged));
            markerColor = getResources().getColor(R.color.tagUntagged);
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
            File notesRoot = new File(app.getRootDir(), EXPORTED_NOTE_DIRECTORY_PATH);
            if(!notesRoot.exists()) {
                notesRoot.mkdirs();
            }

            final File noteFile = new File(notesRoot, note.getId() + ".txt");
            FileWriter writer = new FileWriter(noteFile);
            writer.append(note.toJSON());
            writer.flush();
            writer.close();

            // Delay the export to visual show ProgressDialog for 1000ms (1 second).
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Exported Note to " + noteFile.getPath(), Toast.LENGTH_LONG).show();
                    exportDialog.dismiss();
                }
            }, 1000);

        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(context, "Export failed, please try again", Toast.LENGTH_SHORT).show();
            exportDialog.dismiss();
        }
    }

    private void checkWritePermission() {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);

            // PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            // Permission has already been granted
            showExportProgressDialog();
            exportNoteToFile(note);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    showExportProgressDialog();
                    exportNoteToFile(note);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void showExportProgressDialog() {
        exportDialog = ProgressDialog.show(context, "Exporting Note to .txt-file",
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
                                if(note.getArchived()) {
                                    deleteNoteFromArchive(note);
                                } else {
                                    deleteNote(note);
                                }
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

    private void deleteNoteFromArchive(final Note note) {
        databaseManager.deleteNoteFromArchive(note, new FirebaseDatabaseCallback() {
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
                                    databaseManager.updateArchivedNote(note, new FirebaseDatabaseCallback() {
                                        @Override
                                        public void onSuccess() {}

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            Log.e(TAG, "Failed to undo 'Delete note': " + errorMessage);
                                        }
                                    });
                                }
                            }).show();
                } else {
                    Toast.makeText(context, "Failed to delete note, please try again",
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
                                    deleteNoteFromArchive(note);
                                }
                            }).show();
                } else {
                    dismissDialog();
                }
            }
        });

        dismissDialog();

    }

    private void deleteNote(final Note note) {
        databaseManager.deleteNote(note, new FirebaseDatabaseCallback() {
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
                                    databaseManager.updateNote(note, new FirebaseDatabaseCallback() {
                                        @Override
                                        public void onSuccess() {}

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            Log.e(TAG, "Failed to undo 'Delete note': " + errorMessage);
                                        }
                                    });
                                }
                            }).show();
                } else {
                    Toast.makeText(context, "Failed to delete note, please try again",
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
        databaseManager.unarchiveNote(note, new FirebaseDatabaseCallback() {
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
                                    databaseManager.archiveNote(note, new FirebaseDatabaseCallback() {
                                        @Override
                                        public void onSuccess() {}

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
        databaseManager.archiveNote(note, new FirebaseDatabaseCallback() {
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

                                    databaseManager.unarchiveNote(note, new FirebaseDatabaseCallback() {
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
        String timestampAdded = note.getTimestampAdded();
        String timestampUpdated = Long.toString(System.currentTimeMillis());
        String tagId = selectedTag.getTagId();

        if(TextUtils.isEmpty(newInfo.trim())) {
            newInfo = "";
        }

        Note updatedNote = new Note(id, newTitle, newInfo, timestampAdded, tagId);
        updatedNote.setArchived(archived);
        updatedNote.setTimestampUpdated(timestampUpdated);

        note = updatedNote;

        if(note.getArchived()) {
            databaseManager.updateArchivedNote(updatedNote, new FirebaseDatabaseCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(app, "Archived Note successfully updated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(app, "Failed to update archived note, please try again", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            databaseManager.updateNote(updatedNote, new FirebaseDatabaseCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(app, "Note successfully updated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(app, "Failed to update note, please try again", Toast.LENGTH_SHORT).show();
                }
            });
        }

        editMode = false;
        populateNoteDetails();
        createOptionsMenu();
        toggleMode(editMode);

    }

    private void dismissDialog() {
        this.dismiss();
    }
}
