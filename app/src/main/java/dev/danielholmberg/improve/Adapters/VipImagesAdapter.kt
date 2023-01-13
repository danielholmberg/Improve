package dev.danielholmberg.improve.Adapters

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import dev.danielholmberg.improve.ViewHolders.VipImageViewHolder
import androidx.recyclerview.widget.SortedList
import dev.danielholmberg.improve.Models.VipImage
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import dev.danielholmberg.improve.R
import java.util.ArrayList

class VipImagesAdapter(private val noteId: String, private val thumbnail: Boolean) :
    RecyclerView.Adapter<VipImageViewHolder>() {

    private val vipImages: SortedList<VipImage> =
        SortedList(VipImage::class.java, object : SortedList.Callback<VipImage>() {
            override fun compare(o1: VipImage, o2: VipImage): Int {
                return o1.id!!.compareTo(o2.id!!)
            }

            override fun onChanged(position: Int, count: Int) {
                notifyItemChanged(position)
            }

            override fun areContentsTheSame(oldItem: VipImage, newItem: VipImage): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areItemsTheSame(oldItem: VipImage, newItem: VipImage): Boolean {
                return oldItem.id == newItem.id
            }

            override fun onInserted(position: Int, count: Int) {
                notifyItemInserted(position)
            }

            override fun onRemoved(position: Int, count: Int) {
                notifyItemRemoved(position)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }
        })

    private var vipImageView: View? = null
    private var editMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VipImageViewHolder {
        vipImageView = if (thumbnail) {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vip_image_thumbnail, parent, false)
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vip_image_preview, parent, false)
        }
        return VipImageViewHolder(parent.context, vipImageView!!, noteId, this)
    }

    override fun onBindViewHolder(holder: VipImageViewHolder, position: Int) {
        if (thumbnail) {
            holder.bindModelToThumbnailView(vipImages[position])
        } else {
            holder.bindModelToPreviewView(vipImages[position])
        }
        holder.setEditMode(editMode)
    }

    override fun getItemCount(): Int {
        return vipImages.size()
    }

    fun add(vipImage: VipImage) {
        Log.d(TAG, "Adding vipImage: " + vipImage.id)
        vipImages.add(vipImage)
    }

    fun remove(vipImage: VipImage) {
        Log.d(TAG, "Removing vipImage: " + vipImage.id)
        vipImages.remove(vipImage)
    }

    fun addImages(images: ArrayList<VipImage>) {
        vipImages.beginBatchedUpdates()
        for (vipImage in images) {
            vipImages.add(vipImage)
        }
        vipImages.endBatchedUpdates()
    }

    val imageList: ArrayList<VipImage>
        get() {
            val vipImageCopy = ArrayList<VipImage>()
            for (i in 0 until vipImages.size()) {
                vipImageCopy.add(vipImages[i])
            }
            return vipImageCopy
        }

    fun setEditMode(editMode: Boolean) {
        this.editMode = editMode
        notifyDataSetChanged()
    }

    companion object {
        private val TAG = VipImagesAdapter::class.java.simpleName
    }
}