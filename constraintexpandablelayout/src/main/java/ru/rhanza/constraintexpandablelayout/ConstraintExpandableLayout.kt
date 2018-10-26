package ru.rhanza.constraintexpandablelayout

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

class ConstraintExpandableLayout : ConstraintLayout {
    private val contentView: LinearLayout

    constructor(context: Context) : super(context) {
        init(null)
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
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        contentView.addView(child, index, params)
    }

    private fun init(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ConstraintExpandableLayout)

        //use

        typedArray.recycle()

    }
}