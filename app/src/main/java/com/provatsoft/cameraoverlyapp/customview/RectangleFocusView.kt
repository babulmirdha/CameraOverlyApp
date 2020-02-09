package com.provatsoft.cameraoverlyapp.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class RectangleFocusView : View {
    private var mTransparentPaint: Paint? = null
    private var mSemiBlackPaint: Paint? = null
    private val mPath = Path()

    constructor(context: Context?) : super(context) {
        initPaints()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        initPaints()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initPaints()
    }

    private fun initPaints() {
        mTransparentPaint = Paint()
        mTransparentPaint!!.color = Color.TRANSPARENT
        mTransparentPaint!!.strokeWidth = 10f
        mSemiBlackPaint = Paint()
        mSemiBlackPaint!!.color = Color.TRANSPARENT
        mSemiBlackPaint!!.strokeWidth = 10f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPath.reset()
        val top = height / 4
        val width = width
        mPath.addRect(
            0f,
            top.toFloat(),
            width.toFloat(),
            top * 3.toFloat(),
            Path.Direction.CW
        )
        mPath.fillType = Path.FillType.INVERSE_EVEN_ODD
        canvas.drawRect(0f, top.toFloat(), width.toFloat(), top * 3.toFloat(), mTransparentPaint)
        val redPaint = Paint()
        redPaint.color = Color.RED
        redPaint.strokeWidth = 5f
        redPaint.style = Paint.Style.STROKE
        canvas.drawRect(0f, top.toFloat(), width.toFloat(), top * 3.toFloat(), redPaint)
        canvas.drawPath(mPath, mSemiBlackPaint)
        canvas.clipPath(mPath)
        canvas.drawColor(Color.parseColor("#A6000000"))
    }
}