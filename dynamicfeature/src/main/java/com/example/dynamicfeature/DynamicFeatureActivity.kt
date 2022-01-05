package com.example.dynamicfeature

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat

class DynamicFeatureActivity : AppCompatActivity() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // Emulates installation of on demand modules using SplitCompat.
        SplitCompat.installActivity(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_feature)
    }
}