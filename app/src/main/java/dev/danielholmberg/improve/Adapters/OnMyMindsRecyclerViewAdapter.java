package dev.danielholmberg.improve.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Activities.AddOnMyMindActivity;
import dev.danielholmberg.improve.Activities.OnMyMindDetailsActivity;
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

        OnMyMindsRecyclerViewAdapter.ViewHolder viewHolder =
                new OnMyMindsRecyclerViewAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(OnMyMindsRecyclerViewAdapter.ViewHolder holder, int position) {
        final int itemPos = position;
        final OnMyMind omm = ommsList.get(position);

        // Fill the list-item with all the necessary content.
        holder.cardToolbarView.setBackgroundColor(Color.parseColor(omm.getColor()));
        holder.title.setText(omm.getTitle());
        holder.info.setText(omm.getInfo());

        // Handle what happens when the user clicks on "edit".
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editOnMyMindFragment(omm, itemPos);
            }
        });
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context).setTitle("Delete OnMyMind")
                        .setMessage("Do you really want to delete: " + omm.getTitle())
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

        // Handle what happens when the user clicks on "delete".
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        // Handle what happens when the user clicks on the OnMyMind toolbar.
        setUpOnClickListener(holder.cardToolbarView, omm);
    }

    private void setUpOnClickListener(View view, final OnMyMind omm) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOnMyMindDetailsActivity(omm);
            }
        });
    }

    private void showOnMyMindDetailsActivity(OnMyMind omm) {
        Bundle bundle = createBundle(omm);

        Intent i = new Intent(context, OnMyMindDetailsActivity.class);
        i.putExtra("onmymind", bundle);
        context.startActivity(i);
    }

    private void editOnMyMindFragment(OnMyMind omm, int itemPos){
        Bundle bundle = createBundle(omm);
        bundle.putInt("position", itemPos);

        Intent i = new Intent(context, AddOnMyMindActivity.class);
        i.putExtra("onmymind", bundle);
        context.startActivity(i);
    }

    private Bundle createBundle(OnMyMind omm) {
        Bundle bundle = new Bundle();
        bundle.putString("id", omm.getId());
        bundle.putString("title", omm.getTitle());
        bundle.putString("info", omm.getInfo());
        bundle.putString("color", omm.getColor());
        return bundle;
    }

    public void deleteOnMyMind(final String ommId, final int position){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String ommTitle = ommsList.get(position).getTitle();

        // Delete the OnMyMind from the recycler list.
        ommsList.remove(position);
        if(ommsList.isEmpty()) {
            TextView emptyListText = (TextView) parentLayout.getRootView().findViewById(R.id.empty_omms_list_tv);
            emptyListText.setVisibility(View.VISIBLE);
        }
        try {
            // Try to get the stored list of OnMyminds and remove the specified OnMyMind.
            List<OnMyMind> storedList = (ArrayList<OnMyMind>) InternalStorage.readObject(InternalStorage.onmyminds);
            storedList.remove(position);
            InternalStorage.writeObject(InternalStorage.onmyminds, storedList);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read from Internal Storage: ");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, ommsList.size());
        // Delete the specified OnMyMind from Firestore Database.
        firestoreDB.collection("users")
                .document(userId)
                .collection("onmyminds")
                .document(ommId)
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
                        Log.e(TAG, "Failed to delete OnMyMind - id: " + ommId);
                        e.printStackTrace();
                    }
                });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View cardToolbarView;
        private TextView title;
        private TextView info;
        private Button edit;
        private Button delete;

        public ViewHolder(View view) {
            super(view);

            cardToolbarView = view.findViewById(R.id.toolbar_omm);

            title = (TextView) view.findViewById(R.id.title_tv);
            info = (TextView) view.findViewById(R.id.info_tv);
            edit = (Button) view.findViewById(R.id.edit_omm_b);
            delete = (Button) view.findViewById(R.id.delete_omm_b);
        }
    }
}
