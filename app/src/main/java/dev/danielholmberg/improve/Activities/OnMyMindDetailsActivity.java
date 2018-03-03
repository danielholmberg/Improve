package dev.danielholmberg.improve.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.R;

/**
 * Class ${CLASS}
 */

public class OnMyMindDetailsActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omm_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_omm_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View container = (View) findViewById(R.id.omm_details_container);
        TextView title = (TextView) findViewById(R.id.toolbar_omm_details_title_tv);
        TextView info = (TextView) findViewById(R.id.omm_details_info_tv);

        OnMyMind omm = null;
        Bundle extras = getIntent().getBundleExtra("onmymind");

        if(extras != null){
            omm = new OnMyMind();
            omm.setId(extras.getString("id"));
            omm.setTitle(extras.getString("title"));
            omm.setInfo(extras.getString("info"));
            omm.setColor(extras.getString("color"));
        }
        if(omm != null) {
            title.setText(omm.getTitle());
            container.setBackgroundColor(Color.parseColor(omm.getColor()));
            info.setText(omm.getInfo());
        }

        // TODO - Add actions like deleting the OnMyMind in the DetailActivity (maybe even share it?)

    }
}
