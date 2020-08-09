package com.viajero.cmara

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    //Create the variables
    //-- Folder app / Images
    private val folderRoot = "Camera/"
    private val pathImages = "${folderRoot}Images"

    //variable - Code_take_photo
    private val codeTakePhoto = 200

    //variable imageView
    private lateinit var imageView: ImageView

    //variable name image
    private lateinit var tempStamp: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Permission in runtime
        //getPermission()
        val btn = findViewById<Button>(R.id.btnCamera)
        imageView = findViewById(R.id.ivPhoto)

        btn.setOnClickListener {
            //get Permissions in context
            getPermission()

        }
        newMethod()
    }

    private fun newMethod() {
        Toast.makeText(this, "New method", Toast.LENGTH_LONG).show()
    }

    private val requestCodePhoto = 200
    private fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePhotoIntent ->
            takePhotoIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    //Create the image file
                    createFileImage()
                } catch (e: Exception) {
                    null
                }

                photoFile?.also { fileImage ->
                    val uriPhoto: Uri = FileProvider.getUriForFile(
                        this, "com.viajero.cmara", fileImage
                    )
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriPhoto)
                    startActivityForResult(takePhotoIntent, requestCodePhoto)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == codeTakePhoto) {
            // the photo was taken successfully
            MediaScannerConnection.scanFile(this, arrayOf(currentPath), null) { _, _ ->
                Log.i("TAG", "Image saved")
            }
            val bitmap = BitmapFactory.decodeFile(currentPath)
            imageView.setImageBitmap(bitmap)
        }
    }

    private lateinit var currentPath: String
    private fun createFileImage(): File? {

        //Create path folder
        val storageDir = File(getExternalStorageDirectory(), pathImages)

        //Check if directory exist
        var directoryExist: Boolean = storageDir.exists()
        //case false create
        if (!directoryExist) {
            directoryExist = storageDir.mkdirs()
        }
        if (directoryExist) {
            //Create the image name
            tempStamp =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        }

        //return a file image and get path
        return File.createTempFile(
            "IMG_${tempStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPath = absolutePath
        }
    }

    private val requestCodePermission = 1000
    private fun getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED -> {
                    //The permissions were granted
                    takePhoto()
                }
                else -> {
                    //The permissions not yet granted
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) &&
                        shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ) {
                        Toast.makeText(this, "The permissions are needed", Toast.LENGTH_LONG).show()
                    }
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        requestCodePermission
                    )
                }
            }
        } else {
            //the device version android is < M
            //Run the method
            takePhoto()
        }
    }

    private val requestCode = 1000
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                takePhoto()
                Log.d("TAG", "Success")
            } else {
                Log.d("TAG", "Failed")
            }
        }
    }
}