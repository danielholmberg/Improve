package dev.danielholmberg.improve.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class AddOnMyMindActivity extends AppCompatActivity {
    private static final String TAG = AddOnMyMindActivity.class.getSimpleName();

    private EditText inputTitle, inputInfo;
    private TextInputLayout inputLayoutTitle, inputLayoutInfo;

    private FirebaseFirestore firestoreDB;
    private boolean isEdit;

    private String ommId, oldTitle, oldInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_omm);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_omm);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.done);

        inputLayoutTitle = (TextInputLayout) findViewById(R.id.input_layout_first_name);
        inputLayoutInfo = (TextInputLayout) findViewById(R.id.input_layout_last_name);
        inputTitle = (EditText) findViewById(R.id.input_first_name);
        inputInfo = (EditText) findViewById(R.id.input_last_name);

        OnMyMind omm = null;
        Bundle extras = getIntent().getBundleExtra("onmymind");
        if(extras != null){
            omm = new OnMyMind();
            omm.setID(extras.getString("id"));
            omm.setTitle(extras.getString("title"));
            omm.setInfo(extras.getString("info"));
        }
        if(omm != null){
            Log.d(TAG, "OnMyMind is not null");
            ((TextView) findViewById(R.id.input_title)).setText(omm.getTitle());
            ((TextView) findViewById(R.id.input_info)).setText(omm.getInfo());

            ((TextView) findViewById(R.id.toolbar_add_omm_title)).setText(R.string.title_edit_onmymind);
            isEdit = true;
            ommId = omm.getId();
            oldTitle = omm.getTitle();
            oldInfo = omm.getInfo();
        }

        firestoreDB = FirebaseFirestore.getInstance();

        fab.setOnClickListener(new View.OnClickListener()        {
            @Override
            public void onClick(View v)
            {
                if(!isEdit){
                    addOnMyMind();
                } else {
                    updateOnMyMind();
                }
            }
        });
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
        firestoreDB.collection("onmyminds")
                .add(omm)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "OnMyMind document added - id: "
                                + documentReference.getId());
                        restUi();
                        showMainActivity();
                        Toast.makeText(getApplicationContext(),
                                "OnMyMind document has been added",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding OnMyMind document: " + e);
                        Toast.makeText(getApplicationContext(),
                                "OnMyMind document could not be added",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Update a OnMyMind in Firestore Database
     * @param updatedOnMyMind
     */
    private void updateDocumentToCollection(OnMyMind updatedOnMyMind) {
        firestoreDB.collection("onmyminds").document(ommId)
                .set(updatedOnMyMind, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "OnMyMind document updated ");
                        Toast.makeText(getApplicationContext(),
                                "OnMyMind document has been updated",
                                Toast.LENGTH_SHORT).show();
                        showMainActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding OnMyMind document: " + e);
                        Toast.makeText(getApplicationContext(),
                                "OnMyMind document could not be added",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showMainActivity() {
        NavUtils.navigateUpFromSameTask(this);
    }

    private void restUi(){
        ((TextView) findViewById(R.id.input_title)).setText("");
        ((TextView) findViewById(R.id.input_info)).setText("");
    }
}
