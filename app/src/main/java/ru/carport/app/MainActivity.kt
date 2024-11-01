@file:Suppress("DEPRECATION")

package ru.carport.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import ru.carport.app.presenters.WebViewPresenter
import java.io.DataOutputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var webViewPresenter: WebViewPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        App.getComponent().injectActivity(this)

        webViewPresenter.prepareWebView(this)

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token: String ->
            if (!TextUtils.isEmpty(token)) {
                // Get new Instance ID token
                App.fbToken = token
                readCpTokenFromConsole()
            } else {
                Log.w(
                    "getInstanceId failed",
                    "token should not be null..."
                )
            }
        }.addOnFailureListener { _: java.lang.Exception? -> }.addOnCanceledListener {}
            .addOnCompleteListener { task: Task<String> ->
                Log.v(
                    "getInstanceId failed",
                    "This is the token : " + task.result
                )
            }

//        FirebaseInstanceId.getInstance().instanceId
//            .addOnCompleteListener(OnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    Log.w( "getInstanceId failed", task.exception)
//                    return@OnCompleteListener
//                }
//
//                // Get new Instance ID token
//                val token = task.result?.token
//                App.fbToken = token.toString()
//                readCpTokenFromConsole()
//                // getCarPortToken(msg) //old ver
//
//                // Log and toast
//                // Log.d("", fbToken)
//                // Toast.makeText(baseContext, fbToken, Toast.LENGTH_SHORT).show()
//            })

        onNewIntent(intent)
    }


    private fun readCpTokenFromConsole() {
        webViewPresenter.webView.webChromeClient = object : WebChromeClient() {
            @Suppress("DEPRECATION")
            @Deprecated("Deprecated in Java")
            override fun onConsoleMessage(message: String, lineNumber: Int, sourceID: String) {
                val t = "\"cpToken\":"
                if(message.startsWith(t)) {
                    Log.d(
                        "MyApplication", message + " -- From line "
                                + lineNumber + " of "
                                + sourceID
                    )
                    App.cpToken = message.substring(t.length)
                    val mt = MyTask()
                    mt.execute(App.cpToken, App.fbToken)
//                    sendPost(cpToken, fbToken.toString())
                }
            }
        }
    }

    override fun onBackPressed() {
        if (webViewPresenter.webView.canGoBack()) webViewPresenter.webView.goBack()
        else super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("onActivityResult","onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        webViewPresenter.onActivityResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(data: Intent) {
        super.onNewIntent(data)
        intent = data
        if (intent != null && intent.extras != null) {
            if (intent.extras!!.containsKey("order_id")) {
                val orderId = intent.getStringExtra("order_id")

                if (orderId != "0") {
                    webViewPresenter.gotoUrl(getString(R.string.home_url) + getString(R.string.order_id_url) + orderId)
                }
                else {
                    webViewPresenter.gotoUrl(getString(R.string.home_url) + getString(R.string.notifications_url))
                }
            }
            else if (intent.extras!!.containsKey("android_channel_id")) {
                val channel = intent.getStringExtra("android_channel_id")
                if (channel == "chat") {
                    webViewPresenter.gotoUrl(getString(R.string.home_url) + getString(R.string.notifications_url)+getString(R.string.open_chat_param))
                }
                else {
                    webViewPresenter.gotoUrl(getString(R.string.home_url) + getString(R.string.notifications_url))
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class MyTask : AsyncTask<String, String, String>() {

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: String): String? {
            try {
                sendPost(params[0], params[1])
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            return null
        }

        private fun sendPost(cpToken: String, fbToken: String) {
            val body = "{\"token\": \"$fbToken\", \"tokenType\":2}"
            val postData: ByteArray = body.toByteArray(StandardCharsets.UTF_8)
            // "https://devcp.datcar.ru/push/deviceToken"
            val url = getString(R.string.host_url) + getString(R.string.subscribe_url)
            val obj = URL(url)
            val con = obj.openConnection() as HttpsURLConnection
            con.requestMethod = "POST"
            con.setRequestProperty("charset", "utf-8")
            con.setRequestProperty("Authorization", "Bearer $cpToken")
            con.setRequestProperty("Content-length", postData.size.toString())
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            con.setRequestProperty("Accept", "application/json")

            // Send post request
            con.doOutput = true
            try {
                val wr = DataOutputStream(con.outputStream)
                wr.write(postData)
                wr.flush()
                //val responseCode = con.responseCode
            }
            catch (exception: Exception) {
                Log.d("", exception.toString())

            }
        }
    }

}