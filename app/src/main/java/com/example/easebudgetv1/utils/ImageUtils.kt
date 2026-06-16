package com.example.easebudgetv1.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    private const val MAX_IMAGE_SIZE = 5 * 1024 * 1024 // 5MB
    
    /**
     * Optimization: Use WebP for better compression and faster I/O.
     */
    fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, filename: String): String? {
        return try {
            val file = File(context.filesDir, filename)
            val outputStream = FileOutputStream(file)
            
            // Use WebP for better performance and smaller size
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, outputStream)
            } else {
                @Suppress("DEPRECATION")
                bitmap.compress(Bitmap.CompressFormat.WEBP, 80, outputStream)
            }
            
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copies an image from a Uri to internal storage for persistent access.
     */
    fun copyUriToInternalStorage(context: Context, uri: Uri, filename: String): String? {
        return try {
            val file = File(context.filesDir, filename)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Optimization: Added downsampling to prevent high memory usage and OOMs.
     */
    fun loadImageFromInternalStorage(context: Context, filename: String, reqWidth: Int = 500, reqHeight: Int = 500): Bitmap? {
        return try {
            val file = File(context.filesDir, filename)
            if (!file.exists()) return null

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            // Calculate inSampleSize (Downsampling factor)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
    
    fun deleteImageFromInternalStorage(context: Context, filename: String): Boolean {
        return try {
            val file = File(context.filesDir, filename)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun isImageSizeValid(fileSize: Long): Boolean {
        return fileSize <= MAX_IMAGE_SIZE
    }
}
