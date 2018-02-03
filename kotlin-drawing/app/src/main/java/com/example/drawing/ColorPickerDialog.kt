package com.example.drawing

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View

class ColorPickerDialog(context: Context, private val listener: OnColorChangedListener, private val initialColor: Int) : Dialog(context) {
    private val CENTER_X = 100
    private val CENTER_Y = 100
    private val CENTER_RADIUS = 32
    private val PI = 3.1415926f

    interface OnColorChangedListener {
        fun colorChanged(newColor: Int)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val l = object : OnColorChangedListener {
            override fun colorChanged(newColor: Int) {
                listener.colorChanged(newColor)
                dismiss()
            }
        }

        setContentView(ColorPickerView(context, l, initialColor))
        setTitle("Pick a Color")
    }

    inner class ColorPickerView internal constructor(c: Context, private val listener: OnColorChangedListener, color: Int) : View(c) {

        private val paint: Paint
        private val centerPaint: Paint
        private val colors: IntArray = intArrayOf(-0x10000, -0xff01, -0xffff01, -0xff0001, -0xff0100, -0x100, -0x10000)

        private var trackingCenter: Boolean = false
        private var highlightCenter: Boolean = false

        init {
            val s = SweepGradient(0f, 0f, colors, null)

            paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.shader = s
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 32f

            centerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            centerPaint.color = color
            centerPaint.strokeWidth = 5f
        }

        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            val r = CENTER_X - paint.strokeWidth * 0.5f

            canvas.translate(CENTER_X.toFloat(), CENTER_X.toFloat())

            canvas.drawOval(RectF(-r, -r, r, r), paint)
            canvas.drawCircle(0f, 0f, CENTER_RADIUS.toFloat(), centerPaint)

            if (trackingCenter) {
                val c = centerPaint.color
                centerPaint.style = Paint.Style.STROKE

                if (highlightCenter) {
                    centerPaint.alpha = 0xFF
                } else {
                    centerPaint.alpha = 0x80
                }
                canvas.drawCircle(0f, 0f,
                        CENTER_RADIUS + centerPaint.strokeWidth,
                        centerPaint)

                centerPaint.style = Paint.Style.FILL
                centerPaint.color = c
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(CENTER_X * 2, CENTER_Y * 2)
        }

        private fun floatToByte(x: Float): Int {
            return Math.round(x)
        }

        private fun pinToByte(n: Int): Int {
            var n = n
            if (n < 0) {
                n = 0
            } else if (n > 255) {
                n = 255
            }
            return n
        }

        private fun ave(s: Int, d: Int, p: Float): Int {
            return s + java.lang.Math.round(p * (d - s))
        }

        private fun interpColor(colors: IntArray, unit: Float): Int {
            if (unit <= 0) {
                return colors[0]
            }
            if (unit >= 1) {
                return colors[colors.size - 1]
            }

            var p = unit * (colors.size - 1)
            val i = p.toInt()
            p -= i.toFloat()

            val c0 = colors[i]
            val c1 = colors[i + 1]
            val a = ave(Color.alpha(c0), Color.alpha(c1), p)
            val r = ave(Color.red(c0), Color.red(c1), p)
            val g = ave(Color.green(c0), Color.green(c1), p)
            val b = ave(Color.blue(c0), Color.blue(c1), p)

            return Color.argb(a, r, g, b)
        }

        private fun rotateColor(color: Int, rad: Float): Int {
            val deg = rad * 180 / 3.1415927f
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)

            val cm = ColorMatrix()
            val tmp = ColorMatrix()

            cm.setRGB2YUV()
            tmp.setRotate(0, deg)
            cm.postConcat(tmp)
            tmp.setYUV2RGB()
            cm.postConcat(tmp)

            val a = cm.array

            val ir = floatToByte(a[0] * r + a[1] * g + a[2] * b)
            val ig = floatToByte(a[5] * r + a[6] * g + a[7] * b)
            val ib = floatToByte(a[10] * r + a[11] * g + a[12] * b)

            return Color.argb(Color.alpha(color), pinToByte(ir),
                    pinToByte(ig), pinToByte(ib))
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x - CENTER_X
            val y = event.y - CENTER_Y
            val inCenter = java.lang.Math.sqrt((x * x + y * y).toDouble()) <= CENTER_RADIUS

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    trackingCenter = inCenter
                    if (inCenter) {
                        highlightCenter = true
                        invalidate()
                        return true
                    }
                    if (trackingCenter) {
                        if (highlightCenter != inCenter) {
                            highlightCenter = inCenter
                            invalidate()
                        }
                    } else {
                        val angle = java.lang.Math.atan2(y.toDouble(), x.toDouble()).toFloat()
                        var unit = angle / (2 * PI)
                        if (unit < 0) {
                            unit += 1f
                        }
                        centerPaint.color = interpColor(colors, unit)
                        invalidate()
                    }
                }
                MotionEvent.ACTION_MOVE -> if (trackingCenter) {
                    if (highlightCenter != inCenter) {
                        highlightCenter = inCenter
                        invalidate()
                    }
                } else {
                    val angle = java.lang.Math.atan2(y.toDouble(), x.toDouble()).toFloat()
                    var unit = angle / (2 * PI)
                    if (unit < 0) {
                        unit += 1f
                    }
                    centerPaint.color = interpColor(colors, unit)
                    invalidate()
                }
                MotionEvent.ACTION_UP -> if (trackingCenter) {
                    if (inCenter) {
                        listener.colorChanged(centerPaint.color)
                    }
                    trackingCenter = false
                    invalidate()
                }
            }
            return true
        }
    }
}
