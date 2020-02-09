package com.provatsoft.cameraoverlyapp.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class CircleFocusView : View {
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
        val centerX = width / 2
        val centerY = height / 2
        mPath.addCircle(
            centerX.toFloat(),
            centerY.toFloat(),
            centerX.toFloat(),
            Path.Direction.CW
        )
        mPath.fillType = Path.FillType.INVERSE_EVEN_ODD
        canvas.drawCircle(
            centerX.toFloat(),
            centerY.toFloat(),
            centerX.toFloat(),
            mTransparentPaint
        )
        val redPaint = Paint()
        redPaint.color = Color.RED
        redPaint.strokeWidth = 5f
        redPaint.style = Paint.Style.STROKE
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), centerX.toFloat(), redPaint)
        canvas.drawPath(mPath, mSemiBlackPaint)
        canvas.clipPath(mPath)
        canvas.drawColor(Color.parseColor("#A6000000"))
    }
}