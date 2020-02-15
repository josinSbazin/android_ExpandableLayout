package ru.rhanza.expandablelayout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_custom_shadow_drawable.*

class CustomShadowDrawableActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_shadow_drawable)

        content2.shadowDrawable = R.drawable.fade_out2
    }
}
