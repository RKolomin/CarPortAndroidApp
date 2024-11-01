package ru.carport.app.presenters

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import ru.carport.app.viewComponents.CarPortWebChromeClient
import ru.carport.app.viewComponents.CarPortWebView
import ru.carport.app.App
import ru.carport.app.MainActivity
import ru.carport.app.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class WebViewPresenter {
    @Inject
    lateinit var webChromeClient: CarPortWebChromeClient

    @Inject
    lateinit var webViewClient: CarPortWebView

    @Inject
    lateinit var managePermissions: ManagePermissionsPresenter

    companion object {
        const val PermissionsRequestCode = 100
        fun getPermissionList(): List<String> {
            Log.i("Build.VERSION.SDK_INT", Build.VERSION.SDK_INT.toString())
            val result = arrayListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.add(Manifest.permission.POST_NOTIFICATIONS)
            }

//          Access Storage / Gallery / Images
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                result.add(Manifest.permission.READ_MEDIA_IMAGES)
//                result.add(Manifest.permission.READ_MEDIA_VIDEO)
                result.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.add(Manifest.permission.READ_MEDIA_IMAGES)
//                result.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
            else /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)*/ {
                result.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                result.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            return result
        }
    }

    private lateinit var activity: MainActivity


    lateinit var webView: WebView

    var uploadMessage: ValueCallback<Array<Uri>>? = null


    private val RequestSelectFile = 100

    private val FileChooserResultCode = 1

    @SuppressLint("SetJavaScriptEnabled")
    fun prepareWebView(activity: MainActivity) {

        App.getComponent().injectPresenter(this)

        checkAppPermission(activity)

        this.activity = activity

        webView = activity.findViewById(R.id.wv_car_port)

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.domStorageEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.databaseEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = false
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true

        webChromeClient.webViewPresenter = this
        webChromeClient.mainActivity = activity

        val startUrl = webView.context.getString(R.string.home_url)
        webView.loadUrl(startUrl)

        webView.setWebChromeClient(webChromeClient)
        webView.setWebViewClient(webViewClient)
    }

    var mCameraPhotoPath: String? = null


    @Throws(IOException::class)
    private fun createImageFile(): File {
        @SuppressLint("SimpleDateFormat") val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(
            Date()
        )
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }


    fun gotoUrl(startUrl: String) {
        webView.loadUrl(startUrl)
    }

    private fun checkAppPermission(mainActivity: MainActivity) {
        val list = getPermissionList()
        managePermissions = ManagePermissionsPresenter(mainActivity, list, PermissionsRequestCode)
        managePermissions.checkPermissions()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RequestSelectFile) {
            uploadMessage?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(
                    resultCode,
                    data
                )
            )
            uploadMessage = null
        } else if (requestCode == FileChooserResultCode) {
            var results: Array<Uri> = arrayOf()
            if (data != null) {
                results = arrayOf(Uri.parse(data.dataString))
            } else {
                try {
                    val filePath = webChromeClient.mCameraPhotoPath
                    results = arrayOf(Uri.parse(filePath))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            uploadMessage?.onReceiveValue(results)
            uploadMessage = null
        }
    }
}