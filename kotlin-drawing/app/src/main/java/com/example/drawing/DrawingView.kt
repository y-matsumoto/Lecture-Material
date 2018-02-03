package com.example.drawing

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import java.util.*

class DrawingView constructor(context: Context, scale: Float = 1.0f) : View(context) {

    private val paint: Paint
    private var lastX: Int = 0
    private var lastY: Int = 0
    private var buffer: Canvas? = null
    private var bitmap: Bitmap? = null
    private val bitmapPaint: Paint
    private var currentColor = -0x10000
    private val path: Path
    private val outstandingSegments: MutableSet<String>
    private var currentSegment: Segment? = null
    private var scale = 1.0f
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    constructor(context: Context, width: Int, height: Int) : this(context) {
        this.setBackgroundColor(Color.DKGRAY)
        canvasWidth = width
        canvasHeight = height
    }

    init {

        outstandingSegments = HashSet()
        path = Path()
        this.scale = scale

        paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = -0x10000
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f

        bitmapPaint = Paint(Paint.DITHER_FLAG)
    }

    fun setColor(color: Int) {
        currentColor = color
        paint.color = color
    }

    fun clear() {
        bitmap = Bitmap.createBitmap(bitmap?.width ?: 0, bitmap?.height
                ?: 0, Bitmap.Config.ARGB_8888)
        bitmap?.let { buffer = Canvas(bitmap) }
        currentSegment = null
        outstandingSegments.clear()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        bitmap = Bitmap.createBitmap(Math.round(canvasWidth * scale), Math.round(canvasHeight * scale), Bitmap.Config.ARGB_8888)
        bitmap?.let { buffer = Canvas(bitmap) }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.DKGRAY)
        canvas.drawRect(0f, 0f, bitmap?.width?.toFloat() ?: 0f, bitmap?.height?.toFloat()
                ?: 0f, paintFromColor(Color.WHITE, Paint.Style.FILL_AND_STROKE))

        canvas.drawBitmap(bitmap as Bitmap, 0f, 0f, bitmapPaint)
        canvas.drawPath(path, paint)
    }

    private fun drawSegment(segment: Segment, paint: Paint) {
        if (buffer != null) {
            buffer!!.drawPath(getPathForPoints(segment.points, scale.toDouble()), paint)
        }
    }

    private fun onTouchStart(x: Float, y: Float) {
        path.reset()
        path.moveTo(x, y)
        currentSegment = Segment(currentColor)
        lastX = x.toInt() / PIXEL_SIZE
        lastY = y.toInt() / PIXEL_SIZE
        currentSegment!!.addPoint(lastX, lastY)
    }

    private fun onTouchMove(x: Float, y: Float) {

        val x1 = x.toInt() / PIXEL_SIZE
        val y1 = y.toInt() / PIXEL_SIZE

        val dx = Math.abs(x1 - lastX).toFloat()
        val dy = Math.abs(y1 - lastY).toFloat()
        if (dx >= 1 || dy >= 1) {
            path.quadTo((lastX * PIXEL_SIZE).toFloat(), (lastY * PIXEL_SIZE).toFloat(), ((x1 + lastX) * PIXEL_SIZE / 2).toFloat(), ((y1 + lastY) * PIXEL_SIZE / 2).toFloat())
            lastX = x1
            lastY = y1
            currentSegment?.addPoint(lastX, lastY)
        }
    }

    private fun onTouchEnd() {
        path.lineTo((lastX * PIXEL_SIZE).toFloat(), (lastY * PIXEL_SIZE).toFloat())
        buffer?.drawPath(path, paint)
        path.reset()

        val segment = Segment(currentSegment?.color ?: 0)
        currentSegment?.let {
            for (point in it.points) {
                segment.addPoint(Math.round(point.x / scale), Math.round(point.y / scale))
            }
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                onTouchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                onTouchEnd()
                invalidate()
            }
        }
        return true
    }

    companion object {
        val PIXEL_SIZE = 8

        @JvmOverloads
        fun paintFromColor(color: Int, style: Paint.Style = Paint.Style.STROKE): Paint {
            val p = Paint()
            p.isAntiAlias = true
            p.isDither = true
            p.color = color
            p.style = style
            return p
        }

        fun getPathForPoints(points: List<Point>, scale: Double): Path {
            var scale = scale
            val path = Path()
            scale = scale * PIXEL_SIZE
            var current = points[0]
            path.moveTo(Math.round(scale * current.x).toFloat(), Math.round(scale * current.y).toFloat())
            var next: Point? = null
            for (i in 1 until points.size) {
                next = points[i]
                path.quadTo(
                        Math.round(scale * current.x).toFloat(), Math.round(scale * current.y).toFloat(),
                        Math.round(scale * (next.x + current.x) / 2).toFloat(), Math.round(scale * (next.y + current.y) / 2).toFloat()
                )
                current = next
            }
            if (next != null) {
                path.lineTo(Math.round(scale * next.x).toFloat(), Math.round(scale * next.y).toFloat())
            }
            return path
        }
    }

}
