package dev.danielholmberg.improve.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import java.util.ArrayList;

import dev.danielholmberg.improve.Models.VipImage;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.ViewHolders.VipImageViewHolder;

public class VipImagesAdapter extends RecyclerView.Adapter<VipImageViewHolder>{
    private static final String TAG = VipImagesAdapter.class.getSimpleName();

    private String noteId;
    private boolean thumbnail;
    private SortedList<VipImage> vipImages;
    private View vipImageView;
    private boolean editMode = false;

    public VipImagesAdapter(String noteId, boolean thumbnail) {
        this.noteId = noteId;
        this.thumbnail = thumbnail;

        this.vipImages = new SortedList<>(VipImage.class, new SortedList.Callback<VipImage>() {
            @Override
            public int compare(@NonNull VipImage o1, @NonNull VipImage o2) {
                return o1.getId().compareTo(o2.getId());
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemChanged(position);
            }

            @Override
            public boolean areContentsTheSame(@NonNull VipImage oldItem, @NonNull VipImage newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areItemsTheSame(@NonNull VipImage oldItem, @NonNull VipImage newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemInserted(position);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRemoved(position);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });
    }

    @NonNull
    @Override
    public VipImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(thumbnail) {
            vipImageView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_vip_image_thumbnail, parent, false);
        } else {
            vipImageView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_vip_image_preview, parent, false);
        }

        return new VipImageViewHolder(parent.getContext(), vipImageView, noteId, this);

    }

    @Override
    public void onBindViewHolder(@NonNull final VipImageViewHolder holder, int position) {
        if(thumbnail) {
            holder.bindModelToThumbnailView(this.vipImages.get(position));
        } else {
            holder.bindModelToPreviewView(this.vipImages.get(position));
        }

        holder.setEditMode(this.editMode);
    }

    @Override
    public int getItemCount() {
        return vipImages.size();
    }

    public void add(VipImage vipImage) {
        Log.d(TAG, "Adding vipImage: " + vipImage.getId());
        this.vipImages.add(vipImage);
    }

    public void remove(VipImage vipImage) {
        Log.d(TAG, "Removing vipImage: " + vipImage.getId());
        this.vipImages.remove(vipImage);
    }

    public void addImages(ArrayList<VipImage> images) {
        vipImages.beginBatchedUpdates();
        for(VipImage vipImage: images) {
            vipImages.add(vipImage);
        }
        vipImages.endBatchedUpdates();
    }

    public ArrayList<VipImage> getImageList() {
        ArrayList<VipImage> vipImageCopy = new ArrayList<>();
        for(int i = 0; i < vipImages.size(); i++) {
            vipImageCopy.add(vipImages.get(i));
        }
        return vipImageCopy;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        notifyDataSetChanged();
    }

}
