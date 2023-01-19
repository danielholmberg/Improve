package dev.danielholmberg.improve.clean.feature_note.data.source.image

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.core.FileService.Companion.VIP_IMAGE_SUFFIX
import dev.danielholmberg.improve.clean.feature_authentication.domain.repository.AuthRepository
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList

class ImageDataSourceImpl(
    private val authRepository: AuthRepository,
    private val storageService: FirebaseStorage
) : ImageDataSource {
    private val imagesRef: StorageReference
        get() = storageService.getReference(USERS_REF).child(
            authRepository.getCurrentUserId()!!
        ).child(IMAGES_REF)

    override fun deleteImage(noteEntityId: String, imageEntityId: String) {
        imagesRef.child(noteEntityId).child(imageEntityId).delete()
            .addOnSuccessListener { Log.d(TAG, "SUCCESS: Image with id: $noteEntityId was deleted.") }
            .addOnFailureListener { e ->
                Log.e(
                    TAG,
                    "FAILURE: To delete image with id ($noteEntityId) due to: $e"
                )
            }
    }

    override fun uploadImage(noteEntityId: String?, imageEntityUri: Uri?, callback: ImageCallback) {
        imagesRef.child(noteEntityId!!).putFile(imageEntityUri!!).addOnSuccessListener {
            Log.d(TAG, "SUCCESS: Image ($noteEntityId) uploaded to Firebase Cloud Storage")
            callback.onSuccess(noteEntityId)
        }.addOnFailureListener { e ->
            Log.e(TAG, "FAILURE: To upload image ($noteEntityId) to Firebase Cloud Storage: $e")
            callback.onFailure(e.message)
        }
            .addOnProgressListener { taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                Log.d(TAG, "PROGRESS: " + progress.toInt() + "%")
                callback.onProgress(progress.toInt())
            }
    }

    override fun downloadImageToLocalFile(imageEntityId: String, callback: ImageCallback) {
        Log.d(TAG, "Downloading image to Local Filesystem...")
        val targetFile =
            File(instance!!.fileService.imageDir, imageEntityId + VIP_IMAGE_SUFFIX)
        Log.d(TAG, "targetFile path: " + targetFile.path)
        imagesRef.child(imageEntityId).getFile(targetFile).addOnSuccessListener {
            Log.d(
                TAG,
                "SUCCESS: Downloaded image with id ($imageEntityId) to filePath: " + targetFile.path
            )
            callback.onSuccess(targetFile)
        }.addOnFailureListener {
            Log.e(
                TAG,
                "FAILURE: To download image with id ($imageEntityId) to filePath: " + targetFile.path
            )
        }
    }

    override fun uploadMultipleImages(imagesList: List<Image>, callback: ImageCallback) {
        Log.d(TAG, "Uploading multiple (" + imagesList.size + ") images to Firebase")
        val imagesUploaded = ArrayList<Image>()
        for (i in imagesList.indices) {
            val image = imagesList[i]
            val imageId = image.id
            val originalFilePath = Uri.parse(image.originalFilePath)
            val cachedImage =
                File(instance!!.fileService.imageDir, imageId + VIP_IMAGE_SUFFIX)
            if (!cachedImage.exists()) {
                try {
                    Log.d(TAG, "Copying image to Local Filesystem with image id: $imageId")
                    copyFileFromUri(originalFilePath, cachedImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                    callback.onFailure(e.message)
                }
            }
            uploadImage(imageId, Uri.fromFile(cachedImage), object :
                ImageCallback {
                override fun onSuccess(`object`: Any) {
                    imagesUploaded.add(image)
                    if (imagesList.size == imagesUploaded.size) {
                        callback.onSuccess(imagesUploaded)
                    }
                }

                override fun onFailure(errorMessage: String?) {
                    callback.onFailure(errorMessage)
                }

                override fun onProgress(progress: Int) {}
            })
        }
    }

    @Throws(IOException::class)
    private fun copyFileFromUri(sourceUri: Uri, destFile: File) {
        Log.d(TAG, "Copying File from ($sourceUri) to File: ${destFile.absolutePath}")
        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        val `in` = instance!!.contentResolver.openInputStream(sourceUri)
        val out: OutputStream = FileOutputStream(destFile)

        // Copy the bits from instream to outstream
        val buf = ByteArray(1024)
        var len: Int
        if (`in` != null) {
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            `in`.close()
        }
        out.close()
    }

    companion object {
        private val TAG = ImageDataSourceImpl::class.java.simpleName
        private const val USERS_REF = "Users"
        private const val IMAGES_REF = "Images"
    }
}