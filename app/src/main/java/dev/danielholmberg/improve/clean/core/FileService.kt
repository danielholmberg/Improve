package dev.danielholmberg.improve.clean.core

import android.content.Context
import android.util.Log
import dev.danielholmberg.improve.legacy.Managers.StorageManager
import java.io.File

class FileService(private val context: Context) {

    lateinit var imageDir: File

    init {
        val rootDir = generateRootDir()
        generateImageDir(rootDir)
    }

    private fun generateRootDir(): File {
        val rootDir = context.filesDir
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }
        Log.d(TAG, "RootDir: " + rootDir.path)
        return rootDir
    }

    private fun generateImageDir(rootDir: File): File {
        imageDir = File(rootDir, StorageManager.IMAGES_REF)
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
        Log.d(TAG, "ImageDir: " + imageDir.path)
        return imageDir
    }

    companion object {
        private val TAG = FileService::class.java.simpleName
        const val VIP_IMAGE_SUFFIX = ".jpg"
    }
}