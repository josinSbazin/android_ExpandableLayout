package ru.rhanza.constraintexpandablelayout

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.ColorInt
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.transition.ChangeBounds
import android.support.transition.Fade
import android.support.transition.TransitionSet
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
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

    private lateinit var collapsedSet: ConstraintSet
    private lateinit var expandedSet: ConstraintSet

    private lateinit var transition: TransitionSet

    /** Current [State] of this [ConstraintExpandableLayout]. Read-only property. [State.Static] by default. */
    var state = State.Static
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
     * Collapsed height of view
     */
    var collapsedHeight = DEFAULT_COLLAPSED_HEIGHT_PX

    /**
     * Height of shadow when layout is collapsed
     */
    var shadowHeight = DEFAULT_SHADOW_HEIGHT_PX
        set(value) {
            val layoutParams = shadow.layoutParams
            layoutParams.height = value
            shadow.layoutParams = layoutParams
            field = value
        }

    /**
     * If this parameter is true - show shadow in collapsed [State]
     */
    var showShadow = DEFAULT_SHOW_SHADOW_VALUE
        set(value) {
            shadow.visibility = if (showShadow) View.VISIBLE else View.INVISIBLE
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
    var animationDuration = DEFAULT_COLLAPSED_DURATION
        set(value) {
            transition = createTransitionSet(value.toLong())
            field = value
        }

    /**
     * Color of more button (text and arrow)
     */
    @ColorInt
    var moreColor = ContextCompat.getColor(context, R.color.defaultMoreColor)
        set(value) {
            setupMoreColor(moreColor)
            field = value
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
        LayoutInflater.from(context).inflate(R.layout.expandable_layout, this, true)
        contentView = findViewById(R.id.evHolder)
        moreTextView = findViewById(R.id.evMoreText)
        moreImageView = findViewById(R.id.evMoreImage)
        shadow = findViewById(R.id.evShadow)
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        contentView.addView(child, index, params)
    }

    //region Private Methods

    private fun init(attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ConstraintExpandableLayout, defStyleAttr, defStyleRes)

        typedArray.apply {
            collapsedHeight = getDimensionPixelSize(
                R.styleable.ConstraintExpandableLayout_el_collapsedHeight,
                DEFAULT_COLLAPSED_HEIGHT_PX
            )
            shadowHeight = getDimensionPixelSize(
                R.styleable.ConstraintExpandableLayout_el_shadowHeight,
                DEFAULT_SHADOW_HEIGHT_PX
            )
            showShadow = getBoolean(R.styleable.ConstraintExpandableLayout_el_showShadow, DEFAULT_SHOW_SHADOW_VALUE)
            moreText = getText(R.styleable.ConstraintExpandableLayout_el_moreText)
            animationDuration =
                    getInt(R.styleable.ConstraintExpandableLayout_el_animationDuration, DEFAULT_COLLAPSED_DURATION)
            moreColor = getColor(
                R.styleable.ConstraintExpandableLayout_el_moreColor,
                ContextCompat.getColor(context, R.color.defaultMoreColor)
            )
            state = State.values()[getInt(R.styleable.ConstraintExpandableLayout_el_initialState, DEFAULT_STATE)]

        }

        expandedSet = ConstraintSet().apply { clone(this) }
        collapsedSet = expandedSet.apply {
            clone(this)
        }


        typedArray.recycle()
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

    private fun updateState(restoredState: State) {
        when (restoredState) {
            State.Collapsed, State.Collapsing -> collapse(withAnimation = false)
            State.Expanded, State.Expanding -> expand(withAnimation = false)
            State.Static -> makeStatic()
        }
    }

    private fun collapse(withAnimation: Boolean = true) {

    }

    private fun expand(withAnimation: Boolean = true) {

    }

    private fun makeStatic() {

    }

    //endregion

    //region Save State

    override fun onSaveInstanceState(): Parcelable = SavedState(super.onSaveInstanceState()).apply {
        state = this@ConstraintExpandableLayout.state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(state.superState)

        // Expanding ETV when it is not yet shown may yield NPE
        // Therefore we should delay its call
        post {
            updateState(restoredState = ss.state)
        }
    }

    private class SavedState : BaseSavedState {
        var state: State = State.Collapsed

        constructor(superState: Parcelable?) : super(superState)
        constructor(source: Parcel) : super(source) {
            state = State.values()[source.readInt()]
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(state.ordinal)
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
        private const val DEFAULT_COLLAPSED_HEIGHT_PX = 40
        private const val DEFAULT_SHADOW_HEIGHT_PX = 20
        private const val DEFAULT_COLLAPSED_DURATION = 200
        private const val DEFAULT_SHOW_SHADOW_VALUE = false
        private const val DEFAULT_STATE = 0
    }
}

enum class State {
    Collapsed,
    Expanded,
    Static,
    Collapsing,
    Expanding
}