package ru.rhanza.constraintexpandablelayout

import android.support.transition.Transition

inline fun Transition.setOnEndListener(crossinline action: () -> Unit) {
    this.addListener(object : Transition.TransitionListener {
        override fun onTransitionEnd(p0: Transition) {
            action.invoke()
            p0.removeListener(this)
        }

        override fun onTransitionResume(p0: Transition) {
            //NA
        }

        override fun onTransitionPause(p0: Transition) {
            //NA
        }

        override fun onTransitionCancel(p0: Transition) {
            //NA
        }

        override fun onTransitionStart(p0: Transition) {
            //NA
        }
    })
}