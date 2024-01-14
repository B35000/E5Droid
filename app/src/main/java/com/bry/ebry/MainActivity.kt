package com.bry.ebry

import android.Manifest
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.*
import android.webkit.WebChromeClient.FileChooserParams
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var imageView: ImageView
    lateinit var webkitPermissionRequest : PermissionRequest

    var uploadMessage: ValueCallback<Array<Uri?>?>? = null
    var mUploadMessage: ValueCallback<Uri?>? = null
    val REQUEST_SELECT_FILE = 100
    private val FILECHOOSER_RESULTCODE = 1
    private var context: Context? = null;
    private var pressedTime: Long = 0
    private var TAG = "MainActivity"
    private val PERMISSION_REQUEST_CODE = 200
    private val CHANNEL_ID = "35"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this.applicationContext

        window.navigationBarColor = resources.getColor(R.color.dark)

        webView = findViewById(R.id.webview)
        imageView = findViewById(R.id.imageView)

        val settings: WebSettings = webView.getSettings()
        settings.domStorageEnabled = true

        webView.webViewClient = WebViewClient()
        webView.setWebChromeClient(MyWebChromeClient())
        webView.loadUrl("https://b35000.github.io/E5UI/")
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.settings.allowFileAccess=true
        webView.settings.allowContentAccess=true
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setSupportZoom(true)

        notify_permanent_notification()
    }


    inner class WebViewClient : android.webkit.WebViewClient() {
        // Load the URL
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
//            view.loadUrl(url)
            val url: String = request.getUrl().toString();
            if (url.contains("https://b35000.github.io/E5UI/")) {
                view.loadUrl(url)
            } else {
                val intent = Intent(Intent.ACTION_VIEW, request.url)
                startActivity(intent)
            }
            return false
        }

        // ProgressBar will disappear once page is loaded
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            Handler().postDelayed(Runnable {
                webView.visibility = View.VISIBLE
                imageView.visibility = View.INVISIBLE
            }, 1000)
        }


    }

    inner class MyWebChromeClient : WebChromeClient() {
        protected fun openFileChooser(uploadMsg: ValueCallback<Uri?>, acceptType: String?) {
            mUploadMessage = uploadMsg
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "image/*"
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE)
        }


        // For Lollipop 5.0+ Devices
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onShowFileChooser(
            mWebView: WebView?,
            filePathCallback: ValueCallback<Array<Uri?>?>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            if (uploadMessage != null) {
                uploadMessage?.onReceiveValue(null)
                uploadMessage = null
            }
            uploadMessage = filePathCallback
            val intent = fileChooserParams.createIntent()
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE)
            } catch (e: ActivityNotFoundException) {
                uploadMessage = null
                Toast.makeText(context, "Cannot Open File Chooser", Toast.LENGTH_LONG).show()
                return false
            }
            return true
        }

        //For Android 4.1 only
        protected fun openFileChooser(
            uploadMsg: ValueCallback<Uri?>,
            acceptType: String?,
            capture: String?
        ) {
            mUploadMessage = uploadMsg
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(intent, "File Chooser"),
                FILECHOOSER_RESULTCODE
            )
        }

        protected fun openFileChooser(uploadMsg: ValueCallback<Uri?>) {
            mUploadMessage = uploadMsg
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "image/*"
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE)
        }

        override fun onPermissionRequest(request: PermissionRequest) {
            if(!checkPermission()){
                requestPermission()
            }
            request.grant(request.resources)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) return
                uploadMessage!!.onReceiveValue(FileChooserParams.parseResult(resultCode, intent))
                uploadMessage = null
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            val result =
                if (intent == null || resultCode != RESULT_OK) null else intent.data
            mUploadMessage!!.onReceiveValue(result)
            mUploadMessage = null
        } else Toast.makeText(context, "Failed to Upload Image", Toast.LENGTH_LONG).show()
    }


    override fun onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        pressedTime = System.currentTimeMillis()
    }


    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(35)
    }


    fun notify_permanent_notification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "E5 NOTIFICATION"
            val descriptionText = "E5 Notification"
            val importance = NotificationManager.IMPORTANCE_NONE
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        var builder = NotificationCompat.Builder(context!!, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notif)
            .setContentTitle("E5 Running.")
            .setContentText("E5 is running in the background.")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setAutoCancel(false)
            .setOngoing(true)

        with(NotificationManagerCompat.from(context!!)) {
            notify(35, builder.build())
        }
    }
}