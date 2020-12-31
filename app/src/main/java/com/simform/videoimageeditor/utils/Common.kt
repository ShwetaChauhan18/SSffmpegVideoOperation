package com.simform.videoimageeditor.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaExtractor
import android.media.MediaFormat
import androidx.appcompat.app.AppCompatActivity
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.simform.videoimageeditor.R
import java.io.File
import java.io.FileInputStream
import java.text.DecimalFormat
import java.util.Formatter
import java.util.Locale
import pub.devrel.easypermissions.EasyPermissions

/**
 * Created by Ashvin Vavaliya on 24,November,2020
 * Simform Solutions Pvt Ltd.
 */
object Common {
    const val PERM = 111
    const val VIDEO_FILE_REQUEST_CODE = 112
    const val IMAGE_FILE_REQUEST_CODE = 113
    const val TIME_FORMAT = "HH:mm:ss"
    var FRAME_RATE: Int = 25
    const val OUT_PUT_DIR: String = "Output"
    private val format: DecimalFormat = DecimalFormat("#.##")
    private const val MB = (1024 * 1024).toLong()
    private const val KB: Long = 1024

    fun addSupportActionBar(context: AppCompatActivity) {
        if (context.supportActionBar != null) {
            context.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            context.supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }

    fun openActivity(context: Context, activity: AppCompatActivity) {
        context.startActivity(Intent(context, activity::class.java))
    }

    fun getPermission(context: AppCompatActivity): Boolean {
        val perms = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return if (EasyPermissions.hasPermissions(context, *perms)) {
            true
        } else {
            EasyPermissions.requestPermissions(
                context,
                context.getString(R.string.camera_storage_permission_message),
                PERM,
                *perms
            )
            false
        }
    }

    fun stringForTime(timeMs: Long?): String {
        val mFormatBuilder = StringBuilder()
        val mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        val totalSeconds = timeMs?.div(1000)
        val seconds = (totalSeconds?.rem(60))?.toInt()
        val minutes = ((totalSeconds?.div(60))?.rem(60))?.toInt()
        val hours = (totalSeconds?.div(3600))?.toInt()
        mFormatBuilder.setLength(0)
        return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString()
    }

    fun getFrameRate(fileString: String) {
        val extractor = MediaExtractor()
        val file = File(fileString)
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            val fd = fis.fd
            extractor.setDataSource(fd)
            val numTracks = extractor.trackCount
            for (i in 0 until numTracks) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime.startsWith("video/")) {
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        FRAME_RATE = format.getInteger(MediaFormat.KEY_FRAME_RATE)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            extractor.release()
            try {
                fis?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getFileSize(file: File): String {
        require(file.isFile) { "Expected a file" }
        val length = file.length().toDouble()
        if (length > MB) {
            return format.format(length / MB).toString() + " MB"
        }
        return if (length > KB) {
            format.format(length / KB).toString() + " KB"
        } else format.format(length).toString() + " GB"
    }

    fun selectFile(activity: AppCompatActivity, maxSelection: Int, isImageSelection: Boolean) {
        val intent = Intent(activity, FilePickerActivity::class.java)
        if (isImageSelection) {
            intent.putExtra(
                FilePickerActivity.CONFIGS, Configurations.Builder()
                    .setCheckPermission(true)
                    .setShowImages(true)
                    .setShowVideos(false)
                    .enableImageCapture(true)
                    .enableVideoCapture(false)
                    .setMaxSelection(maxSelection)
                    .setSkipZeroSizeFiles(true)
                    .build()
            )
            activity.startActivityForResult(intent, IMAGE_FILE_REQUEST_CODE)
        } else {
            intent.putExtra(
                FilePickerActivity.CONFIGS, Configurations.Builder()
                    .setCheckPermission(true)
                    .setShowImages(false)
                    .setShowVideos(true)
                    .enableImageCapture(false)
                    .enableVideoCapture(true)
                    .setMaxSelection(maxSelection)
                    .setSkipZeroSizeFiles(true)
                    .build()
            )
            activity.startActivityForResult(intent, VIDEO_FILE_REQUEST_CODE)
        }

    }
}