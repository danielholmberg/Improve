package dev.danielholmberg.improve.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

import dev.danielholmberg.improve.Callbacks.FirebaseDatabaseCallback;
import dev.danielholmberg.improve.Models.Feedback;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.DatabaseManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.NoteInputValidator;

/**
 * Created by DanielHolmberg on 2018-01-27.
 */

public class SubmitFeedbackActivity extends AppCompatActivity {
    private static final String TAG = SubmitFeedbackActivity.class.getSimpleName();

    private Improve app;
    private DatabaseManager storageManager;
    private NoteInputValidator validator;

    private TextInputEditText inputTitle, inputInfo;

    private Toolbar toolbar;
    private View inputLayout;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_feedback);

        app = Improve.getInstance();
        storageManager = app.getDatabaseManager();

        toolbar = (Toolbar) findViewById(R.id.toolbar_submit_feedback);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inputLayout = (View) findViewById(R.id.input_layout);
        inputTitle = (TextInputEditText) findViewById(R.id.input_title);
        inputInfo = (TextInputEditText) findViewById(R.id.input_info);

        fab = (FloatingActionButton) findViewById(R.id.submit_feedback);

        inputTitle.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        validator = new NoteInputValidator(this, inputLayout);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validator.formIsValid()) {
                    submitFeedback();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void submitFeedback(){
        String user_id = Improve.getInstance().getAuthManager().getCurrentUserId();
        String feedback_id = storageManager.getFeedbackRef().push().getKey();
        String title = inputTitle.getText().toString();
        String info = inputInfo.getText().toString();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        String timestamp = DateFormat.getDateTimeInstance().format(calendar.getTime());

        Feedback feedback = new Feedback(user_id, feedback_id, title, info, timestamp);

        storageManager.submitFeedback(feedback, new FirebaseDatabaseCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(app, "Feedback submitted, you're awesome!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(app, "Failed to submit feedback, please try again", Toast.LENGTH_SHORT).show();
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
    public void onBackPressed() {
        showParentActivity();
    }
}
