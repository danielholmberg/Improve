package dev.danielholmberg.improve.ViewHolders;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import dev.danielholmberg.improve.Models.Tag;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.R;

public class TagViewHolder extends RecyclerView.ViewHolder{

    private View itemView;
    private Tag tag;
    private TextView tagLabel;
    private ImageView tagEnabledIndicator;
    private GradientDrawable background;

    public TagViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    /**
     * Binds data from Tag (Model) object to related View.
     *
     * OBS! Because the RecyclerView reuses old ViewHolders in the list, we therefore
     * need to define ALL Views of each Tag to make them display specific information
     * for targeted Tag!
     *
     * @param tag - Target Tag (Model)
     */
    public void bindModelToView(final Tag tag) {
        if(tag == null) return;

        this.tag = tag;

        tagLabel = (TextView) itemView.findViewById(R.id.tag_label_tv);
        tagEnabledIndicator = (ImageView) itemView.findViewById(R.id.tag_enabled_indicator);

        if(tag.getLabel() != null) tagLabel.setText(tag.getLabel());
        if(tag.getTextColor() != null) tagLabel.setTextColor(Color.parseColor(tag.getTextColor()));

        StateListDrawable gradientDrawable = (StateListDrawable) itemView.getBackground();
        DrawableContainer.DrawableContainerState drawableContainerState =
                (DrawableContainer.DrawableContainerState) gradientDrawable.getConstantState();
        if(drawableContainerState != null) {
            Drawable[] children = drawableContainerState.getChildren();
            background = (GradientDrawable) children[0];
            background.setColor(Color.parseColor(tag.getColor()));
        }
    }

    public void setTagStatusOnNote(boolean isEnabled) {
        if(isEnabled) {
            tagEnabledIndicator.setVisibility(View.VISIBLE);
        } else {
            tagEnabledIndicator.setVisibility(View.GONE);
        }
    }

    public void setEditMode(boolean editMode) {
        ImageView deleteButton = itemView.findViewById(R.id.tag_delete_btn);
        if(editMode) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialogBuilder =
                            new AlertDialog.Builder(itemView.getContext())
                                    .setTitle(R.string.dialog_delete_tag_title)
                                    .setMessage(R.string.dialog_delete_tag_msg)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Improve.getInstance().getFirebaseDatabaseManager().deleteTag(tag.getId());
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
        } else {
            deleteButton.setVisibility(View.GONE);
        }
    }
}