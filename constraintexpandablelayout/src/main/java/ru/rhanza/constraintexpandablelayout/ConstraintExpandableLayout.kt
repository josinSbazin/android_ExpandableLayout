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

class ConstraintExpandableLayout : ConstraintLayout {
    private val contentView: LinearLayout
    private val moreTextView: TextView
    private val moreImageView: ImageView
    private val shadow: View

    private var dontInterceptAddView = true

    private lateinit var collapsedSet: ConstraintSet
    private lateinit var expandedSet: ConstraintSet

    private lateinit var transition: TransitionSet

    /** Current [State] of this [ConstraintExpandableLayout]. Read-only property. [State.Statical] by default. */
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

            collapsedSet.constrainHeight(R.id.evHolder, value)
            field = value
        }

    /**
     * Height of shadow when layout is collapsed
     */
    var shadowHeight = context.resources.getDimensionPixelSize(R.dimen.default_shadow_height)
        set(value) {
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
                View.INVISIBLE
            }
            collapsedSet.setVisibility(R.id.evShadow, visibility)
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
            if (!isInEditMode) {
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
     * Do not save when [ConstraintExpandableLayout] paralyzed. Set it manually when restore.
     */
    var animationSceneRoot: ViewGroup? = null

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
            context.obtainStyledAttributes(attrs, R.styleable.ConstraintExpandableLayout, defStyleAttr, defStyleRes)

        expandedSet = ConstraintSet().apply {
            clone(context, R.layout.expandable_layout_expanded)
        }
        collapsedSet = ConstraintSet().apply {
            clone(context, R.layout.expandable_layout_collapsed)
        }

        typedArray.apply {
            collapsedHeight = getDimensionPixelSize(
                R.styleable.ConstraintExpandableLayout_el_collapsedHeight,
                context.resources.getDimensionPixelSize(R.dimen.default_collapsed_height)
            )

            shadowHeight = getDimensionPixelSize(
                R.styleable.ConstraintExpandableLayout_el_shadowHeight,
                context.resources.getDimensionPixelSize(R.dimen.default_shadow_height)
            )

            showShadow = getBoolean(R.styleable.ConstraintExpandableLayout_el_showShadow, DEFAULT_SHOW_SHADOW_VALUE)

            val moreTextStyleable = getText(R.styleable.ConstraintExpandableLayout_el_moreText)
            if (moreTextStyleable != null) {
                moreText = moreTextStyleable
            }

            animationDuration =
                    getInt(
                        R.styleable.ConstraintExpandableLayout_el_animationDuration,
                        context.resources.getInteger(R.integer.default_animation_duration)
                    )

            moreColor = getColor(
                R.styleable.ConstraintExpandableLayout_el_moreColor,
                ContextCompat.getColor(context, R.color.defaultMoreColor)
            )

            state = State.values()[getInt(R.styleable.ConstraintExpandableLayout_el_initialState, DEFAULT_STATE)]
        }

        typedArray.recycle()

        moreTextView.setOnClickListener { toggle() }
        moreImageView.setOnClickListener { toggle() }

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
        updateState(restoredState.state)
        collapsedHeight = restoredState.collapsedHeight
        shadowHeight = restoredState.shadowHeight
        showShadow = restoredState.showShadow
        animationDuration = restoredState.animationDuration
        moreColor = restoredState.moreColor
        moreText = restoredState.moreText
    }

    private fun updateState(state: State) {
        when (state) {
            State.Collapsed, State.Collapsing -> collapse(withAnimation = false, forced = true)
            State.Expanded, State.Expanding -> expand(withAnimation = false, forced = true)
            State.Statical -> makeStatical()
        }
    }

    private fun collapse(withAnimation: Boolean = true, forced: Boolean = false) {
        if (!forced && (state == State.Collapsed || state == State.Expanding || state == State.Collapsing)) {
            return
        }
        if (withAnimation) {
            state = State.Collapsing
            transition.setOnEndListener {
                state = State.Collapsed
            }
            TransitionManager.beginDelayedTransition(animationSceneRoot ?: this, transition)
        }
        collapsedSet.applyTo(this)
    }

    private fun expand(withAnimation: Boolean = true, forced: Boolean = false) {
        if (!forced && (state == State.Expanded || state == State.Expanding || state == State.Collapsing)) return
        if (withAnimation) {
            state = State.Expanding
            transition.setOnEndListener {
                state = State.Expanded
            }
            TransitionManager.beginDelayedTransition(animationSceneRoot ?: this, transition)
        }
        expandedSet.applyTo(this)
    }

    private fun makeStatical() {
        state = State.Statical
    }

    private fun toggle() {
        when (state) {
            State.Collapsed, State.Collapsing -> expand()
            State.Expanded, State.Expanding -> collapse()
            else -> return
        }
    }

    //endregion

    //region Save State

    override fun onSaveInstanceState(): Parcelable = SavedState(super.onSaveInstanceState()).apply {
        state = this@ConstraintExpandableLayout.state
        collapsedHeight = this@ConstraintExpandableLayout.collapsedHeight
        shadowHeight = this@ConstraintExpandableLayout.shadowHeight
        showShadow = this@ConstraintExpandableLayout.showShadow
        animationDuration = this@ConstraintExpandableLayout.animationDuration
        moreColor = this@ConstraintExpandableLayout.moreColor
        moreText = this@ConstraintExpandableLayout.moreText
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
        var showShadow: Boolean = false
        var moreText: CharSequence = ""

        constructor(superState: Parcelable?) : super(superState)
        constructor(source: Parcel) : super(source) {
            state = State.values()[source.readInt()]
            collapsedHeight = source.readInt()
            shadowHeight = source.readInt()
            animationDuration = source.readInt()
            moreColor = source.readInt()
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