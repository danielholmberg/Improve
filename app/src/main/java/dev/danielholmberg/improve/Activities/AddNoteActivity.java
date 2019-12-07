package dev.danielholmberg.improve.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import dev.danielholmberg.improve.Adapters.TagsAdapter;
import dev.danielholmberg.improve.Adapters.VipImagesAdapter;
import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Models.Note;
import dev.danielholmberg.improve.Models.Tag;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.Models.VipImage;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.NoteInputValidator;
import dev.danielholmberg.improve.ViewHolders.TagViewHolder;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class AddNoteActivity extends AppCompatActivity {
    private static final String TAG = AddNoteActivity.class.getSimpleName();

    private static final int PICK_IMAGE_REQUEST = 1;

    private Improve app;
    private FirebaseDatabaseManager databaseManager;
    private Context context;
    private NoteInputValidator validator;

    private Note newNote;
    private String newTagColor;
    private ImageButton noTagColor, tagColor1, tagColor2, tagColor3, tagColor4, tagColor5, tagColor6, tagColor7, tagColor8;
    private ArrayList<String> newTags;

    private TextInputEditText inputTitle, inputInfo;
    private FlexboxLayout tagsList;

    private Toolbar toolbar;
    private TagsAdapter tagsAdapter;

    // VIP
    private RecyclerView vipImagesRecyclerView;
    private VipImagesAdapter vipImagesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        app = Improve.getInstance();
        databaseManager = app.getFirebaseDatabaseManager();
        context = this;

        initActivity();
    }

    private void initActivity() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_add_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextInputLayout inputTitleLayout = (TextInputLayout) findViewById(R.id.input_title_layout);
        TextInputLayout inputInfoLayout = (TextInputLayout) findViewById(R.id.input_info_layout);

        tagsList = (FlexboxLayout) findViewById(R.id.footer_note_tags_list);

        inputTitle = (TextInputEditText) findViewById(R.id.input_title);
        inputInfo = (TextInputEditText) findViewById(R.id.input_info);

        inputTitle.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        validator = new NoteInputValidator(this, inputTitleLayout);

        tagsAdapter = app.getTagsAdapter();
        newTagColor = "#" + Integer.toHexString(app.getResources().getColor(R.color.tagColorNull));

        String id = databaseManager.getNotesRef().push().getKey();
        newNote = new Note(id);
        newTags = new ArrayList<>();

        if(app.isVIPUser()) {
            vipImagesRecyclerView = (RecyclerView) findViewById(R.id.vip_images_list);
            vipImagesAdapter = new VipImagesAdapter(newNote.getId(), false);

            vipImagesRecyclerView.setAdapter(vipImagesAdapter);
            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            vipImagesRecyclerView.setLayoutManager(layoutManager);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_note, menu);

        // Show Menu-group with VIP features.
        menu.setGroupEnabled(R.id.vipMenuGroup, app.isVIPUser());
        menu.setGroupVisible(R.id.vipMenuGroup, app.isVIPUser());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showDiscardChangesDialog();
                return true;
            case R.id.addTag:
                showAddNewTagDialog();
                return true;
            case R.id.noteDone:
                if(validator.formIsValid()) {
                    addNote();
                }
                return true;
            case R.id.starNote:
                newNote.setStared(!newNote.isStared());
                if(newNote.isStared()) {
                    item.setIcon(R.drawable.ic_star_enabled_accent);
                    item.setTitle(R.string.menu_note_star_disable);
                } else {
                    item.setIcon(R.drawable.ic_star_disabled_accent);
                    item.setTitle(R.string.menu_note_star_enable);
                }
                return true;
            case R.id.vipAddImage:
                // *** VIP feature *** //
                chooseImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    private void showDiscardChangesDialog() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context)
                        .setMessage(R.string.dialog_discard_changes_msg)
                        .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                onBackPressed();
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
        View addDialogView = getLayoutInflater().inflate(R.layout.dialog_add_tag, null, false);

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
        existingTagsListView.setAdapter(app.getTagsAdapter());

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

        tagsAdapter.setCurrentNote(newNote);

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

                newNote.addTag(newTag.getId());

                renderTagList();

                // Reset
                if(labelEditText.getText() != null) labelEditText.getText().clear();
                newTagColor = "#" + Integer.toHexString(getResources().getColor(R.color.tagColorNull));
                uncheckAllOtherTags();

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

    private void renderTagList() {
        tagsList.removeAllViews();
        for(String tagId: newNote.getTags().keySet()) {
            View tagView = LayoutInflater.from(context).inflate(R.layout.item_tag, tagsList, false);
            TagViewHolder tagViewHolder = new TagViewHolder(tagView);
            tagViewHolder.bindModelToView(Improve.getInstance().getTagsAdapter().getTag(tagId));
            tagsList.addView(tagView);
        }
    }

    public void addNote(){
        String title = inputTitle.getText() == null ? "" : inputTitle.getText().toString();
        String info = inputInfo.getText() == null ? "" : inputInfo.getText().toString();
        String timestampAdded = Long.toString(System.currentTimeMillis());

        if(TextUtils.isEmpty(info)) {
            info = "";
        }

        newNote.setTitle(title);
        newNote.setInfo(info);
        newNote.setArchived(false);
        newNote.setAdded(timestampAdded);
        newNote.setUpdated(timestampAdded);

        if (app.isVIPUser() && vipImagesAdapter.getItemCount() > 0) {
            // Wait on adding new note and returning to parent activity
            // until all image uploads has been successfully completed.
            uploadImages();
        } else {
            databaseManager.addNote(newNote);
            showParentActivity();
        }
    }

    private void uploadImages() {
        Log.d(TAG, "Uploading " + vipImagesAdapter.getItemCount() + " image(s)");

        String progressDialogText = "Saving attached image(s)";

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(progressDialogText);
        progressDialog.show();

        progressDialog.setMessage("Uploading " + vipImagesAdapter.getItemCount() + " image(s)...");

        app.getFirebaseStorageManager().uploadMultipleImages(newNote.getId(), vipImagesAdapter.getList(), new FirebaseStorageCallback() {
            @Override
            public void onSuccess(Object object) {
                Log.d(TAG, "Last image uploaded successfully!");

                ArrayList<VipImage> uploadedImages = (ArrayList<VipImage>) object;

                for(VipImage vipImage: uploadedImages) {
                    newNote.addVipImage(vipImage);
                }

                progressDialog.dismiss();
                databaseManager.addNote(newNote);
                showParentActivity();
            }

            @Override
            public void onFailure(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(app, "Failed to upload images!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProgress(int progress) {}
        });
    }

    private void showParentActivity() {
        restUI();
        startActivity(new Intent(this, MainActivity.class));
        finishAfterTransition();
    }

    private void restUI(){
        if(inputTitle.getText() != null) inputTitle.getText().clear();
        if(inputInfo.getText() != null) inputInfo.getText().clear();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {

            if(data.getClipData() != null) {
                // User selected multiple images.

                int numberOfImagesSelected = data.getClipData().getItemCount();

                Log.d(TAG, "Multiple (" + numberOfImagesSelected + ") images selected.");

                for (int i = 0; i < numberOfImagesSelected; i++) {
                    String filePath = data.getClipData().getItemAt(i).getUri().toString();
                    String imageId = Long.toString(System.currentTimeMillis());

                    VipImage vipImage = new VipImage(imageId, filePath);
                    vipImage.setOriginalFilePath(filePath);

                    vipImagesAdapter.add(vipImage);
                    vipImagesAdapter.setEditMode(true);
                    vipImagesRecyclerView.setVisibility(View.VISIBLE);
                }

            } else if (data.getData() != null) {
                // User selected a single image.

                Log.d(TAG, "1 image selected.");

                String filePath = data.getData().toString();
                String imageId = Long.toString(System.currentTimeMillis());

                VipImage vipImage = new VipImage(imageId, filePath);
                vipImage.setOriginalFilePath(filePath);

                vipImagesAdapter.add(vipImage);
                vipImagesAdapter.setEditMode(true);
                vipImagesRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        showParentActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Re-add newly created (and therefore added) Tags from Note.
        for (String tagId : newTags) {
            newNote.addTag(tagId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Remove newly created (and therefore added) Tags from Note.
        for(String tagId: newTags) {
            newNote.removeTag(tagId);
        }
    }

}
