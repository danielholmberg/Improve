package dev.danielholmberg.improve.clean.feature_note.presentation.notes

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import dev.danielholmberg.improve.R
import com.squareup.picasso.Picasso
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import dev.danielholmberg.improve.clean.core.util.CircleTransform
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.ImagesAdapter
import dev.danielholmberg.improve.legacy.Managers.StorageManager.Companion.VIP_IMAGE_SUFFIX
import java.io.File

class ImageViewHolder(
    private val context: Context,
    itemView: View,
    noteId: String,
    vipImagesAdapter: ImagesAdapter
) : RecyclerView.ViewHolder(
    itemView
) {
    private val noteId: String
    private var image: Image? = null
    private val vipImagesAdapter: ImagesAdapter

    init {
        this.noteId = noteId
        this.vipImagesAdapter = vipImagesAdapter
    }

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
        val file = File(
            instance!!.fileService.imageDir,
            image.id + VIP_IMAGE_SUFFIX
        )
        val targetSize = instance!!.resources.getDimension(R.dimen.vip_image_view_size).toInt()
        val vipImageView = this.itemView.findViewById<View>(R.id.vip_image_view) as ImageView
        if (file.exists()) {
            Log.d(TAG, "Loading Preview image from Local Filesystem at path: ${file.path}")
            Picasso.get()
                .load(file)
                .centerCrop()
                .resize(targetSize, targetSize)
                .into(vipImageView)

            //vipImagePlaceholder.setVisibility(View.GONE);
            vipImageView.visibility = View.VISIBLE
        } else if (image.originalFilePath != null) {
            Log.d(
                TAG,
                "Loading Preview image from Device Filesystem at path: ${image.originalFilePath}"
            )
            Picasso.get()
                .load(Uri.parse(image.originalFilePath))
                .centerCrop()
                .resize(targetSize, targetSize)
                .into(vipImageView)

            //vipImagePlaceholder.setVisibility(View.GONE);
            vipImageView.visibility = View.VISIBLE
        } else {
            Log.d(
                TAG,
                "Downloading Preview image from Firebase for Note ($noteId) with image id: " + image.id
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
                            .into(vipImageView)

                        //vipImagePlaceholder.setVisibility(View.GONE);
                        vipImageView.visibility = View.VISIBLE
                        vipImagesAdapter.notifyDataSetChanged()
                    }

                    override fun onFailure(errorMessage: String?) {}
                    override fun onProgress(progress: Int) {}
                })
        }
        vipImageView.setOnClickListener { showImageFullscreen() }
    }

    private fun showImageFullscreen() {
        val vipImageViewFullscreenLayout = LayoutInflater.from(context)
            .inflate(R.layout.dialog_vip_image_fullscreen, null) as LinearLayout
        val vipImageViewFull =
            vipImageViewFullscreenLayout.findViewById<View>(R.id.vip_image_view_full) as ImageView
        val image = File(
            instance!!.fileService.imageDir,
            image!!.id + VIP_IMAGE_SUFFIX
        )
        if (image.exists()) {
            Log.d(TAG, "Loading Fullscreen image from Local Filesystem at path: " + image.path)
            Picasso.get()
                .load(image)
                .into(vipImageViewFull)
        } else if (this.image!!.originalFilePath != null) {
            Log.d(
                TAG,
                "Loading Fullscreen image from Local Filesystem at path: " + this.image!!.originalFilePath
            )
            Picasso.get()
                .load(Uri.parse(this.image!!.originalFilePath))
                .into(vipImageViewFull)
        } else {
            Log.d(TAG, "Loading Fullscreen image from Firebase with id: " + this.image!!.id)

            // TODO: Should be handled by UseCase
            instance!!.imageRepository
                .downloadImageToLocalFile(this.image!!.id!!, object :
                    ImageCallback {
                    override fun onSuccess(`object`: Any) {
                        Picasso.get()
                            .load(`object` as Uri)
                            .into(vipImageViewFull)
                    }

                    override fun onFailure(errorMessage: String?) {}
                    override fun onProgress(progress: Int) {}
                })
        }
        val alertDialogBuilder = AlertDialog.Builder(
            context, R.style.CustomFullscreenDialogStyle
        )
            .setView(vipImageViewFullscreenLayout)
            .setCancelable(true)
        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    fun bindModelToThumbnailView(image: Image?) {
        if (image == null) return
        this.image = image
        val vipImageView = itemView.findViewById<ImageView>(R.id.vip_image_view_thumbnail)
        val vipImagePlaceholder = itemView.findViewById<ProgressBar>(R.id.vip_image_progressBar)
        vipImagePlaceholder.visibility = View.VISIBLE
        vipImageView.visibility = View.GONE
        val file = File(
            instance!!.fileService.imageDir,
            image.id + VIP_IMAGE_SUFFIX
        )
        val thumbnailSize =
            instance!!.resources.getDimension(R.dimen.vip_image_view_thumbnail_size).toInt()

        // If an image has previously been downloaded to local storage
        if (file.exists()) {
            Log.d(TAG, "Loading image from Local Filesystem with file at path: ${file.path}")
            Picasso.get()
                .load(file)
                .centerCrop()
                .transform(CircleTransform())
                .resize(thumbnailSize, thumbnailSize)
                .into(vipImageView)
            vipImagePlaceholder.visibility = View.GONE
            vipImageView.visibility = View.VISIBLE
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
                            .into(vipImageView)
                        vipImagePlaceholder.visibility = View.GONE
                        vipImageView.visibility = View.VISIBLE
                        vipImagesAdapter.notifyDataSetChanged()
                    }

                    override fun onFailure(errorMessage: String?) {}
                    override fun onProgress(progress: Int) {}
                })
        }
    }

    fun setEditMode(editMode: Boolean) {
        val vipImageClearBtn: ImageButton? = this.itemView.findViewById(R.id.vip_image_clear_btn)
        if (vipImageClearBtn != null) {
            if (editMode) {
                vipImageClearBtn.setOnClickListener { vipImagesAdapter.remove(image!!) }
                vipImageClearBtn.visibility = View.VISIBLE
            } else {
                vipImageClearBtn.visibility = View.GONE
            }
        }
    }

    companion object {
        private val TAG = ImageViewHolder::class.java.simpleName
    }
}