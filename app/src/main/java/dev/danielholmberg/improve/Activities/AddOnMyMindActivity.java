package dev.danielholmberg.improve.Activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class AddOnMyMindActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AddOnMyMindActivity.class.getSimpleName();
    public static final int OMM_ADDED = 9998;
    public static final int OMM_UPDATED = 9999;

    private List<OnMyMind> storedOnMyMinds;

    private EditText inputTitle, inputInfo;
    private TextInputLayout inputLayoutTitle, inputLayoutInfo;

    private FirebaseFirestore firestoreDB;
    private ConstraintLayout contentLayout;

    private String userId;
    private boolean isEdit;
    private String oldId, oldTitle, oldInfo;
    private int ommPosition;
    private boolean ommAdded = false;
    private boolean ommUpdated = false;
    private String oldColor;
    private AlertDialog colorPickerDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_omm);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_omm);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contentLayout = (ConstraintLayout) findViewById(R.id.add_omm_content);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.omm_done);

        inputLayoutTitle = (TextInputLayout) findViewById(R.id.input_layout_title);
        inputLayoutInfo = (TextInputLayout) findViewById(R.id.input_layout_info);
        inputTitle = (EditText) findViewById(R.id.input_title);
        inputInfo = (EditText) findViewById(R.id.input_info);

        OnMyMind omm = null;
        Bundle extras = getIntent().getBundleExtra("onmymind");

        // Get current userId.
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(extras != null){
            omm = new OnMyMind();
            omm.setId(extras.getString("id"));
            omm.setTitle(extras.getString("title"));
            omm.setInfo(extras.getString("info"));
            omm.setColor(extras.getString("color"));
        }
        if(omm != null){
            Log.d(TAG, "OnMyMind is not null - Entering Edit Mode...");
            isEdit = true;
            ((TextView) findViewById(R.id.toolbar_add_omm_title_tv)).setText(R.string.title_edit_onmymind);

            oldId = omm.getId();
            oldTitle = omm.getTitle();
            oldInfo = omm.getInfo();
            oldColor = omm.getColor();
            ommPosition = extras.getInt("position");

            contentLayout.setBackgroundColor(Color.parseColor(oldColor));
            ((TextView) findViewById(R.id.input_title)).setText(oldTitle);
            ((TextView) findViewById(R.id.input_info)).setText(oldInfo);
        }

        firestoreDB = FirebaseFirestore.getInstance();

        try {
            storedOnMyMinds = (ArrayList<OnMyMind>) InternalStorage.readObject(InternalStorage.onmyminds);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read from Internal Storage: ");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(storedOnMyMinds.isEmpty()) {
            storedOnMyMinds = new ArrayList<>();
        }

        fab.setOnClickListener(new View.OnClickListener()        {
            @Override
            public void onClick(View v)
            {
                if(!isEdit){
                    if(validateTitle() && valitdateInfo()) {
                        addOnMyMind();
                    }
                } else {
                    updateOnMyMind();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_edit_omm_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.chooseBackgroundColor:
                chooseBackgroundColor();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void chooseBackgroundColor() {
        LinearLayout colorPickerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.color_picker, null, false);
        colorPickerLayout.findViewById(R.id.buttonColorWhite).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorYellow).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorBlue).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorPink).setOnClickListener(this);
        colorPickerLayout.findViewById(R.id.buttonColorBrown).setOnClickListener(this);

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this).setTitle("Choose a color")
                        .setMessage("Assign a specific color to your OnMyMind")
                        .setCancelable(true)
                        .setView(colorPickerLayout);
        colorPickerDialog = alertDialogBuilder.create();
        colorPickerDialog.show();
    }

    private boolean validateTitle() {
        if(TextUtils.isEmpty(inputTitle.getText())) {
            inputLayoutTitle.setError(getString(R.string.err_msg_title));
            requestFocus(inputTitle);
            return false;
        } else {
            return true;
        }
    }

    private boolean valitdateInfo() {
        if(TextUtils.isEmpty(inputInfo.getText())) {
            inputLayoutInfo.setError(getString(R.string.err_msg_info));
            requestFocus(inputInfo);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Requests focus of the incoming view.
     * @param view
     */
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public void addOnMyMind(){
        OnMyMind omm = createOnMyMindObj();
        addOnMyMind(omm);
    }

    public void updateOnMyMind(){
        OnMyMind omm = createOnMyMindObj();
        updateOnMyMind(omm);
    }

    private OnMyMind createOnMyMindObj(){
        final OnMyMind omm = new OnMyMind();
        omm.setTitle(((TextView) findViewById(R.id.input_title)).getText().toString());
        omm.setInfo(((TextView) findViewById(R.id.input_info)).getText().toString());
        omm.setColor("#"+Integer.toHexString(((ColorDrawable)contentLayout.getBackground()).getColor()));

        return omm;
    }

    /**
     * Add a new OnMyMind to the Firestore Database.
     * @param omm
     */
    private void addOnMyMind(final OnMyMind omm) {
        omm.setId(UUID.randomUUID().toString());
        Log.d(TAG, "Id: " + omm.getId());
        storedOnMyMinds.add(omm);
        try {
            InternalStorage.writeObject(InternalStorage.onmyminds, storedOnMyMinds);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to Internal Storage: ");
            e.printStackTrace();
        }

        firestoreDB.collection("users")
                .document(userId)
                .collection("onmyminds")
                .document(omm.getId())
                .set(omm)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        Log.d(TAG, "OnMyMind document added - id: "
                                + omm.getId());
                        ommAdded = true;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error adding OnMyMind document: " + e);
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                        "Adding new OnMyMind failed",
                        Toast.LENGTH_SHORT).show();
                    }
                });

        showMainActivity();
    }

    /**
     * Update a OnMyMind in Firestore Database
     * @param updatedOnMyMind
     */
    private void updateOnMyMind(final OnMyMind updatedOnMyMind) {
        updatedOnMyMind.setId(oldId);
        storedOnMyMinds.set(ommPosition, updatedOnMyMind);
        try {
            InternalStorage.writeObject(InternalStorage.onmyminds, storedOnMyMinds);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to Internal Storage: ");
            e.printStackTrace();
        }
        Log.d(TAG, "oldId: " + oldId);
        firestoreDB.collection("users")
                .document(userId)
                .collection("onmyminds")
                .document(oldId)
                .set(updatedOnMyMind, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, updatedOnMyMind.getTitle() + " updated successfully");
                        ommUpdated = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding OnMyMind document: " + e);
                        Toast.makeText(getApplicationContext(),
                                updatedOnMyMind.getTitle() + " document could not be added",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        showMainActivity();
    }

    private void showMainActivity() {
        restUi();
        if(ommAdded) {
            setResult(OMM_ADDED);
        } else if(ommUpdated){
            setResult(OMM_UPDATED);
        }
        NavUtils.navigateUpFromSameTask(this);
        finish();
    }

    private void restUi(){
        ((TextView) findViewById(R.id.input_title)).setText("");
        ((TextView) findViewById(R.id.input_info)).setText("");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonColorWhite:
                Log.d(TAG, "White was chosen");
                contentLayout.setBackgroundColor(getResources().getColor(R.color.cardBackgroundWhite));
                break;
            case R.id.buttonColorYellow:
                Log.d(TAG, "Yellow was chosen");
                contentLayout.setBackgroundColor(getResources().getColor(R.color.cardBackgroundYellow));
                break;
            case R.id.buttonColorBlue:
                Log.d(TAG, "Blue was chosen");
                contentLayout.setBackgroundColor(getResources().getColor(R.color.cardBackgroundBlue));
                break;
            case R.id.buttonColorPink:
                Log.d(TAG, "Pink was chosen");
                contentLayout.setBackgroundColor(getResources().getColor(R.color.cardBackgroundPink));
                break;
            case R.id.buttonColorBrown:
                Log.d(TAG, "Brown was chosen");
                contentLayout.setBackgroundColor(getResources().getColor(R.color.cardBackgroundBrown));
                break;
            default:
                // White
                contentLayout.setBackgroundColor(getResources().getColor(R.color.cardBackgroundWhite));
                break;
        }
        colorPickerDialog.dismiss();
    }
}
