package ru.rhanza.expandablelayout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_programmatically_sample.*

class ProgrammaticallySampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programmatically_sample)
        //You can setup ConstraintExpandableLayout programmatically
        content.showButton = false
        content.showShadow = true
        content.animationDuration = 300
        content.collapsedHeight = 120
        button.setOnClickListener { content.toggle() }
    }
}
