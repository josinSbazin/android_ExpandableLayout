package ru.rhanza.constraintexpandablelayout

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.ColorInt
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.transition.ChangeBounds
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class ExpandableLayout : ConstraintLayout {
    private val contentView: LinearLayout
    private val moreTextView: TextView
    private val moreImageView: ImageView
    private val shadow: View

    private var dontInterceptAddView = true

    private lateinit var collapsedSet: ConstraintSet
    private lateinit var expandedSet: ConstraintSet

    private lateinit var transition: TransitionSet

    /** Current [State] of this [ExpandableLayout]. Read-only property. [State.Statical] by default. */
    var state = State.Statical
        private set(value) {
            if (field != value) {
                onStateChangeListener?.invoke(field, value)
                field = value
            }
        }

    /**
     * Invoke when [State] changed
     */
    var onStateChangeListener: ((oldState: State, newState: State) -> Unit)? = null

    /**
     * Collapsed height of view. WARNING! Don't set [collapsedHeight] less, then maximum height of wrapped view
     */
    var collapsedHeight = context.resources.getDimensionPixelSize(R.dimen.default_collapsed_height)
        set(value) {
            doOnGlobalLayout {
                val maxHeight = contentView.getLayoutMaxHeight()
                if (value > maxHeight) {
                    throw IllegalArgumentException("CollapsedHeight must be less then max height (unwrapped) of expandable layout. \nUnwrapped height - $maxHeight\ncollapsedHeight - $collapsedHeight")
                }
            }
            contentView.layoutParams.height = value
            shadow.requestLayout()
            collapsedSet.constrainHeight(R.id.evHolder, value)
            field = value
        }

    /**
     * Height of shadow when layout is collapsed
     */
    var shadowHeight = context.resources.getDimensionPixelSize(R.dimen.default_shadow_height)
        set(value) {
            shadow.layoutParams.height = value
            shadow.requestLayout()
            collapsedSet.constrainHeight(R.id.evShadow, value)
            expandedSet.constrainHeight(R.id.evShadow, value)
            field = value
        }

    /**
     * If this parameter is true - show shadow in collapsed [State]
     */
    var showShadow = DEFAULT_SHOW_SHADOW_VALUE
        set(value) {
            val visibility = if (value) {
                View.VISIBLE
            } else {
                shadow.visibility = View.INVISIBLE
                View.INVISIBLE
            }
            collapsedSet.setVisibility(R.id.evShadow, visibility)
            field = value
        }

    /**
     * Hide default collapse/expand button. Use if you want make custom button
     */

    var hideButton = DEFAULT_HIDE_BUTTON_VALUE
        set(value) {
            val visibility = if (value) {
                View.GONE
            } else {
                View.VISIBLE
            }

            moreImageView.visibility = visibility
            moreTextView.visibility = visibility

            collapsedSet.setVisibility(R.id.evMoreImage, visibility)
            collapsedSet.setVisibility(R.id.evMoreText, visibility)
            expandedSet.setVisibility(R.id.evMoreImage, visibility)
            expandedSet.setVisibility(R.id.evMoreText, visibility)
            field = value
        }

    /**
     * Text showing on more button
     */
    var moreText: CharSequence = context.getText(R.string.defaultMoreText)
        set(value) {
            moreTextView.text = value
            field = value
        }

    /**
     * Duration of animation of collapse/expand. In milliseconds
     */
    var animationDuration = context.resources.getInteger(R.integer.default_animation_duration)
        set(value) {
            if (!isInEditMode && field != value) {
                transition = createTransitionSet(value.toLong())
            }
            field = value
        }

    /**
     * Color of more button (text and arrow)
     */
    @ColorInt
    var moreColor = ContextCompat.getColor(context, R.color.defaultMoreColor)
        set(value) {
            setupMoreColor(value)
            field = value
        }

    /**
     * Animation scene root for transition. Use for animate container for this view.
     * Do not save when [ExpandableLayout] paralyzed. Set it manually when restore.
     * Default is parent of this view, or self if parent is invalid
     */
    var animationSceneRoot: ViewGroup? = null

    /**
     * Toggle [ExpandableLayout] state. Ignore if [State.Statical]
     * @param [withAnimation] should it toggle with animation or instantaneously. **true** by default.
     */
    fun toggle(withAnimation: Boolean = true) {
        when (state) {
            State.Collapsed, State.Collapsing -> expand(withAnimation)
            State.Expanded, State.Expanding -> collapse(withAnimation)
            else -> return
        }
    }

    /**
     * Collapse [ExpandableLayout]. Ignore if [State.Statical]
     * @param [withAnimation] should it collapse with animation or instantaneously. **false** by default.
     * @param [withAnimation] should it collapse in any state forced. **true** by default.
     */
    fun collapse(withAnimation: Boolean = true, forced: Boolean = false) {
        if (!forced && (state == State.Collapsed || state == State.Expanding || state == State.Collapsing)) {
            return
        }
        if (withAnimation) {
            state = State.Collapsing
            transition.setOnEndListener {
                state = State.Collapsed
            }
            val parent = animationSceneRoot ?: this.parent as? ViewGroup ?: this
            TransitionManager.beginDelayedTransition(parent, transition)
        }
        collapsedSet.applyTo(this)
    }

    /**
     * Collapse [ExpandableLayout]. Ignore if [State.Statical]
     * @param [withAnimation] should it expand with animation or instantaneously. **false** by default.
     * @param [withAnimation] should it expand in any state forced. **true** by default.
     */
    fun expand(withAnimation: Boolean = true, forced: Boolean = false) {
        if (!forced && (state == State.Expanded || state == State.Expanding || state == State.Collapsing)) return
        if (withAnimation) {
            state = State.Expanding
            transition.setOnEndListener {
                state = State.Expanded
            }
            val parent = animationSceneRoot ?: this.parent as? ViewGroup ?: this
            TransitionManager.beginDelayedTransition(parent, transition)
        }
        expandedSet.applyTo(this)
    }


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.expandable_layout, this)
        contentView = findViewById(R.id.evHolder)
        moreTextView = findViewById(R.id.evMoreText)
        moreImageView = findViewById(R.id.evMoreImage)
        shadow = findViewById(R.id.evShadow)
        dontInterceptAddView = false
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (dontInterceptAddView) {
            super.addView(child, index, params)
        } else {
            contentView.addView(child, index, params)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearCached()
    }

    //region Private Methods

    private fun init(attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ExpandableLayout, defStyleAttr, defStyleRes)

        expandedSet = ConstraintSet().apply {
            clone(context, R.layout.expandable_layout_expanded)
        }
        collapsedSet = ConstraintSet().apply {
            clone(context, R.layout.expandable_layout_collapsed)
        }

        typedArray.apply {
            collapsedHeight = getDimensionPixelSize(
                R.styleable.ExpandableLayout_el_collapsedHeight,
                context.resources.getDimensionPixelSize(R.dimen.default_collapsed_height)
            )

            shadowHeight = getDimensionPixelSize(
                R.styleable.ExpandableLayout_el_shadowHeight,
                context.resources.getDimensionPixelSize(R.dimen.default_shadow_height)
            )

            showShadow = getBoolean(R.styleable.ExpandableLayout_el_showShadow, DEFAULT_SHOW_SHADOW_VALUE)

            hideButton = getBoolean(R.styleable.ExpandableLayout_el_hideButton, DEFAULT_HIDE_BUTTON_VALUE)

            val moreTextStyleable = getText(R.styleable.ExpandableLayout_el_moreText)
            if (moreTextStyleable != null) {
                moreText = moreTextStyleable
            }

            animationDuration =
                    getInt(
                        R.styleable.ExpandableLayout_el_animationDuration,
                        context.resources.getInteger(R.integer.default_animation_duration)
                    )

            moreColor = getColor(
                R.styleable.ExpandableLayout_el_moreColor,
                ContextCompat.getColor(context, R.color.defaultMoreColor)
            )

            state = State.values()[getInt(R.styleable.ExpandableLayout_el_initialState, DEFAULT_STATE)]
        }

        typedArray.recycle()

        moreTextView.setOnClickListener { toggle(true) }
        moreImageView.setOnClickListener { toggle(true) }

        post {
            updateState(state)
        }
    }

    private fun clearCached() {
        animationSceneRoot = null
    }

    private fun createTransitionSet(animationDuration: Long) = TransitionSet().apply {
        addTransition(ChangeBounds())
        addTransition(Fade())
        addTransition(Rotate())
        ordering = android.support.transition.TransitionSet.ORDERING_TOGETHER
        duration = animationDuration
    }

    private fun setupMoreColor(@ColorInt color: Int) {
        moreTextView.setTextColor(color)
        DrawableCompat.setTint(moreImageView.drawable, color)
    }

    private fun update(restoredState: SavedState) {
        collapsedHeight = restoredState.collapsedHeight
        shadowHeight = restoredState.shadowHeight
        showShadow = restoredState.showShadow
        hideButton = restoredState.hideButton
        animationDuration = restoredState.animationDuration
        moreColor = restoredState.moreColor
        moreText = restoredState.moreText
        updateState(restoredState.state)
    }

    private fun updateState(state: State) {
        when (state) {
            State.Collapsed, State.Collapsing -> collapse(withAnimation = false, forced = true)
            State.Expanded, State.Expanding -> expand(withAnimation = false, forced = true)
            State.Statical -> makeStatical()
        }
    }

    private fun makeStatical() {
        expandedSet.applyTo(this)
        state = State.Statical
    }

    //endregion

    //region Save State

    override fun onSaveInstanceState(): Parcelable = SavedState(super.onSaveInstanceState()).apply {
        state = this@ExpandableLayout.state
        collapsedHeight = this@ExpandableLayout.collapsedHeight
        shadowHeight = this@ExpandableLayout.shadowHeight
        showShadow = this@ExpandableLayout.showShadow
        hideButton = this@ExpandableLayout.hideButton
        animationDuration = this@ExpandableLayout.animationDuration
        moreColor = this@ExpandableLayout.moreColor
        moreText = this@ExpandableLayout.moreText
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(state.superState)

        post {
            update(restoredState = ss)
        }
    }

    private class SavedState : BaseSavedState {
        var state: State = State.Collapsed
        var collapsedHeight: Int = -1
        var shadowHeight: Int = -1
        var animationDuration: Int = -1
        var moreColor: Int = -1
        var hideButton: Boolean = false
        var showShadow: Boolean = false
        var moreText: CharSequence = ""

        constructor(superState: Parcelable?) : super(superState)
        constructor(source: Parcel) : super(source) {
            state = State.values()[source.readInt()]
            collapsedHeight = source.readInt()
            shadowHeight = source.readInt()
            animationDuration = source.readInt()
            moreColor = source.readInt()
            hideButton = source.readInt() == 1
            showShadow = source.readInt() == 1
            moreText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source)
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(state.ordinal)
            dest.writeInt(collapsedHeight)
            dest.writeInt(shadowHeight)
            dest.writeInt(animationDuration)
            dest.writeInt(moreColor)
            dest.writeInt(if (hideButton) 1 else 0)
            dest.writeInt(if (showShadow) 1 else 0)
            TextUtils.writeToParcel(moreText, dest, flags)
        }

        companion object {
            @Suppress("unused")
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel) = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    //endregion

    companion object {
        private const val DEFAULT_SHOW_SHADOW_VALUE = false
        private const val DEFAULT_HIDE_BUTTON_VALUE = false
        private const val DEFAULT_STATE = 0
    }
}

enum class State {
    Collapsed,
    Expanded,
    Statical,
    Collapsing,
    Expanding
}