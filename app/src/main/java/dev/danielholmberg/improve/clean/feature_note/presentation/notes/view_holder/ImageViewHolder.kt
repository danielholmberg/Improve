package dev.danielholmberg.improve.clean.feature_note.presentation.notes.view_holder

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.core.util.CircleTransform
import dev.danielholmberg.improve.clean.core.util.RoundedCornersTransform
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.ImagesAdapter
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback
import dev.danielholmberg.improve.legacy.Managers.StorageManager.Companion.IMAGE_SUFFIX
import java.io.File

class ImageViewHolder(
    private val context: Context,
    itemView: View,
    private val imagesAdapter: ImagesAdapter
) : RecyclerView.ViewHolder(
    itemView
) {
    private lateinit var image: Image
    private var imageClearBtn: ImageButton? = null

    /**
     * Binds data from Image (Model) object to related View.
     *
     * OBS! Because the RecyclerView reuses old ViewHolders in the list, we therefore
     * need to define ALL Views of each Image to make them display specific information
     * for targeted Image!
     *
     * @param image - Target Image (Model)
     */
    fun bindModelToPreviewView(image: Image?) {
        if (image == null) return

        this.image = image

        val targetSize = instance!!.resources.getDimension(R.dimen.image_view_size).toInt()
        val roundedCornersRadius =
            (instance!!.resources.getDimension(R.dimen.image_preview_corner_radius) - instance!!.resources.getDimension(
                R.dimen.image_border_width
            )).toInt()
        val imageView = this.itemView.findViewById<View>(R.id.image_view) as ImageView
        imageClearBtn = itemView.findViewById(R.id.image_clear_btn)
        imageClearBtn?.setOnClickListener { imagesAdapter.remove(image) }

        val file = File(
            instance!!.fileService.imageDir,
            image.id + IMAGE_SUFFIX
        )
        if (file.exists()) {
            Log.d(TAG, "Loading Preview image from Local Filesystem at path: ${file.path}")
            Picasso.get()
                .load(file)
                .resize(targetSize, targetSize)
                .centerCrop()
                .transform(RoundedCornersTransform(roundedCornersRadius))
                .into(imageView)

            imageView.visibility = View.VISIBLE
        } else if (image.originalFilePath != null) {
            Log.d(
                TAG,
                "Loading Preview image from Device Filesystem at path: ${image.originalFilePath}"
            )
            Picasso.get()
                .load(Uri.parse(image.originalFilePath))
                .resize(targetSize, targetSize)
                .centerCrop()
                .transform(RoundedCornersTransform(roundedCornersRadius))
                .into(imageView)

            imageView.visibility = View.VISIBLE
        } else {
            Log.d(
                TAG,
                "Downloading Preview image from Firebase with image id: " + image.id
            )

            // TODO: Should be handled by UseCase

            instance!!.imageRepository
                .downloadImageToLocalFile(image.id!!, object :
                    ImageCallback {
                    override fun onSuccess(`object`: Any) {
                        Picasso.get()
                            .load((`object` as File))
                            .centerCrop()
                            .resize(targetSize, targetSize)
                            .into(imageView)

                        //vipImagePlaceholder.setVisibility(View.GONE);
                        imageView.visibility = View.VISIBLE
                        imagesAdapter.notifyDataSetChanged()
                    }

                    override fun onFailure(errorMessage: String?) {}
                    override fun onProgress(progress: Int) {}
                })
        }
        imageView.setOnClickListener { showImageFullscreen() }
    }

    private fun showImageFullscreen() {
        val imageViewFullscreenLayout = LayoutInflater.from(context)
            .inflate(R.layout.dialog_image_fullscreen, null) as LinearLayout
        val imageViewFull =
            imageViewFullscreenLayout.findViewById<View>(R.id.image_view_full) as ImageView
        val image = File(
            instance!!.fileService.imageDir,
            image.id + IMAGE_SUFFIX
        )
        if (image.exists()) {
            Log.d(TAG, "Loading Fullscreen image from Local Filesystem at path: " + image.path)
            Picasso.get()
                .load(image)
                .into(imageViewFull)
        } else if (this.image.originalFilePath != null) {
            Log.d(
                TAG,
                "Loading Fullscreen image from Local Filesystem at path: " + this.image.originalFilePath
            )
            Picasso.get()
                .load(Uri.parse(this.image.originalFilePath))
                .into(imageViewFull)
        } else {
            Log.d(TAG, "Loading Fullscreen image from Firebase with id: " + this.image.id)

            // TODO: Should be handled by UseCase

            instance!!.imageRepository
                .downloadImageToLocalFile(this.image.id!!, object :
                    ImageCallback {
                    override fun onSuccess(`object`: Any) {
                        Picasso.get()
                            .load(`object` as Uri)
                            .into(imageViewFull)
                    }

                    override fun onFailure(errorMessage: String?) {}
                    override fun onProgress(progress: Int) {}
                })
        }
        val alertDialogBuilder = AlertDialog.Builder(
            context, R.style.CustomFullscreenDialogStyle
        )
            .setView(imageViewFullscreenLayout)
            .setCancelable(true)
        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    fun bindModelToThumbnailView(image: Image?) {
        if (image == null) return

        this.image = image

        val imageView = itemView.findViewById<ImageView>(R.id.image_view_thumbnail)
        val imagePlaceholder = itemView.findViewById<ProgressBar>(R.id.image_progressBar)

        imagePlaceholder.visibility = View.VISIBLE
        imageView.visibility = View.GONE

        val file = File(
            instance!!.fileService.imageDir,
            image.id + IMAGE_SUFFIX
        )
        val thumbnailSize =
            instance!!.resources.getDimension(R.dimen.image_view_thumbnail_size).toInt()

        // If an image has previously been downloaded to local storage
        if (file.exists()) {
            Log.d(TAG, "Loading image from Local Filesystem with file at path: ${file.path}")
            Picasso.get()
                .load(file)
                .centerCrop()
                .transform(CircleTransform())
                .resize(thumbnailSize, thumbnailSize)
                .into(imageView)
            imagePlaceholder.visibility = View.GONE
            imageView.visibility = View.VISIBLE
        } else {
            // Download image from Firebase to a local file
            Log.d(TAG, "Loading image from Firebase with image id: ${image.id}")

            // TODO: Should be handled by UseCase

            instance!!.imageRepository
                .downloadImageToLocalFile(image.id!!, object :
                    ImageCallback {
                    override fun onSuccess(`object`: Any) {
                        Picasso.get()
                            .load((`object` as File))
                            .centerCrop()
                            .transform(CircleTransform())
                            .resize(thumbnailSize, thumbnailSize)
                            .into(imageView)
                        imagePlaceholder.visibility = View.GONE
                        imageView.visibility = View.VISIBLE
                        imagesAdapter.notifyDataSetChanged()
                    }

                    override fun onFailure(errorMessage: String?) {}
                    override fun onProgress(progress: Int) {}
                })
        }
    }

    fun setEditMode(editMode: Boolean) {
        imageClearBtn?.visibility = if (editMode) View.VISIBLE else View.GONE
    }

    companion object {
        private val TAG = ImageViewHolder::class.java.simpleName
    }
}