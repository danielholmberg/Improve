package dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.ImageViewHolder
import java.util.ArrayList

class ImagesAdapter(private val noteId: String, private val thumbnail: Boolean) :
    RecyclerView.Adapter<ImageViewHolder>() {

    private val vipImages: SortedList<Image> =
        SortedList(Image::class.java, object : SortedList.Callback<Image>() {
            override fun compare(o1: Image, o2: Image): Int {
                return o1.id!!.compareTo(o2.id!!)
            }

            override fun onChanged(position: Int, count: Int) {
                notifyItemChanged(position)
            }

            override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        vipImageView = if (thumbnail) {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vip_image_thumbnail, parent, false)
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vip_image_preview, parent, false)
        }
        return ImageViewHolder(parent.context, vipImageView!!, noteId, this)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
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

    fun add(image: Image) {
        Log.d(TAG, "Adding image: " + image.id)
        vipImages.add(image)
    }

    fun remove(image: Image) {
        Log.d(TAG, "Removing image: " + image.id)
        vipImages.remove(image)
    }

    fun addImages(images: ArrayList<Image>) {
        vipImages.beginBatchedUpdates()
        for (image in images) {
            vipImages.add(image)
        }
        vipImages.endBatchedUpdates()
    }

    val imageList: ArrayList<Image>
        get() {
            val imageCopy = ArrayList<Image>()
            for (i in 0 until vipImages.size()) {
                imageCopy.add(vipImages[i])
            }
            return imageCopy
        }

    fun setEditMode(editMode: Boolean) {
        this.editMode = editMode
        notifyDataSetChanged()
    }

    companion object {
        private val TAG = ImagesAdapter::class.java.simpleName
    }
}