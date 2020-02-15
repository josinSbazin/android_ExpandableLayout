package ru.rhanza.expandablelayout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        byXml.setOnClickListener {
            startActivity(Intent(this, XmlSampleActivity::class.java))
        }

        programmatically.setOnClickListener {
            startActivity(Intent(this, ProgrammaticallySampleActivity::class.java))
        }

        transitionParent.setOnClickListener {
            startActivity(Intent(this, TransitionParentActivity::class.java))
        }
    }
}
