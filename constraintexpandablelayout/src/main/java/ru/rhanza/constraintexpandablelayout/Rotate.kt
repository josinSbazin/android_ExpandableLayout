package ru.rhanza.constraintexpandablelayout

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.os.Build
import android.support.transition.Transition
import android.support.transition.TransitionValues
import android.view.View
import android.view.ViewGroup

/**
 * This transition captures the rotation property of targets before and after
 * the scene change and animates any changes.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class Rotate : Transition() {

    override fun captureStartValues(transitionValues: TransitionValues) {
        transitionValues.values[PROPNAME_ROTATION] = transitionValues.view.rotation;
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        transitionValues.values[PROPNAME_ROTATION] = transitionValues.view.rotation;
    }

    override fun createAnimator(
        sceneRoot: ViewGroup, startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }
        val view = endValues.view
        val startRotation = startValues.values[PROPNAME_ROTATION] as Float
        val endRotation = endValues.values[PROPNAME_ROTATION] as Float
        if (startRotation != endRotation) {
            view.rotation = startRotation
            return ObjectAnimator.ofFloat(
                view, View.ROTATION,
                startRotation, endRotation
            )
        }
        return null
    }

    companion object {
        private const val PROPNAME_ROTATION = "android:rotate:rotation"
    }
}