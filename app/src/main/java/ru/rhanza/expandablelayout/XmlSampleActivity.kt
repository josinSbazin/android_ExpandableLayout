package ru.rhanza.expandablelayout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class XmlSampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //You can setup ConstraintExpandableLayout in xml
        setContentView(R.layout.activity_xml_sample)
    }
}
