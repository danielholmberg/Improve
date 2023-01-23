package dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import android.view.ViewGroup
import android.view.LayoutInflater
import dev.danielholmberg.improve.BuildConfig
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.view_holder.ImageViewHolder
import java.util.ArrayList

class ImagesAdapter(private val thumbnail: Boolean) :
    RecyclerView.Adapter<ImageViewHolder>() {

    private val images: SortedList<Image> =
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

    private var editMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(
                if (thumbnail) R.layout.item_image_thumbnail else R.layout.item_image_preview,
                parent,
                false
            )
        return ImageViewHolder(parent.context, imageView, this)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (thumbnail) {
            holder.bindModelToThumbnailView(images[position])
        } else {
            holder.bindModelToPreviewView(images[position])
        }
        holder.setEditMode(editMode)
    }

    override fun getItemCount(): Int {
        return images.size()
    }

    fun add(image: Image) {
        Log.d(TAG, "Adding image: " + image.id)
        images.add(image)
    }

    fun remove(image: Image) {
        Log.d(TAG, "Removing image: " + image.id)
        images.remove(image)
    }

    fun addImages(images: ArrayList<Image>) {
        this.images.beginBatchedUpdates()
        for (image in images) {
            this.images.add(image)
        }
        this.images.endBatchedUpdates()
    }

    val imageList: ArrayList<Image>
        get() {
            val imageCopy = ArrayList<Image>()
            for (i in 0 until images.size()) {
                imageCopy.add(images[i])
            }
            return imageCopy
        }

    fun clear() {
        images.clear()
    }

    fun setEditMode(editMode: Boolean) {
        this.editMode = editMode
        notifyDataSetChanged()
    }

    companion object {
        private val TAG = BuildConfig.TAG + ImagesAdapter::class.java.simpleName
    }
}