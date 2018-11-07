package ru.rhanza.constraintexpandablelayout

import android.os.Build
import android.support.transition.Transition
import android.view.View
import android.view.ViewTreeObserver

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

fun View.getLayoutMaxHeight(): Int {
    this.measure(
        View.MeasureSpec.makeMeasureSpec(this.measuredWidth, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    return this.measuredHeight
}

inline fun <V> V.doOnGlobalLayout(crossinline work: (view: V) -> Unit) where V : View {
    this.viewTreeObserver.addOnGlobalLayoutListener(
        object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    this@doOnGlobalLayout.viewTreeObserver
                        .removeOnGlobalLayoutListener(this)
                } else {
                    @Suppress("DEPRECATION")
                    this@doOnGlobalLayout.viewTreeObserver
                        .removeGlobalOnLayoutListener(this)
                }
                work.invoke(this@doOnGlobalLayout)
            }
        })
}