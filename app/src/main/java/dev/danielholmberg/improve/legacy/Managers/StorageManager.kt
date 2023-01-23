package dev.danielholmberg.improve.legacy.Managers

import android.net.Uri
import android.util.Log
import dev.danielholmberg.improve.Improve.Companion.instance
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dev.danielholmberg.improve.legacy.Callbacks.StorageCallback
import dev.danielholmberg.improve.legacy.Models.VipImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList

class StorageManager {
    private val storage: FirebaseStorage
        get() = FirebaseStorage.getInstance()

    private val userRef: StorageReference
        get() {
            val userId = instance!!.authManager.currentUserId
            return storage.getReference(USERS_REF).child(
                userId!!
            )
        }
    private val imagesRef: StorageReference
        get() = userRef.child(IMAGES_REF)

    private fun uploadImage(imageId: String?, imageUri: Uri?, callback: StorageCallback) {
        imagesRef.child(imageId!!).putFile(imageUri!!).addOnSuccessListener {
            Log.d(TAG, "SUCCESS: Image ($imageId) uploaded to Firebase Cloud Storage")
            callback.onSuccess(imageId)
        }.addOnFailureListener { e ->
            Log.e(TAG, "FAILURE: To upload image ($imageId) to Firebase Cloud Storage: $e")
            callback.onFailure(e.message)
        }
            .addOnProgressListener { taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                Log.d(TAG, "PROGRESS: " + progress.toInt() + "%")
                callback.onProgress(progress.toInt())
            }
    }

    fun deleteImage(noteId: String, imageId: String) {
        imagesRef.child(noteId).child(imageId).delete()
            .addOnSuccessListener { Log.d(TAG, "SUCCESS: Image with id: $imageId was deleted.") }
            .addOnFailureListener { e ->
                Log.e(
                    TAG,
                    "FAILURE: To delete image with id: $imageId due to: $e"
                )
            }
    }

    fun downloadImageToLocalFile(imageId: String, callback: StorageCallback) {
        Log.d(TAG, "Downloading image to Local Filesystem...")
        val targetFile = File(instance!!.imageDir, imageId + IMAGE_SUFFIX)
        Log.d(TAG, "targetFile path: " + targetFile.path)
        imagesRef.child(imageId).getFile(targetFile).addOnSuccessListener {
            Log.d(
                TAG,
                "SUCCESS: Downloaded image with id: " + imageId + " to filePath: " + targetFile.path
            )
            callback.onSuccess(targetFile)
        }.addOnFailureListener {
            Log.e(
                TAG,
                "FAILURE: To download image with id: " + imageId + " to filePath: " + targetFile.path
            )
        }
    }

    fun uploadMultipleImages(vipImagesList: List<VipImage>, callback: StorageCallback) {
        Log.d(TAG, "Uploading multiple (" + vipImagesList.size + ") images to Firebase")
        val vipImagesUploaded = ArrayList<VipImage>()
        for (i in vipImagesList.indices) {
            val vipImage = vipImagesList[i]
            val imageId = vipImage.id
            val originalFilePath = Uri.parse(vipImage.originalFilePath)
            val cachedImage = File(instance!!.imageDir, imageId + IMAGE_SUFFIX)
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
                StorageCallback {
                override fun onSuccess(`object`: Any) {
                    vipImagesUploaded.add(vipImage)
                    if (vipImagesList.size == vipImagesUploaded.size) {
                        callback.onSuccess(vipImagesUploaded)
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
        Log.d(TAG, "Copying File from: " + sourceUri + " to File: " + destFile.absolutePath)
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
        private val TAG = StorageManager::class.java.simpleName
        const val USERS_REF = "Users"
        const val IMAGES_REF = "Images"
        const val IMAGE_SUFFIX = ".jpg"
    }
}