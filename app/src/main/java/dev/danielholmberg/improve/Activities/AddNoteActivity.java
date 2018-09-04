package dev.danielholmberg.improve.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.NoteInputValidator;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class AddNoteActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AddNoteActivity.class.getSimpleName();

    private Improve app;
    private FirebaseStorageManager storageManager;
    private NoteInputValidator validator;

    private int markerColor;
    private Drawable colorPaletteIcon;
    private TextInputEditText inputTitle, inputInfo;

    private Toolbar toolbar;
    private View inputLayout;

    private AlertDialog colorPickerDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();

        toolbar = (Toolbar) findViewById(R.id.toolbar_add_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        markerColor = getResources().getColor(R.color.colorPickerDeepOrange);

        inputLayout = (View) findViewById(R.id.input_layout);
        inputTitle = (TextInputEditText) findViewById(R.id.input_title);
        inputInfo = (TextInputEditText) findViewById(R.id.input_info);

        inputTitle.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        validator = new NoteInputValidator(this, inputLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_note_details_edit, menu);
        colorPaletteIcon = menu.findItem(R.id.chooseMarkerColor).getIcon();
        colorPaletteIcon.setTint(markerColor);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.chooseMarkerColor:
                chooseMarkerColor();
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
                new AlertDialog.Builder(this).setTitle(R.string.choose_marker_color_note_title)
                        .setMessage(R.string.choose_marker_color_note_msg)
                        .setCancelable(true)
                        .setView(colorPickerLayout).setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        colorPickerDialog = alertDialogBuilder.create();
        colorPickerDialog.show();
    }

    public void addNote(){
        String id = storageManager.getNotesRef().push().getKey();
        String title = inputTitle.getText().toString();
        String info = inputInfo.getText().toString();
        String color = "#" + Integer.toHexString(markerColor);
        String timestampAdded = Long.toString(System.currentTimeMillis());

        if(TextUtils.isEmpty(info)) {
            info = "";
        }

        Note newNote = new Note(id, title, info, color, timestampAdded);
        newNote.setTimestampUpdated(timestampAdded);

        storageManager.writeNoteToFirebase(newNote, false, new FirebaseStorageCallback() {

            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(app, "Failed to add new note, please try again", Toast.LENGTH_SHORT).show();
            }
        });

        showParentActivity();

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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonColorGreen:
                markerColor = getResources().getColor(R.color.colorPickerGreen);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorLightGreen:
                markerColor = getResources().getColor(R.color.colorPickerLightGreen);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorAmber:
                markerColor = getResources().getColor(R.color.colorPickerAmber);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorDeepOrange:
                markerColor = getResources().getColor(R.color.colorPickerDeepOrange);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorBrown:
                markerColor = getResources().getColor(R.color.colorPickerBrown);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorBlueGrey:
                markerColor = getResources().getColor(R.color.colorPickerBlueGrey);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorTurquoise:
                markerColor = getResources().getColor(R.color.colorPickerTurquoise);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorPink:
                markerColor = getResources().getColor(R.color.colorPickerPink);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorDeepPurple:
                markerColor = getResources().getColor(R.color.colorPickerDeepPurple);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorDarkGrey:
                markerColor = getResources().getColor(R.color.colorPickerDarkGrey);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorRed:
                markerColor = getResources().getColor(R.color.colorPickerRed);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorPurple:
                markerColor = getResources().getColor(R.color.colorPickerPurple);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorBlue:
                markerColor = getResources().getColor(R.color.colorPickerBlue);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorDarkOrange:
                markerColor = getResources().getColor(R.color.colorPickerDarkOrange);
                colorPaletteIcon.setTint(markerColor);
                break;
            case R.id.buttonColorBabyBlue:
                markerColor = getResources().getColor(R.color.colorPickerBabyBlue);
                colorPaletteIcon.setTint(markerColor);
                break;
            default:
                markerColor = getResources().getColor(R.color.colorPickerDeepOrange);
                colorPaletteIcon.setTint(markerColor);
                break;
        }
        colorPickerDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        showParentActivity();
    }
}
