package ru.rhanza.expandablelayout

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_transition_parent.*
import ru.rhanza.constraintexpandablelayout.ExpandableLayout

class TransitionParentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transition_parent)

        //Use it for correct group animation relative to parent
        (first as ExpandableLayout).animationSceneRoot = container
        (second as ExpandableLayout).animationSceneRoot = container
        (third as ExpandableLayout).animationSceneRoot = container
    }
}
