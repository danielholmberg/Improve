package dev.danielholmberg.improve.ViewHolders;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.Fragments.OnMyMindDetailsDialogFragment;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.R;

/**
 * Created by Daniel Holmberg.
 */

public class OnMyMindViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    private Context context;

    public OnMyMindViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        context = itemView.getContext();
    }

    public void bindModelToView(final OnMyMind onMyMind) {
        final ImageView marker = (ImageView) mView.findViewById(R.id.item_onmymind_marker_iv);
        marker.setBackgroundColor(Color.parseColor(onMyMind.getColor()));
        ((TextView) mView.findViewById(R.id.item_onmymind_title_tv)).setText(onMyMind.getTitle());
        ((TextView) mView.findViewById(R.id.item_onmymind_info_tv)).setText(onMyMind.getInfo());
        ((TextView) mView.findViewById(R.id.footer_omm_created_tv)).setText(onMyMind.getCreatedTimestamp());

        if(onMyMind.getUpdatedTimestamp() != null) {
            if(!onMyMind.getUpdatedTimestamp().isEmpty()) {
                ((TextView) mView.findViewById(R.id.footer_omm_updated_tv))
                        .setText(onMyMind.getUpdatedTimestamp());
                ((TextView) mView.findViewById(R.id.footer_omm_updated_tv))
                        .setVisibility(View.VISIBLE);
                ((TextView) mView.findViewById(R.id.footer_omm_updated_label_tv))
                        .setVisibility(View.VISIBLE);
            }
        }

        if(onMyMind.getIsDone()) {
            marker.setBackgroundColor(context.getResources().getColor(R.color.doneBackground));
        }

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = Improve.getInstance().getFirebaseStorageManager().getOnMyMindsRef()
                        .child(onMyMind.getId());
                ref.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        OnMyMind onMyMind = dataSnapshot.getValue(OnMyMind.class);
                        int itemPosition = getAdapterPosition();

                        OnMyMindDetailsDialogFragment onMyMindDetailsDialogFragment = new OnMyMindDetailsDialogFragment();
                        onMyMindDetailsDialogFragment.setArguments(createBundle(onMyMind, itemPosition));
                        onMyMindDetailsDialogFragment.show(((AppCompatActivity)context).getSupportFragmentManager(),
                                onMyMindDetailsDialogFragment.getTag());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private Bundle createBundle(OnMyMind omm, int itemPos) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("onMyMind", omm);
        bundle.putInt("position", itemPos);
        bundle.putBoolean("isDone", omm.getIsDone());
        return bundle;
    }
}
