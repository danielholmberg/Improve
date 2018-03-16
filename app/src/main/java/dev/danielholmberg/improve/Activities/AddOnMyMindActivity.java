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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Date;

import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.OnMyMindInputValidator;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class AddOnMyMindActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AddOnMyMindActivity.class.getSimpleName();
    public static final int OMM_ADDED = 9998;
    public static final int OMM_UPDATED = 9999;

    private Improve app;
    private FirebaseStorageManager storageManager;
    private OnMyMindInputValidator validator;

    private EditText inputTitle, inputInfo;
    private TextInputLayout inputLayoutTitle, inputLayoutInfo;

    private FirebaseFirestore firestoreDB;
    private Toolbar toolbar;
    private View contentLayout;

    private Bundle onMyMindBundle;
    private String userId;
    private boolean isEdit;
    private String oldId, oldTitle, oldColor, oldInfo, oldCreatedTimestamp, oldUpdatedTimestamp;
    private int ommPosition;
    private AlertDialog colorPickerDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_omm);

        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();
        userId = app.getAuthManager().getCurrentUserId();

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_add_omm);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Main Layout
        contentLayout = (View) findViewById(R.id.add_omm_layout);

        // Input Components
        inputLayoutTitle = (TextInputLayout) findViewById(R.id.input_layout_title);
        inputLayoutInfo = (TextInputLayout) findViewById(R.id.input_layout_info);
        inputTitle = (TextInputEditText) findViewById(R.id.input_title);
        inputInfo = (TextInputEditText) findViewById(R.id.input_info);

        // Incoming OnMyMind
        onMyMindBundle = getIntent().getBundleExtra("onMyMindBundle");
        OnMyMind onMyMind = onMyMindBundle != null ? (OnMyMind) onMyMindBundle.getSerializable("onMyMind") : null;

        if(onMyMind != null){
            isEdit = true;
            ((TextView) findViewById(R.id.toolbar_add_omm_title_tv)).setText(R.string.title_edit_onmymind);

            oldId = onMyMind.getId();
            oldTitle = onMyMind.getTitle();
            oldInfo = onMyMind.getInfo();
            oldColor = onMyMind.getColor();
            oldCreatedTimestamp = onMyMind.getCreatedTimestamp();
            if(onMyMind.getUpdatedTimestamp() != null) {
                if (!onMyMind.getUpdatedTimestamp().isEmpty()) {
                    oldUpdatedTimestamp = onMyMind.getUpdatedTimestamp();
                }
            }
            ommPosition = onMyMindBundle.getInt("position");

            toolbar.setBackgroundColor(Color.parseColor(oldColor));
            inputTitle.setText(oldTitle);
            inputInfo.setText(oldInfo);
        }

        // Initialize the OnMyMind InputValidator
        validator = new OnMyMindInputValidator(this, contentLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_edit_omm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.chooseBackgroundColor:
                chooseBackgroundColor();
                return true;
            case R.id.ommDone:
                if(validator.formIsValid()) {
                    if(!isEdit) {
                        addOnMyMind();
                    } else {
                        updateOnMyMind();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void chooseBackgroundColor() {
        LinearLayout colorPickerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.color_picker, null, false);
        colorPickerLayout.findViewById(R.id.buttonColorOrange).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorYellow).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorBlue).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorPink).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorGrey).setOnClickListener(this);

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this).setTitle("Choose a color")
                        .setMessage("Assign a specific color to your OnMyMind")
                        .setCancelable(true)
                        .setView(colorPickerLayout);
        colorPickerDialog = alertDialogBuilder.create();
        colorPickerDialog.show();
    }

    public void addOnMyMind(){
        String id = storageManager.getOnMyMindsRef().push().getKey();
        String title = ((TextView) findViewById(R.id.input_title)).getText().toString();
        String info = ((TextView) findViewById(R.id.input_info)).getText().toString();
        String color = "#" + Integer.toHexString(((ColorDrawable) toolbar.getBackground()).getColor());
        String createdTimestamp = getCurrentTimestamp();

        OnMyMind newOnMymind = new OnMyMind(id, title, info, color, createdTimestamp);
        storageManager.writeOnMyMindToFirebase(newOnMymind);
        showParentActivity();
    }

    public void updateOnMyMind(){
        String id = oldId;
        String title = ((TextView) findViewById(R.id.input_title)).getText().toString();
        String info = ((TextView) findViewById(R.id.input_info)).getText().toString();
        String color = "#" + Integer.toHexString(((ColorDrawable) toolbar.getBackground()).getColor());
        String createdTimestamp = oldCreatedTimestamp;
        String updatedTimestamp = getCurrentTimestamp();

        OnMyMind updatedOnMyMind = new OnMyMind(id, title, info, color, createdTimestamp, updatedTimestamp);
        storageManager.writeOnMyMindToFirebase(updatedOnMyMind);
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
            case R.id.buttonColorOrange:
                Log.d(TAG, "Orange was chosen");
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                break;
            case R.id.buttonColorYellow:
                Log.d(TAG, "Yellow was chosen");
                toolbar.setBackgroundColor(getResources().getColor(R.color.ommYellow));
                break;
            case R.id.buttonColorBlue:
                Log.d(TAG, "Blue was chosen");
                toolbar.setBackgroundColor(getResources().getColor(R.color.ommBlue));
                break;
            case R.id.buttonColorPink:
                Log.d(TAG, "Pink was chosen");
                toolbar.setBackgroundColor(getResources().getColor(R.color.ommPink));
                break;
            case R.id.buttonColorGrey:
                Log.d(TAG, "Brown was chosen");
                toolbar.setBackgroundColor(getResources().getColor(R.color.ommGrey));
                break;
            default:
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                break;
        }
        colorPickerDialog.dismiss();
    }
}
