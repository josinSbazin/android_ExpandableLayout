package ru.rhanza.expandablelayout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_programmatically_sample.*
import kotlinx.android.synthetic.main.include_custom_button.view.*
import ru.rhanza.constraintexpandablelayout.State

class ProgrammaticallySampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programmatically_sample)
        //You can setup ConstraintExpandableLayout programmatically
        content.showButton = false
        content.showShadow = true
        content.animationDuration = 300
        content.collapsedHeight = 120
        button.setOnClickListener { collapseExpandWithAnimation() }
    }

    fun collapseExpandWithAnimation() {
        if (content.state == State.Collapsed) {
            content.toggle()
            button.moreImage.rotation = 180f
        } else if (content.state == State.Expanded) {
            content.toggle()
            button.moreImage.rotation = 0f
        }
    }
}
