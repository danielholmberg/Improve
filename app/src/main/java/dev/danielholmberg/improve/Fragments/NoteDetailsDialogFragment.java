package dev.danielholmberg.improve.Fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import dev.danielholmberg.improve.Adapters.TagsAdapter;
import dev.danielholmberg.improve.Models.Note;
import dev.danielholmberg.improve.Models.Tag;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.NoteInputValidator;
import dev.danielholmberg.improve.ViewHolders.TagViewHolder;

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

    private Toolbar toolbar;

    private Bundle noteBundle;
    private Note note;
    private int parentFragment;

    private ProgressDialog exportDialog;

    private boolean editMode = false;

    private String newTagColor;
    private ImageButton noTagColor, tagColor1, tagColor2, tagColor3, tagColor4, tagColor5, tagColor6, tagColor7, tagColor8;
    private ArrayList<String> newTags;
    private HashMap<String, Boolean> oldTags;

    private NoteInputValidator validator;

    private TextInputLayout inputTitleLayout, inputInfoLayout;
    private EditText inputTitle, inputInfo;
    private FlexboxLayout tagsList;
    private String noteId, noteTitle, noteInfo, noteTimestampAdded, noteTimestampUpdated;

    public static NoteDetailsDialogFragment newInstance() {
        return new NoteDetailsDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        databaseManager = app.getFirebaseDatabaseManager();
        activity = (AppCompatActivity) getActivity();
        context = app.getMainActivityRef();

        noteBundle = getArguments();

        if(noteBundle != null) {
            parentFragment = noteBundle.getInt(NOTE_PARENT_FRAGMENT_KEY);
            note = (Note) noteBundle.getParcelable(NOTE_KEY);
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
        View view = inflater.inflate(R.layout.fragment_note_details, container);
        setCancelable(false);

        newTagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagColorNull));

        // Set transparent background and no title to enable corner radius.
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.background_note_details);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Objects.requireNonNull(this.getDialog().getWindow())
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar_note_details_fragment);
        createOptionsMenu();
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(editMode) {
                    switch (item.getItemId()) {
                        case R.id.addTag:
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
                            newTags = new ArrayList<>();
                            createOptionsMenu();
                            toggleMode(editMode);
                            return true;
                        default:
                            return true;
                    }
                }
            }
        });

        inputTitleLayout = (TextInputLayout) view.findViewById(R.id.input_title_layout);
        inputInfoLayout = (TextInputLayout) view.findViewById(R.id.input_info_layout);

        inputTitle = (EditText) inputTitleLayout.findViewById(R.id.input_title);
        inputInfo = (EditText) inputInfoLayout.findViewById(R.id.input_info);

        tagsList = (FlexboxLayout) view.findViewById(R.id.footer_note_tags_list);

        validator = new NoteInputValidator(context, inputTitleLayout);

        toggleMode(editMode);

        if(note != null) {
            populateNoteDetails();
            oldTags = new HashMap<String, Boolean>(note.getTags());
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
        Log.d(TAG, "Menu is redrawn");
        TextView menuTitle = ((TextView) toolbar.findViewById(R.id.toolbar_note_dialog_title_tv));
        Menu menu = toolbar.getMenu();
        menu.clear();

        // Check (if)Edit or (else)Show.
        if(editMode) {

            menuTitle.setText(R.string.title_edit_note);
            toolbar.inflateMenu(R.menu.menu_edit_note);
            toolbar.findViewById(R.id.close_dialog_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDiscardChangesDialog();
                }
            });

        } else {

            menuTitle.setText(R.string.note_activity_details);
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
                menu.findItem(R.id.noteArchive).setVisible(true);
                menu.findItem(R.id.noteUnarchive).setVisible(false);
            } else if(parentFragment == R.integer.ARCHIVED_NOTES_FRAGMENT){
                menu.findItem(R.id.noteUnarchive).setVisible(true);
                menu.findItem(R.id.noteArchive).setVisible(false);
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
                                createOptionsMenu();

                                note.setTags(oldTags);

                                populateNoteDetails();
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
     * Displays a dialog window to the user in order to choose a Tag color and enter a new Tag label.
     * Adds the new Tag to Firebase.
     */
    private void showAddNewTagDialog() {
        View addDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_tag, null, false);

        final TextInputEditText labelEditText = (TextInputEditText) addDialogView.findViewById(R.id.tag_label_et);
        final ImageButton createTagButton = (ImageButton) addDialogView.findViewById(R.id.create_tag_btn);
        final RecyclerView existingTagsListView = (RecyclerView) addDialogView.findViewById(R.id.existing_tags_list);
        final TextView labelCounterCurrent = (TextView) addDialogView.findViewById(R.id.tag_label_counter_current);

        noTagColor = (ImageButton) addDialogView.findViewById(R.id.tag_no_color);
        tagColor1 = (ImageButton) addDialogView.findViewById(R.id.tag_color_1);
        tagColor2 = (ImageButton) addDialogView.findViewById(R.id.tag_color_2);
        tagColor3 = (ImageButton) addDialogView.findViewById(R.id.tag_color_3);
        tagColor4 = (ImageButton) addDialogView.findViewById(R.id.tag_color_4);
        tagColor5 = (ImageButton) addDialogView.findViewById(R.id.tag_color_5);
        tagColor6 = (ImageButton) addDialogView.findViewById(R.id.tag_color_6);
        tagColor7 = (ImageButton) addDialogView.findViewById(R.id.tag_color_7);
        tagColor8 = (ImageButton) addDialogView.findViewById(R.id.tag_color_8);

        existingTagsListView.setHasFixedSize(false);
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.CENTER);
        layoutManager.setAlignItems(AlignItems.STRETCH);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        existingTagsListView.setLayoutManager(layoutManager);

        final TagsAdapter tagsAdapter = app.getTagsAdapter();
        existingTagsListView.setAdapter(tagsAdapter);

        labelCounterCurrent.setText("0");
        labelEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                labelCounterCurrent.setText(String.valueOf(charSequence.length()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        tagsAdapter.setCurrentNote(note);

        final AlertDialog addNewTagDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.menu_tag_add)
                .setView(addDialogView)
                .setCancelable(false)
                .setNeutralButton("Edit Tags", null)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tagsAdapter.removeCurrentNote();
                        tagsAdapter.setEditMode(false);
                        renderTagList();
                        dialogInterface.dismiss();
                    }
                })
                .create();
        addNewTagDialog.show();

        addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View.OnClickListener listener = this;
                tagsAdapter.setEditMode(true);
                renderTagList();
                addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setText("Stop Edit");
                addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tagsAdapter.setEditMode(false);
                        renderTagList();
                        addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setText("Edit Tags");
                        addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(listener);
                    }
                });
            }
        });

        Objects.requireNonNull(addNewTagDialog.getWindow()).setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        createTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(labelEditText.getText())) {
                    labelEditText.setError(getString(R.string.err_msg_tag_label));
                    return;
                }

                final String newTagId = databaseManager.getTagRef().push().getKey();
                final Tag newTag = new Tag(newTagId);

                newTag.setLabel(labelEditText.getText().toString());
                newTag.setColor(newTagColor);
                if(newTagColor.equals("#" + Integer.toHexString(getResources().getColor(R.color.tagColorNull)))) {
                    newTag.setTextColor("#" + Integer.toHexString(getResources().getColor(R.color.tagTextColorNull)));
                } else {
                    newTag.setTextColor("#" + Integer.toHexString(getResources().getColor(R.color.tagTextColor)));
                }

                databaseManager.addTag(newTag);
                tagsAdapter.addTag(newTag);

                note.addTag(newTag.getId());
                newTags.add(newTag.getId());

                // Only update the List of Tags to keep all other edited information in check.
                renderTagList();

                // Reset
                if(labelEditText.getText() != null) labelEditText.getText().clear();
                newTagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagColorNull));
                uncheckAllOtherTags();

                // Display the latest added Tag
                layoutManager.scrollToPosition(tagsAdapter.getItemCount()-1);
            }
        });

        noTagColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagColorNull));
                uncheckAllOtherTags();
            }
        });
        tagColor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagColor1));
                tagColor1.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_1_checked));
                uncheckAllOtherTags();
            }
        });
        tagColor2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagColor2));
                tagColor2.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_2_checked));
                uncheckAllOtherTags();
            }
        });
        tagColor3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagColor3));
                tagColor3.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_3_checked));
                uncheckAllOtherTags();
            }
        });
        tagColor4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagColor4));
                tagColor4.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_4_checked));
                uncheckAllOtherTags();
            }
        });
        tagColor5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagColor5));
                tagColor5.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_5_checked));
                uncheckAllOtherTags();
            }
        });
        tagColor6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagColor6));
                tagColor6.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_6_checked));
                uncheckAllOtherTags();
            }
        });
        tagColor7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagColor7));
                tagColor7.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_7_checked));
                uncheckAllOtherTags();
            }
        });
        tagColor8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagColor8));
                tagColor8.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_8_checked));
                uncheckAllOtherTags();
            }
        });
    }

    private void uncheckAllOtherTags() {
        if(!newTagColor.equals("#" + Integer.toHexString(getResources().getColor(R.color.tagColor1)))) {
            tagColor1.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_1));
        }
        if(!newTagColor.equals("#" + Integer.toHexString(getResources().getColor(R.color.tagColor2)))) {
            tagColor2.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_2));
        }
        if(!newTagColor.equals("#" + Integer.toHexString(getResources().getColor(R.color.tagColor3)))) {
            tagColor3.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_3));
        }
        if(!newTagColor.equals("#" + Integer.toHexString(getResources().getColor(R.color.tagColor4)))) {
            tagColor4.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_4));
        }
        if(!newTagColor.equals("#" + Integer.toHexString(getResources().getColor(R.color.tagColor5)))) {
            tagColor5.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_5));
        }
        if(!newTagColor.equals("#" + Integer.toHexString(getResources().getColor(R.color.tagColor6)))) {
            tagColor6.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_6));
        }
        if(!newTagColor.equals("#" + Integer.toHexString(getResources().getColor(R.color.tagColor7)))) {
            tagColor7.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_7));
        }
        if(!newTagColor.equals("#" + Integer.toHexString(getResources().getColor(R.color.tagColor8)))) {
            tagColor8.setBackground(app.getResources().getDrawable(R.drawable.ic_tag_8));
        }
    }

    /**
     * Changes the NoteDetails layout depending on the incoming editMode.
     * @param editMode - True; Edit, False; Show
     */
    private void toggleMode(boolean editMode) {
        inputTitleLayout.setCounterEnabled(editMode);
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

        if(note.getAdded() != null) {
            noteTimestampAdded = tranformMillisToDateSring(Long.parseLong(note.getAdded()));
        }
        if(note.getUpdated() != null) {
            noteTimestampUpdated = tranformMillisToDateSring(Long.parseLong(note.getUpdated()));
        }
        if(note.getTitle() != null) {
            inputTitle.setText(noteTitle);
        }
        if(note.getInfo() != null) {
            inputInfo.setText(noteInfo);
        }

        if(TextUtils.isEmpty(noteInfo)) {
            inputInfo.setVisibility(View.GONE);
        } else {
            inputInfo.setVisibility(View.VISIBLE);
        }

        renderTagList();

    }

    private void renderTagList() {
        tagsList.removeAllViews();
        for(String tagId: note.getTags().keySet()) {
            View tagView = LayoutInflater.from(context).inflate(R.layout.item_tag, tagsList, false);
            TagViewHolder tagViewHolder = new TagViewHolder(tagView);
            tagViewHolder.bindModelToView(Improve.getInstance().getTagsAdapter().getTag(tagId));
            tagsList.addView(tagView);
        }
    }

    private String tranformMillisToDateSring(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);

        return DateFormat.getDateTimeInstance().format(calendar.getTime());
    }

    private void showInfoDialog() {
        RelativeLayout noteInfoLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.dialog_note_info, null);
        TextView noteAddedTimestamp = noteInfoLayout.findViewById(R.id.note_info_added_timestamp_tv);
        TextView noteUpdatedTimestamp = noteInfoLayout.findViewById(R.id.note_info_updated_timestamp_tv);

        noteAddedTimestamp.setText(noteTimestampAdded);
        noteUpdatedTimestamp.setText(noteTimestampUpdated);

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

    /*
    private void shareNoteToDrive(final Note note) {
        // TODO Integrate Drive accessibility
    }

    private void checkWritePermission() {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            }

            // PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            // Permission has already been granted
            showExportProgressDialog();
            shareNoteToDrive(note);
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
                    shareNoteToDrive(note);
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
    }*/

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
                                if(note.isArchived()) {
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
        databaseManager.deleteNoteFromArchive(note);
        dismissDialog();
    }

    private void deleteNote(final Note note) {
        databaseManager.deleteNote(note);
        dismissDialog();

    }

    private void unarchiveNote() {
        databaseManager.unarchiveNote(note);
        dismissDialog();
    }

    private void archiveNote() {
        databaseManager.archiveNote(note);
        dismissDialog();
    }

    public void updateNote(){
        String id = noteId;
        boolean archived = note.isArchived();
        String newTitle = inputTitle.getText().toString();
        String newInfo = inputInfo.getText().toString();
        String timestampAdded = note.getAdded();
        String timestampUpdated = Long.toString(System.currentTimeMillis());
        Note updatedNote;

        if(TextUtils.isEmpty(newInfo.trim())) {
            newInfo = "";
        }

        updatedNote = new Note(id);

        updatedNote.setTitle(newTitle);
        updatedNote.setInfo(newInfo);
        updatedNote.setTags(note.getTags());
        updatedNote.setAdded(timestampAdded);
        updatedNote.setArchived(archived);
        updatedNote.setUpdated(timestampUpdated);

        note = updatedNote;
        oldTags = new HashMap<String, Boolean>(note.getTags());

        if(note.isArchived()) {
            databaseManager.updateArchivedNote(updatedNote);
        } else {
            databaseManager.updateNote(updatedNote);
        }

        editMode = false;
        populateNoteDetails();
        createOptionsMenu();
        toggleMode(editMode);

    }

    private void dismissDialog() {
        this.dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(editMode) {
            if(newTags != null) {
                // Re-add newly created (and therefore added) Tags from Note.
                for (String tagId : newTags) {
                    note.addTag(tagId);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(editMode) {
            // Remove newly created (and therefore added) Tags from Note.
            for(String tagId: newTags) {
                note.removeTag(tagId);
            }
        }
    }

}
