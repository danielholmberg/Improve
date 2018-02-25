package dev.danielholmberg.improve.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Activities.AddOnMyMindActivity;
import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-21.
 */

public class OnMyMindsRecyclerViewAdapter extends
        RecyclerView.Adapter<OnMyMindsRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = OnMyMindsRecyclerViewAdapter.class.getSimpleName();
    private static final String INTERNAL_STORAGE_KEY = "OnMyMinds";

    private List<OnMyMind> ommsList;
    private Context context;
    private FirebaseFirestore firestoreDB;
    private Toolbar toolbar;
    private View parentLayout;

    public OnMyMindsRecyclerViewAdapter(List<OnMyMind> list, Context ctx, FirebaseFirestore firestore) {
        this.ommsList = list;
        this.context = ctx;
        this.firestoreDB = firestore;
    }

    public void setAdapterList(List<OnMyMind> list) {
        ommsList = list;
    }

    @Override
    public int getItemCount() {
        return ommsList.size();
    }

    @Override
    public OnMyMindsRecyclerViewAdapter.ViewHolder
    onCreateViewHolder(ViewGroup parent, int viewType) {
        parentLayout = parent;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_omm_item, parent, false);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar_omm);

        OnMyMindsRecyclerViewAdapter.ViewHolder viewHolder =
                new OnMyMindsRecyclerViewAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(OnMyMindsRecyclerViewAdapter.ViewHolder holder, int position) {
        final int itemPos = position;
        final OnMyMind omm = ommsList.get(position);
        holder.title.setText(omm.getTitle());
        holder.info.setText(omm.getInfo());
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editOnMyMindFragment(omm, itemPos);
            }
        });
        holder.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO - Move the OnMyMind to Archive.
                Snackbar.make(parentLayout, omm.getTitle() + " moved to Archive",
                        Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // TODO - Undo move to archive.
                            }
                        })
                        .show();
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder =
                        new AlertDialog.Builder(context).setTitle("Delete " + omm.getTitle())
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        deleteOnMyMind(omm.getId(), itemPos);
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
    }

    private void editOnMyMindFragment(OnMyMind omm, int itemPos){
        Bundle bundle = new Bundle();
        bundle.putString("id", omm.getId());
        bundle.putString("title", omm.getTitle());
        bundle.putString("info", omm.getInfo());
        bundle.putInt("position", itemPos);

        Intent i = new Intent(context, AddOnMyMindActivity.class);
        i.putExtra("onmymind", bundle);
        context.startActivity(i);
    }

    public void deleteOnMyMind(final String docId, final int position){
        final String ommTitle = ommsList.get(position).getTitle();

        // Delete the OnMyMind from the recycler list.
        ommsList.remove(position);
        if(ommsList.isEmpty()) {
            TextView emptyListText = (TextView) parentLayout.getRootView().findViewById(R.id.empty_omms_list_tv);
            emptyListText.setVisibility(View.VISIBLE);
        }
        try {
            // Try to get the stored list of OnMyminds and remove the specified OnMyMind.
            List<OnMyMind> storedList = (ArrayList<OnMyMind>) InternalStorage.readObject(context,
                    InternalStorage.ONMYMINDS_STORAGE_KEY);
            storedList.remove(position);
            InternalStorage.writeObject(context, InternalStorage.ONMYMINDS_STORAGE_KEY, storedList);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read from Internal Storage: ");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, ommsList.size());
        // Delete the specified OnMyMind from Firestore Database.
        firestoreDB.collection("onmyminds").document(docId).delete()
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
                        Log.e(TAG, "Failed to delete OnMyMind - id: " + docId);
                        e.printStackTrace();
                    }
                });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView info;
        private Button edit;
        private Button done;
        private Button delete;

        public ViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.title_tv);
            info = (TextView) view.findViewById(R.id.info_tv);
            edit = (Button) view.findViewById(R.id.edit_omm_b);
            done = (Button) view.findViewById(R.id.done_omm_b);
            delete = (Button) view.findViewById(R.id.delete_omm_b);
        }
    }
}
