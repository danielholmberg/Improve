package dev.danielholmberg.improve.legacy.ViewHolders

import android.content.Context
import android.net.Uri
import android.util.Log
import dev.danielholmberg.improve.Improve.Companion.instance
import dev.danielholmberg.improve.legacy.Adapters.VipImagesAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.danielholmberg.improve.legacy.Models.VipImage
import dev.danielholmberg.improve.legacy.Managers.StorageManager
import dev.danielholmberg.improve.R
import com.squareup.picasso.Picasso
import dev.danielholmberg.improve.legacy.Callbacks.StorageCallback
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import dev.danielholmberg.improve.legacy.Utilities.CircleTransform
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import java.io.File

class VipImageViewHolder(
    private val context: Context,
    itemView: View,
    noteId: String,
    vipImagesAdapter: VipImagesAdapter
) : RecyclerView.ViewHolder(
    itemView
) {
    private val noteId: String
    private var vipImage: VipImage? = null
    private val vipImagesAdapter: VipImagesAdapter

    init {
        this.noteId = noteId
        this.vipImagesAdapter = vipImagesAdapter
    }

    /**
     * Binds data from VipImage (Model) object to related View.
     *
     * OBS! Because the RecyclerView reuses old ViewHolders in the list, we therefore
     * need to define ALL Views of each VipImage to make them display specific information
     * for targeted VipImage!
     *
     * @param vipImage - Target VipImage (Model)
     */
    fun bindModelToPreviewView(vipImage: VipImage?) {
        if (vipImage == null) return
        this.vipImage = vipImage
        val image = File(
            instance!!.imageDir,
            vipImage.id + StorageManager.VIP_IMAGE_SUFFIX
        )
        val targetSize = instance!!.resources.getDimension(R.dimen.vip_image_view_size).toInt()
        val vipImageView = this.itemView.findViewById<View>(R.id.vip_image_view) as ImageView
        if (image.exists()) {
            Log.d(TAG, "Loading Preview image from Local Filesystem at path: " + image.path)
            Picasso.get()
                .load(image)
                .centerCrop()
                .resize(targetSize, targetSize)
                .into(vipImageView)

            //vipImagePlaceholder.setVisibility(View.GONE);
            vipImageView.visibility = View.VISIBLE
        } else if (vipImage.originalFilePath != null) {
            Log.d(
                TAG,
                "Loading Preview image from Device Filesystem at path: " + vipImage.originalFilePath
            )
            Picasso.get()
                .load(Uri.parse(vipImage.originalFilePath))
                .centerCrop()
                .resize(targetSize, targetSize)
                .into(vipImageView)

            //vipImagePlaceholder.setVisibility(View.GONE);
            vipImageView.visibility = View.VISIBLE
        } else {
            Log.d(
                TAG,
                "Downloading Preview image from Firebase for Note: " + noteId + " with image id: " + vipImage.id
            )
            instance!!.storageManager
                .downloadImageToLocalFile(vipImage.id!!, object :
                    StorageCallback {
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
            instance!!.imageDir,
            vipImage!!.id + StorageManager.VIP_IMAGE_SUFFIX
        )
        if (image.exists()) {
            Log.d(TAG, "Loading Fullscreen image from Local Filesystem at path: " + image.path)
            Picasso.get()
                .load(image)
                .into(vipImageViewFull)
        } else if (vipImage!!.originalFilePath != null) {
            Log.d(
                TAG,
                "Loading Fullscreen image from Local Filesystem at path: " + vipImage!!.originalFilePath
            )
            Picasso.get()
                .load(Uri.parse(vipImage!!.originalFilePath))
                .into(vipImageViewFull)
        } else {
            Log.d(TAG, "Loading Fullscreen image from Firebase with id: " + vipImage!!.id)
            instance!!.storageManager
                .downloadImageToLocalFile(vipImage!!.id!!, object :
                    StorageCallback {
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

    fun bindModelToThumbnailView(vipImage: VipImage?) {
        if (vipImage == null) return
        this.vipImage = vipImage
        val vipImageView = itemView.findViewById<ImageView>(R.id.vip_image_view_thumbnail)
        val vipImagePlaceholder = itemView.findViewById<ProgressBar>(R.id.vip_image_progressBar)
        vipImagePlaceholder.visibility = View.VISIBLE
        vipImageView.visibility = View.GONE
        val image = File(
            instance!!.imageDir,
            vipImage.id + StorageManager.VIP_IMAGE_SUFFIX
        )
        val thumbnailSize =
            instance!!.resources.getDimension(R.dimen.vip_image_view_thumbnail_size).toInt()

        // If an image has previously been downloaded to local storage
        if (image.exists()) {
            Log.d(TAG, "Loading image from Local Filesystem with file at path: " + image.path)
            Picasso.get()
                .load(image)
                .centerCrop()
                .transform(CircleTransform())
                .resize(thumbnailSize, thumbnailSize)
                .into(vipImageView)
            vipImagePlaceholder.visibility = View.GONE
            vipImageView.visibility = View.VISIBLE
        } else {
            // Download image from Firebase to a local file
            Log.d(TAG, "Loading image from Firebase with image id: " + vipImage.id)
            instance!!.storageManager
                .downloadImageToLocalFile(vipImage.id!!, object :
                    StorageCallback {
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
                vipImageClearBtn.setOnClickListener { vipImagesAdapter.remove(vipImage!!) }
                vipImageClearBtn.visibility = View.VISIBLE
            } else {
                vipImageClearBtn.visibility = View.GONE
            }
        }
    }

    companion object {
        private val TAG = VipImageViewHolder::class.java.simpleName
    }
}