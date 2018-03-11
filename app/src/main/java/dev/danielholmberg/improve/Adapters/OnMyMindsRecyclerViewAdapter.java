package dev.danielholmberg.improve.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.Fragments.OnMyMindDetailsDialogFragment;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-21.
 */

public class OnMyMindsRecyclerViewAdapter extends
        RecyclerView.Adapter<OnMyMindsRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = OnMyMindsRecyclerViewAdapter.class.getSimpleName();
    private static final String INTERNAL_STORAGE_KEY = "OnMyMinds";

    public List<OnMyMind> ommsList;
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
        final OnMyMind omm = ommsList.get(holder.getAdapterPosition());

        // Fill the list-item with all the necessary content.
        holder.cardMarker.setBackgroundColor(Color.parseColor(omm.getColor()));
        holder.title.setText(omm.getTitle());
        holder.info.setText(omm.getInfo());
        holder.createdTimestamp.setText(omm.getCreatedTimestamp());
        if(omm.getUpdatedTimestamp() != null) {
            if(!omm.getUpdatedTimestamp().isEmpty()) {
                holder.updatedTimestamp.setText(omm.getUpdatedTimestamp());
                holder.updatedTimestamp.setVisibility(View.VISIBLE);
                holder.updatedLabel.setVisibility(View.VISIBLE);
            }
        }

        // Handle what happens when the user clicks on the OnMyMind toolbar.
        setUpOnClickListener(holder.container, omm, holder.getAdapterPosition());
    }

    private void setUpOnClickListener(View view, final OnMyMind omm, final int itemPos) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOnMyMindDetailsActivity(omm, itemPos);
            }
        });
    }

    private Bundle createBundle(OnMyMind omm, int itemPos) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("omm", omm);
        bundle.putInt("position", itemPos);
        return bundle;
    }

    private void showOnMyMindDetailsActivity(OnMyMind omm, int itemPos) {
        Bundle bundle = createBundle(omm, itemPos);
        OnMyMindDetailsDialogFragment onMyMindDetailsDialogFragment = new OnMyMindDetailsDialogFragment();
        onMyMindDetailsDialogFragment.setArguments(bundle);
        onMyMindDetailsDialogFragment.setAdapter(this);
        onMyMindDetailsDialogFragment.show(((AppCompatActivity)context).getSupportFragmentManager(), onMyMindDetailsDialogFragment.getTag());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View container;
        private ImageView cardMarker;
        private TextView title;
        private TextView info;
        private TextView createdTimestamp;
        private TextView updatedTimestamp;
        private TextView updatedLabel;

        public ViewHolder(View view) {
            super(view);

            container = (View) view.findViewById(R.id.omm_item_container);
            cardMarker = (ImageView) view.findViewById(R.id.omm_item_marker_iv);

            title = (TextView) view.findViewById(R.id.title_tv);
            info = (TextView) view.findViewById(R.id.info_tv);
            createdTimestamp = (TextView) view.findViewById(R.id.footer_omm_created_tv);
            updatedTimestamp = (TextView) view.findViewById(R.id.footer_omm_updated_tv);
            updatedLabel = (TextView) view.findViewById(R.id.footer_omm_updated_label_tv);
        }
    }
}
