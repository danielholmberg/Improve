package dev.danielholmberg.improve.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
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
                Toast.makeText(context, omm.getTitle()+" marked as Done", Toast.LENGTH_SHORT).show();
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder =
                        new AlertDialog.Builder(context).setTitle("Delete OnMyMind")
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

    public void deleteOnMyMind(String docId, final int position){
        // Delete the OnMyMind from the recycler list.
        ommsList.remove(position);
        try {
            // Try to get the stored list of OnMyminds and remove the specified OnMyMind.
            List<OnMyMind> storedList = (List<OnMyMind>) InternalStorage.readObject(context, INTERNAL_STORAGE_KEY);
            storedList.remove(position);
            InternalStorage.writeObject(context, INTERNAL_STORAGE_KEY, storedList);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, ommsList.size());
        // Delete the specified OnMyMind from Firestore Database.
        firestoreDB.collection("onmyminds").document(docId).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Deletion was successful.
                        Toast.makeText(context,
                                "OnMyMind document has been deleted",
                                Toast.LENGTH_SHORT).show();
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
