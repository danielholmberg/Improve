package dev.danielholmberg.improve.Activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
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

    private EditText inputTitle, inputInfo;
    private TextInputLayout inputLayoutTitle, inputLayoutInfo;

    private FirebaseFirestore firestoreDB;
    private Toolbar toolbar;
    private View contentLayout;

    private Bundle noteBundle;
    private String userId;
    private boolean isEdit;
    private String oldId, oldTitle, oldColor, oldInfo, oldTimestamp;
    private int notePosition;
    private AlertDialog colorPickerDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();
        userId = app.getAuthManager().getCurrentUserId();

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_add_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Main Layout
        contentLayout = (View) findViewById(R.id.add_note_layout);

        // Input Components
        inputLayoutTitle = (TextInputLayout) findViewById(R.id.input_layout_title);
        inputLayoutInfo = (TextInputLayout) findViewById(R.id.input_layout_info);
        inputTitle = (TextInputEditText) findViewById(R.id.input_title);
        inputInfo = (TextInputEditText) findViewById(R.id.input_info);

        // Incoming Note
        noteBundle = getIntent().getBundleExtra("noteBundle");
        Note note = noteBundle != null ? (Note) noteBundle.getSerializable("note") : null;

        if(note != null){
            isEdit = true;
            ((TextView) findViewById(R.id.toolbar_add_note_title_tv)).setText(R.string.title_edit_note);

            oldId = note.getId();
            oldTitle = note.getTitle();
            oldInfo = note.getInfo();
            oldColor = note.getColor();
            oldTimestamp = note.getTimestamp();

            notePosition = noteBundle.getInt("position");

            toolbar.setBackgroundColor(oldColor != null ? Color.parseColor(oldColor) :
                    getResources().getColor(R.color.colorPickerDeepOrange));
            inputTitle.setText(oldTitle);
            inputInfo.setText(oldInfo);
        }

        // Initialize the Note InputValidator
        validator = new NoteInputValidator(this, contentLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_edit_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.chooseBackgroundColor:
                chooseBackgroundColor();
                return true;
            case R.id.noteDone:
                if(validator.formIsValid()) {
                    if(!isEdit) {
                        addNote();
                    } else {
                        updateNote();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void chooseBackgroundColor() {
        LinearLayout colorPickerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.color_picker, null, false);

        // First row
        colorPickerLayout.findViewById(R.id.buttonColorGreen).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorLightGreen).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorAmber).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorDeepOrange).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorBrown).setOnClickListener(this);

        // Second row
        colorPickerLayout.findViewById(R.id.buttonColorBlueGrey).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorRed).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorPink).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorDeepPurple).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorIndigo).setOnClickListener(this);

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this).setTitle("Choose a color")
                        .setMessage("Assign a specific color to your Note")
                        .setCancelable(true)
                        .setView(colorPickerLayout);
        colorPickerDialog = alertDialogBuilder.create();
        colorPickerDialog.show();
    }

    public void addNote(){
        String id = storageManager.getNotesRef().push().getKey();
        String title = ((TextView) findViewById(R.id.input_title)).getText().toString();
        String info = ((TextView) findViewById(R.id.input_info)).getText().toString();
        String color = "#" + Integer.toHexString(((ColorDrawable) toolbar.getBackground()).getColor());
        String timestamp = getCurrentTimestamp();

        Note newNote = new Note(id, title, info, color, timestamp);
        storageManager.writeNoteToFirebase(newNote, false, new FirebaseStorageCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(app, "Added new note", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(app, "Failed to add new note", Toast.LENGTH_SHORT).show();
            }
        });
        showParentActivity();
    }

    public void updateNote(){
        String id = oldId;
        String title = ((TextView) findViewById(R.id.input_title)).getText().toString();
        String info = ((TextView) findViewById(R.id.input_info)).getText().toString();
        String color = "#" + Integer.toHexString(((ColorDrawable) toolbar.getBackground()).getColor());
        String updatedTimestamp = getCurrentTimestamp();

        Note updatedNote = new Note(id, title, info, color, updatedTimestamp);
        storageManager.writeNoteToFirebase(updatedNote, false, new FirebaseStorageCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(app, "Updated note", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(app, "Failed to update note", Toast.LENGTH_SHORT).show();
            }
        });
        showParentActivity();
    }

    private String getCurrentTimestamp() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    private void showParentActivity() {
        restUI();
        NavUtils.navigateUpFromSameTask(this);
        finish();
    }

    private void restUI(){
        inputTitle.getText().clear();
        inputInfo.getText().clear();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonColorGreen:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerGreen));
                break;
            case R.id.buttonColorLightGreen:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerLightGreen));
                break;
            case R.id.buttonColorAmber:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerAmber));
                break;
            case R.id.buttonColorDeepOrange:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerDeepOrange));
                break;
            case R.id.buttonColorBrown:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerBrown));
                break;
            case R.id.buttonColorBlueGrey:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerBlueGrey));
                break;
            case R.id.buttonColorRed:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerTurquoise));
                break;
            case R.id.buttonColorPink:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerPink));
                break;
            case R.id.buttonColorDeepPurple:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerDeepPurple));
                break;
            case R.id.buttonColorIndigo:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerIndigo));
                break;
            default:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPickerDeepOrange));
                break;
        }
        colorPickerDialog.dismiss();
    }
}
