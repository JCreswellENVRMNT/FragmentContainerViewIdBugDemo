package com.example.fcvidbugdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import timber.log.Timber
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var dynamicFeatureSessionId = 0
    private var splitInstallManager: SplitInstallManager? = null

    // Creates a listener for request status updates.
    var dynamicFeatureListener =
        SplitInstallStateUpdatedListener { state: SplitInstallSessionState ->
            if (state.sessionId() == dynamicFeatureSessionId) {
                when (state.status()) {
                    SplitInstallSessionStatus.DOWNLOADING -> {
                        val totalBytes = state.totalBytesToDownload()
                        val progress = state.bytesDownloaded()
                        Timber.d("dynamic feature download progress: %s%%", progress / totalBytes * 100)
                    }
                    SplitInstallSessionStatus.INSTALLED -> {
                        Timber.d("dynamic feature module installed")
                        startActivity(
                            Intent().setClassName(
                                applicationContext.packageName,
                                "com.example.dynamicfeature.DynamicFeatureActivity"
                            )
                        )
                    }
                    else -> Timber.d("dynamic feature req status update: %s", state.status())
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        setContentView(R.layout.activity_main)
        splitInstallManager = SplitInstallManagerFactory.create(this)
        splitInstallManager!!.registerListener(dynamicFeatureListener)
        val button: Button = findViewById(R.id.btn_req_dynamic_feature)
        button.setOnClickListener {
            val req = SplitInstallRequest.newBuilder()
                .addModule("dynamicfeature")
                .build()
            splitInstallManager!!.startInstall(req)
                .addOnSuccessListener { sessionId: Int ->
                    Timber.d("dynamic feature req succeeded")
                    dynamicFeatureSessionId = sessionId
                }
                .addOnFailureListener { exception: Exception? ->
                    Timber.e(
                        exception,
                        "dynamic feature module install req failed"
                    )
                }
        }
    }
}