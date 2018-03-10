package dev.danielholmberg.improve.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Activities.AddOnMyMindActivity;
import dev.danielholmberg.improve.Adapters.OnMyMindsRecyclerViewAdapter;
import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

/**
 * Created by Daniel Holmberg.
 */

public class OnMyMindDetailsDialogFragment extends DialogFragment {
    private static final String TAG = OnMyMindDetailsDialogFragment.class.getSimpleName();

    private DialogFragment detailsDialog;
    private FirebaseFirestore firestoreDB;

    private OnMyMind omm;
    private Bundle ommBundle;
    private int ommPos;

    private OnMyMindsRecyclerViewAdapter adapter;

    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        detailsDialog = this;
        firestoreDB = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_omm_details, container, false);

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_omm_details);
        View layout = (View) view.findViewById(R.id.omm_details_container);

        Button closeDetailsBtn = (Button) view.findViewById(R.id.close_details_btn);
        Button updateOnMyMindBtn = (Button) view.findViewById(R.id.update_omm_btn);
        Button deleteOnMyMindBtn = (Button) view.findViewById(R.id.delete_omm_btn);

        TextView title = (TextView) view.findViewById(R.id.omm_details_title_tv);
        TextView info = (TextView) view.findViewById(R.id.omm_details_info_tv);
        TextView created = (TextView) view.findViewById(R.id.footer_omm_created_tv);
        TextView updated = (TextView) view.findViewById(R.id.footer_omm_updated_tv);
        TextView updatedLabel = (TextView) view.findViewById(R.id.footer_omm_updated_label_tv);

        ommBundle =  this.getArguments();

        if(ommBundle != null){
            omm = (OnMyMind) ommBundle.getSerializable("omm");
            ommPos = ommBundle.getInt("position");
        }
        if(omm != null) {
            title.setText(omm.getTitle());
            toolbar.setBackgroundColor(Color.parseColor(omm.getColor()));
            info.setText(omm.getInfo());
            created.setText(omm.getCreatedTimestamp());
            if(omm.getUpdatedTimestamp() != null) {
                if(!omm.getUpdatedTimestamp().isEmpty()) {
                    updated.setText(omm.getUpdatedTimestamp());
                    updated.setVisibility(View.VISIBLE);
                    updatedLabel.setVisibility(View.VISIBLE);
                }
            }
        } else {
            // Dismiss dialog and show Toast.
            this.dismiss();
            Toast.makeText(getContext(), "Unable to show OnMyMind details", Toast.LENGTH_SHORT).show();
        }

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(getContext()).setTitle("Delete OnMyMind")
                        .setMessage("Do you really want to delete: " + omm.getTitle())
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteOnMyMind(omm, ommPos);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        final AlertDialog dialog = alertDialogBuilder.create();


        closeDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailsDialog.dismiss();
            }
        });
        updateOnMyMindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateOnMyMindMode(omm, ommPos);
            }
        });
        deleteOnMyMindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        return view;
    }

    private void updateOnMyMindMode(OnMyMind omm, int itemPos){
        Intent updateOnMyMind = new Intent(getContext(), AddOnMyMindActivity.class);
        updateOnMyMind.putExtra("ommBundle", ommBundle);
        startActivity(updateOnMyMind);
    }

    public void deleteOnMyMind(final OnMyMind omm, int ommPos){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String ommTitle = omm.getTitle();

        final View parentLayout = getActivity().getWindow().findViewById(R.id.omm_fragment_container);

        // Delete the OnMyMind from the recycler list.
        adapter.ommsList.remove(ommPos);
        if(adapter.ommsList.isEmpty()) {
            TextView emptyListText = (TextView) parentLayout.findViewById(R.id.empty_omms_list_tv);
            emptyListText.setVisibility(View.VISIBLE);
        }
        try {
            // Try to get the stored list of OnMyminds and remove the specified OnMyMind.
            List<OnMyMind> storedList = (ArrayList<OnMyMind>) InternalStorage.readObject(InternalStorage.onmyminds);
            storedList.remove(ommPos);
            InternalStorage.writeObject(InternalStorage.onmyminds, storedList);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read from Internal Storage: ");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        detailsDialog.dismiss();
        adapter.notifyItemRemoved(ommPos);
        adapter.notifyItemRangeChanged(ommPos, adapter.ommsList.size());
        // Delete the specified OnMyMind from Firestore Database.
        firestoreDB.collection("users")
                .document(userId)
                .collection("onmyminds")
                .document(omm.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "*** Deleted OnMyMind successfully ***");
                        // Deletion was successful.
                        Snackbar.make(parentLayout,
                                ommTitle + " has been deleted",
                                Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to delete OnMyMind - id: " + omm.getId());
                        e.printStackTrace();
                    }
                });
    }

    public void setAdapter(OnMyMindsRecyclerViewAdapter adapter) {
        this.adapter = adapter;
    }

}
