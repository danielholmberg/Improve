package dev.danielholmberg.improve.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class AddOnMyMindActivity extends AppCompatActivity {
    private static final String TAG = AddOnMyMindActivity.class.getSimpleName();
    public static final int OMM_ADDED = 9998;
    public static final int OMM_UPDATED = 9999;

    private List<OnMyMind> storedOnMyMinds;

    private EditText inputTitle, inputInfo;
    private TextInputLayout inputLayoutTitle, inputLayoutInfo;

    private FirebaseFirestore firestoreDB;
    private boolean isEdit;

    private String oldId, oldTitle, oldInfo;
    private int ommPosition;

    private boolean ommAdded = false;
    private boolean ommUpdated = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_omm);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_omm);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.omm_done);

        inputLayoutTitle = (TextInputLayout) findViewById(R.id.input_layout_title);
        inputLayoutInfo = (TextInputLayout) findViewById(R.id.input_layout_info);
        inputTitle = (EditText) findViewById(R.id.input_title);
        inputInfo = (EditText) findViewById(R.id.input_info);

        OnMyMind omm = null;
        Bundle extras = getIntent().getBundleExtra("onmymind");
        if(extras != null){
            omm = new OnMyMind();
            omm.setId(extras.getString("id"));
            omm.setTitle(extras.getString("title"));
            omm.setInfo(extras.getString("info"));
        }
        if(omm != null){
            Log.d(TAG, "OnMyMind is not null");
            ((TextView) findViewById(R.id.input_title)).setText(omm.getTitle());
            ((TextView) findViewById(R.id.input_info)).setText(omm.getInfo());

            ((TextView) findViewById(R.id.toolbar_add_omm_title)).setText(R.string.title_edit_onmymind);
            isEdit = true;
            oldId = omm.getId();
            oldTitle = omm.getTitle();
            oldInfo = omm.getInfo();
            ommPosition = extras.getInt("position");
        }

        firestoreDB = FirebaseFirestore.getInstance();

        try {
            storedOnMyMinds = (ArrayList<OnMyMind>) InternalStorage.readObject(getApplicationContext(),
                    InternalStorage.ONMYMINDS_STORAGE_KEY);
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
        addDocumentToCollection(omm);
    }

    public void updateOnMyMind(){
        OnMyMind omm = createOnMyMindObj();
        updateDocumentToCollection(omm);
    }

    private OnMyMind createOnMyMindObj(){
        final OnMyMind omm = new OnMyMind();
        omm.setTitle(((TextView) findViewById(R.id.input_title)).getText().toString());
        omm.setInfo(((TextView) findViewById(R.id.input_info)).getText().toString());

        return omm;
    }

    /**
     * Add a new OnMyMind to the Firestore Database.
     * @param omm
     */
    private void addDocumentToCollection(final OnMyMind omm) {
        omm.setId(UUID.randomUUID().toString());
        Log.d(TAG, "Id: " + omm.getId());
        storedOnMyMinds.add(omm);
        try {
            InternalStorage.writeObject(getApplicationContext(), InternalStorage.ONMYMINDS_STORAGE_KEY,
                    storedOnMyMinds);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to Internal Storage: ");
            e.printStackTrace();
        }
        firestoreDB.collection("onmyminds").document(omm.getId())
                .set(omm)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        Log.d(TAG, "OnMyMind document added - id: "
                                + omm.getId());
                        ommAdded = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding OnMyMind document: " + e);
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
    private void updateDocumentToCollection(final OnMyMind updatedOnMyMind) {
        updatedOnMyMind.setId(oldId);
        storedOnMyMinds.set(ommPosition, updatedOnMyMind);
        try {
            InternalStorage.writeObject(getApplicationContext(), InternalStorage.ONMYMINDS_STORAGE_KEY,
                    storedOnMyMinds);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to Internal Storage: ");
            e.printStackTrace();
        }
        Log.d(TAG, "oldId: " + oldId);
        firestoreDB.collection("onmyminds").document(oldId)
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
}
