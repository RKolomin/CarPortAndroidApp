package ru.carport.app.viewComponents

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ru.carport.app.MainActivity
import ru.carport.app.presenters.WebViewPresenter
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CarPortWebChromeClient : WebChromeClient() {

    @Inject
    lateinit var mainActivity: MainActivity

    lateinit var webViewPresenter: WebViewPresenter

    var mCameraPhotoPath: String? = null
    
    override fun onShowFileChooser(
        mWebView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        Log.i("onShowFileChooser", "onShowFileChooser")
        if (checkRequiredPermissions()) {
            webViewPresenter.uploadMessage?.onReceiveValue(null)
            webViewPresenter.uploadMessage = null
            webViewPresenter.uploadMessage = filePathCallback

            var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent!!.resolveActivity(mainActivity.packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                } catch (ex: IOException) {
                    Log.e("onShowFileChooser", ex.toString())
                }

                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.absolutePath
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                } else {
                    takePictureIntent = null
                }
            }

            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            contentSelectionIntent.type = "image/*"
            val intentArray: Array<Intent> = if (takePictureIntent != null) {
                arrayOf(takePictureIntent)
            } else {
                arrayOf()
            }

            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
            mainActivity.startActivityForResult(Intent.createChooser(chooserIntent, "Select images"), 1)

            return true
        } else {
            return false
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        @SuppressLint("SimpleDateFormat") val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
        callback?.invoke(origin, true, false)
    }


    //------------------------------------------------------------------------
    /*-- checking and asking for required file permissions --*/
    private fun checkRequiredPermissions(): Boolean {
        Log.i("file_permission", "called file_permission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && (ContextCompat.checkSelfPermission(
                mainActivity, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        ) != PackageManager.PERMISSION_GRANTED
                    )) {
            ActivityCompat.requestPermissions(
                mainActivity,
                arrayOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED, Manifest.permission.CAMERA),
                1
            )
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && (ContextCompat.checkSelfPermission(
                mainActivity, Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
                    )) {
//            Log.e("", "SDK_INT >= 33, No permission: READ_MEDIA_IMAGES, CAMERA")
            ActivityCompat.requestPermissions(
                mainActivity,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA),
                1
            )
            return false
        }
        else if (ContextCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
//            Log.e("", "SDK_INT >= 23, No permission: WRITE_EXTERNAL_STORAGE, CAMERA")
            ActivityCompat.requestPermissions(
                mainActivity/*this@MainActivity*/,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                1
            )
            return false
        } else {
            return true
        }
    }
}