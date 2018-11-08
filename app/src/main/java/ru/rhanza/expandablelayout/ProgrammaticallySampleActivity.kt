package ru.rhanza.expandablelayout

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_programmatically_sample.*

class ProgrammaticallySampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programmatically_sample)
        //You can setup ConstraintExpandableLayout programmatically
        content.hideButton = true
        content.showShadow = true
        content.animationDuration = 300
        content.collapsedHeight = 120
        button.setOnClickListener { content.toggle() }
    }
}
