package dev.danielholmberg.improve.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import dev.danielholmberg.improve.Adapters.TagColorListAdapter;
import dev.danielholmberg.improve.Callbacks.FirebaseDatabaseCallback;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Components.Tag;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.NoteInputValidator;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class AddNoteActivity extends AppCompatActivity {
    private static final String TAG = AddNoteActivity.class.getSimpleName();

    private Improve app;
    private FirebaseDatabaseManager databaseManager;
    private Context context;
    private NoteInputValidator validator;

    private ArrayList<Tag> tagList = new ArrayList<>();
    private int tagColorInt;
    private Tag selectedTag;
    private TextInputEditText inputTitle, inputInfo;

    private Toolbar toolbar;
    private View inputLayout;
    private MenuItem noteTagMenuItem;

    private Tag untaggedTag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        app = Improve.getInstance();
        databaseManager = app.getFirebaseDatabaseManager();
        context = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar_add_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        retrieveTagList();

        inputLayout = (View) findViewById(R.id.input_layout);
        inputTitle = (TextInputEditText) findViewById(R.id.input_title);
        inputInfo = (TextInputEditText) findViewById(R.id.input_info);

        inputTitle.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        validator = new NoteInputValidator(this, inputLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_note, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();

        this.noteTagMenuItem = menu.findItem(R.id.noteTag);
        Menu tagMenu = noteTagMenuItem.getSubMenu();

        Random r = new Random();
        for(final Tag tag: tagList) {
            MenuItem tagMenuItem = tagMenu.add(
                    R.id.group_tag_list,
                    r.nextInt(),
                    0,
                    tag.getLabel()
            );
            tagMenuItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
            setTagIconColor(tagMenuItem, tag.getColorInt());

            tagMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    selectedTag = tag;
                    setTagIconColor(noteTagMenuItem, selectedTag.getColorInt());
                    return true;
                }
            });
        }

        if(selectedTag != null) {
            setTagIconColor(noteTagMenuItem, selectedTag.getColorInt());
        } else {
            noteTagMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_tag_untagged));
        }

        menu.findItem(R.id.tag_untagged).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                selectedTag = untaggedTag;
                setTagIconColor(noteTagMenuItem, selectedTag.getColorInt());
                return true;
            }
        });

        return true;
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.add_new_tag:
                showAddNewTagDialog();
                return true;
            case R.id.noteDone:
                if(validator.formIsValid()) {
                    addNote();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void retrieveTagList() {
        untaggedTag = new Tag(
                "Untagged",
                getResources().getString(R.string.menu_tag_untagged),
                "#" + Integer.toHexString(getResources().getColor(R.color.tagUntagged)),
                R.color.tagUntagged
        );

        // Set default tag selected to Untagged.
        selectedTag = untaggedTag;

        databaseManager.getTagRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tagList = new ArrayList<>();

                for(DataSnapshot tagSnapshot: dataSnapshot.getChildren()) {
                    Tag tag = tagSnapshot.getValue(Tag.class);

                    if (tag != null) {
                        if(!tag.getTagId().equals("Untagged")) {
                            tagList.add(tag);
                            invalidateOptionsMenu();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void showAddNewTagDialog() {
        View addDialogView = getLayoutInflater().inflate(R.layout.dialog_tag, null, false);

        final EditText labelEditText = (EditText) addDialogView.findViewById(R.id.tag_label_et);
        Spinner tagColorSpinner = (Spinner) addDialogView.findViewById(R.id.tag_color_spinner);

        final TagColorListAdapter adapter = new TagColorListAdapter(this);
        tagColorSpinner.setAdapter(adapter);
        tagColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                tagColorInt = getResources().getColor(adapter.tagColors[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                tagColorInt = getResources().getColor(adapter.tagColors[0]);
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
                        String colorHex = "#" + Integer.toHexString(tagColorInt);
                        int colorInt = tagColorInt;

                        if(!label.isEmpty()) {
                            final Tag newTag = new Tag(tagId, label, colorHex, colorInt);
                            databaseManager.addTag(newTag, new FirebaseDatabaseCallback() {
                                @Override
                                public void onSuccess() {
                                    selectedTag = newTag;
                                    invalidateOptionsMenu();
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    if (tagId != null) {
                                        databaseManager.getTagRef().child(tagId).removeValue();
                                    }
                                    Toast.makeText(context, "Failed to add new Tag, please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                            addNewTagDialog.dismiss();
                        } else {
                            labelEditText.setError("Please enter a label");
                            labelEditText.requestFocus();
                        }
                    }
                });
    }

    public void addNote(){
        String id = databaseManager.getNotesRef().push().getKey();
        String title = inputTitle.getText().toString();
        String info = inputInfo.getText().toString();
        String timestampAdded = Long.toString(System.currentTimeMillis());

        if(TextUtils.isEmpty(info)) {
            info = "";
        }

        Note newNote = new Note(id, title, info, timestampAdded, selectedTag.getTagId());
        newNote.setTimestampUpdated(timestampAdded);

        databaseManager.addNote(newNote, new FirebaseDatabaseCallback() {

            @Override
            public void onSuccess() {
                showParentActivity();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(app, "Failed to add new note, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showParentActivity() {
        restUI();
        startActivity(new Intent(this, MainActivity.class));
        finishAfterTransition();
    }

    private void restUI(){
        inputTitle.getText().clear();
        inputInfo.getText().clear();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onBackPressed() {
        showParentActivity();
    }
}
