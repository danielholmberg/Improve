package dev.danielholmberg.improve.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import dev.danielholmberg.improve.Activities.AddOnMyMindActivity;
import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;

/**
 * Created by Daniel Holmberg.
 */

public class OnMyMindDetailsDialogFragment extends DialogFragment implements View.OnClickListener{
    private static final String TAG = OnMyMindDetailsDialogFragment.class.getSimpleName();

    private Improve app;
    private DialogFragment detailsDialog;
    private FirebaseStorageManager storageManager;

    private View view;
    private View parentLayout;

    private OnMyMind onMyMind;
    private Bundle onMyMindBundle;
    private int position;
    private boolean isDone = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();

        detailsDialog = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_omm_details, container, false);
        parentLayout = (View) container;

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_omm_details);
        View layout = (View) view.findViewById(R.id.omm_details_container);

        ((Button) view.findViewById(R.id.close_details_btn)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.update_omm_btn)).setOnClickListener(this);
        Button done = (Button) view.findViewById(R.id.done_omm_btn);
        done.setOnClickListener(this);

        TextView title = (TextView) view.findViewById(R.id.omm_details_title_tv);
        TextView info = (TextView) view.findViewById(R.id.omm_details_info_tv);
        TextView created = (TextView) view.findViewById(R.id.footer_omm_created_tv);
        TextView updated = (TextView) view.findViewById(R.id.footer_omm_updated_tv);
        TextView updatedLabel = (TextView) view.findViewById(R.id.footer_omm_updated_label_tv);
        
        onMyMindBundle =  this.getArguments();

        if(onMyMindBundle != null){
            onMyMind = (OnMyMind) onMyMindBundle.getSerializable("onMyMind");
            position = onMyMindBundle.getInt("position");
            isDone = onMyMindBundle.getBoolean("isDone");
        }

        if(onMyMind != null) {
            title.setText(onMyMind.getTitle());
            info.setText(onMyMind.getInfo());
            created.setText(onMyMind.getCreatedTimestamp());
            if(onMyMind.getUpdatedTimestamp() != null) {
                if(!onMyMind.getUpdatedTimestamp().isEmpty()) {
                    updated.setText(onMyMind.getUpdatedTimestamp());
                    updated.setVisibility(View.VISIBLE);
                    updatedLabel.setVisibility(View.VISIBLE);
                }
            }
            if(isDone) {
                // Change the OnMyMind Detail layout
                done.setBackground(getResources().getDrawable(R.drawable.ic_menu_delete_white));
                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Delete OnMyMind
                        AlertDialog.Builder alertDialogBuilder =
                                new AlertDialog.Builder(getContext()).setTitle("Delete OnMyMind")
                                        .setMessage("Do you really want to delete: " + onMyMind.getTitle())
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                deleteOnMyMind(onMyMind);
                                            }
                                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        final AlertDialog dialog = alertDialogBuilder.create();
                        dialog.show();
                    }
                });
                toolbar.setBackgroundColor(getResources().getColor(R.color.doneBackground));
                layout.setBackgroundColor(getResources().getColor(R.color.doneBackground));
                title.setTextColor(getResources().getColor(R.color.doneTextColor));
                info.setTextColor(getResources().getColor(R.color.doneTextColor));
                ((TextView) view.findViewById(R.id.footer_omm_created_label_tv))
                        .setTextColor(getResources().getColor(R.color.doneTextColor));
                created.setTextColor(getResources().getColor(R.color.doneTextColor));
                updated.setTextColor(getResources().getColor(R.color.doneTextColor));
            } else {
                toolbar.setBackgroundColor(Color.parseColor(onMyMind.getColor()));
            }
        } else {
            // Dismiss dialog and show Toast.
            this.dismiss();
            Toast.makeText(getActivity(), "Unable to show OnMyMind details", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void deleteOnMyMind(OnMyMind onMyMind) {
        storageManager.deleteOnMymind(onMyMind);
        detailsDialog.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.close_details_btn:
                detailsDialog.dismiss();
                break;
            case R.id.update_omm_btn:
                Intent updateOnMyMind = new Intent(getContext(), AddOnMyMindActivity.class);
                updateOnMyMind.putExtra("onMyMindBundle", onMyMindBundle);
                startActivity(updateOnMyMind);
                break;
            case R.id.done_omm_btn:
                onMyMind.setIsDone(true);
                storageManager.writeOnMyMindToFirebase(onMyMind);
                detailsDialog.dismiss();
                break;
            default:
                break;
        }
    }
}
