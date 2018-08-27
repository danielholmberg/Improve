package dev.danielholmberg.improve.Activities;

import android.graphics.Color;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    private LinearLayout marker;
    private int markerColor;
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

        marker = (LinearLayout) findViewById(R.id.include_item_marker);
        markerColor = getResources().getColor(R.color.colorPickerDeepOrange);
        ((GradientDrawable) marker.getBackground()).setColor(markerColor);

        inputLayout = (View) findViewById(R.id.input_layout);
        inputTitle = (TextInputEditText) findViewById(R.id.input_title);
        inputInfo = (TextInputEditText) findViewById(R.id.input_info);

        inputTitle.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        validator = new NoteInputValidator(this, inputLayout);

        marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseMarkerColor();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_note_mode_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
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
                new AlertDialog.Builder(this).setTitle("Note color")
                        .setMessage("Choose a color to more easily characterize Notes")
                        .setCancelable(true)
                        .setView(colorPickerLayout);
        colorPickerDialog = alertDialogBuilder.create();
        colorPickerDialog.show();
    }

    public void addNote(){
        String id = storageManager.getNotesRef().push().getKey();
        String title = inputTitle.getText().toString();
        String info = inputInfo.getText().toString();
        String color = "#" + Integer.toHexString(markerColor);
        String timestamp = Long.toString(System.currentTimeMillis());

        if(TextUtils.isEmpty(info)) {
            info = "";
        }

        Note newNote = new Note(id, title, info, color, timestamp);

        storageManager.writeNoteToFirebase(newNote, false, new FirebaseStorageCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(app, "New note added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(app, "Failed to add new note, please try again", Toast.LENGTH_SHORT).show();
            }
        });

        showParentActivity();

    }

    private void showParentActivity() {
        restUI();
        NavUtils.navigateUpFromSameTask(this);
        finishAfterTransition();
    }

    private void restUI(){
        inputTitle.getText().clear();
        inputInfo.getText().clear();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
