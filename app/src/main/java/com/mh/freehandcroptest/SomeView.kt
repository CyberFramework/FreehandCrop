package com.mh.freehandcroptest

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.text.Layout.Alignment
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.roundToInt

class SomeView : AppCompatImageView, OnTouchListener {
    private var paintLine: Paint
    private var paintFill: Paint
    private var paintCircle: Paint
    private var paintText: Paint
    internal var points: MutableList<Point>
    private var verts: MutableList<Float>
    var DIST = 2
    var flgPathDraw = true
    private var mfirstpoint: Point = Point()
    var bfirstpoint = false
    var mlastpoint: Point? = null
    var bitmap: Bitmap = Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)
    var mContext: Context

    var displayWidth = 0
    var displayHeight = 0
    var src = Rect()
    var dest = Rect()

    constructor(c: Context, bitmap: Bitmap) : super(c) {
        mContext = c
        this.bitmap = bitmap
        isFocusable = true
        isFocusableInTouchMode = true
        paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)
            strokeWidth = 5f
            color = Color.RED
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        paintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#44FF0000")
        }

        paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
            color = Color.WHITE
        }

        paintText = Paint().apply {
            textAlign = Paint.Align.CENTER
            textSize = 40f
            color = Color.BLACK
        }

        setOnTouchListener(this)
        points = ArrayList()
        verts = ArrayList()
        bfirstpoint = false
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        isFocusable = true
        isFocusableInTouchMode = true

        paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)
            strokeWidth = 5f
            color = Color.RED
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        paintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#44FF0000")
        }

        paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
            color = Color.WHITE
        }

        paintText = Paint().apply {
            textAlign = Paint.Align.CENTER
            textAlignment = TEXT_ALIGNMENT_CENTER
            textSize = 40f
            color = Color.BLACK
        }

        setOnTouchListener(this)
        points = ArrayList()
        verts = ArrayList()
        bfirstpoint = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        println("onMeasure Called")
        val width: Int
        val height: Int

        val measureWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)

        val mDefaultWidth = bitmap.width
        val mDefaultHeight = bitmap.height

        width = when (measureWidthMode) {
            MeasureSpec.AT_MOST -> { // wrap_content
                kotlin.math.min(mDefaultWidth, measureWidth)
            }
            MeasureSpec.EXACTLY -> { // match_parent
                measureWidth
            }
            else -> {
                mDefaultWidth
            }
        }

        height = when (measureHeightMode) {
            MeasureSpec.AT_MOST -> {
                kotlin.math.min(mDefaultHeight, measureHeight)
            }
            MeasureSpec.EXACTLY -> {
                measureHeight
            }
            else -> {
                mDefaultHeight
            }
        }

        setMeasuredDimension(mDefaultWidth, mDefaultHeight)
    }

    public override fun onDraw(canvas: Canvas) {
        println("onDraw called")
        /*Rect dest = new Rect(0, 0, getWidth(), getHeight());

     paint.setFilterBitmap(true); canvas.drawBitmap(bitmap, null, dest, paint);*/

        canvas.drawBitmap(bitmap, 0f, 0f, null)
/*
        val path = Path()
        var first = true
        var i = 0
        while (i < points.size) {
            val point = points[i]
            if (first) {
                first = false
                path.moveTo(point.x.toFloat(), point.y.toFloat())
            } else if (i < points.size - 1) {
                val next = points[i + 1]
                path.quadTo(point.x.toFloat(), point.y.toFloat(), next.x.toFloat(), next.y.toFloat())
            } else {
                mlastpoint = points[i]
                path.lineTo(point.x.toFloat(), point.y.toFloat())
            }
            i += 2
        }
        canvas.drawPath(path, paint)
*/


        canvas.drawVertices(
            Canvas.VertexMode.TRIANGLE_FAN,
            verts.size, verts.toFloatArray(), 0,
            null, 0, null, 0,
            null, 0, 0,
            paintFill
        )

        val outputArray = mutableListOf<Float>()
        for (i in verts.indices step 2) {
            if (i == 0) {
                outputArray.add(verts[0])
                outputArray.add(verts[1])
            } else {
                outputArray.add(verts[i])
                outputArray.add(verts[i+1])
                outputArray.add(verts[i])
                outputArray.add(verts[i+1])
                if (i == verts.size - 2) {
                    outputArray.add(verts[0])
                    outputArray.add(verts[1])
                }
            }
        }

        canvas.drawLines(outputArray.toFloatArray(), paintLine)

        for (i in verts.indices step 2) {
            canvas.drawCircle(verts[i], verts[i+1], 20f, paintCircle)
            canvas.drawText((i/2+1).toString(), verts[i], verts[i+1] + 15, paintText)
        }

        println(verts)
        println(outputArray)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        // if(event.getAction() != MotionEvent.ACTION_DOWN)
        // return super.onTouchEvent(event);
        val point = Point()
        point.x = event.x.roundToInt()
        point.y = event.y.roundToInt()
        /*
        if (flgPathDraw) {
            if (bfirstpoint) {
                if (comparepoint(mfirstpoint, point)) {
                    // points.add(point);
                    points.add(mfirstpoint)
                    flgPathDraw = false
                    showcropdialog()
                } else {
                    points.add(point)
                }
            } else {
                points.add(point)
            }
            if (!bfirstpoint) {
                mfirstpoint = point
                bfirstpoint = true
            }
        }
        invalidate()
        Log.e("Hi  ==>", "Size: " + point.x + " " + point.y)
        if (event.action == MotionEvent.ACTION_UP) {
            mlastpoint = point
            if (flgPathDraw) {
                if (points.size > 12) {
                    if (!comparepoint(mfirstpoint, mlastpoint)) {
                        flgPathDraw = false
                        points.add(mfirstpoint)
                        showcropdialog()
                    }
                }
            }
        }
        */

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN -> return true
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP -> {
                verts.add(event.getX(event.actionIndex))
                verts.add(event.getY(event.actionIndex))
                points.add(point)
                invalidate()
                return true
            }
        }
        return true
    }

    private fun comparepoint(first: Point?, current: Point?): Boolean {
        return if ((current!!.x - 3) < first!!.x && first.x < (current!!.x + 3)
            && (current!!.y - 3) < first.y && first.y < (current!!.y + 3)
        ) {
            points.size >= 10
        } else {
            false
        }
    }

    fun fillinPartofPath() {
        val point = Point()
        point.x = points[0]!!.x
        point.y = points[0]!!.y
        points.add(point)
        invalidate()
    }

    fun resetView() {
        points.clear()
        paintLine.color = Color.WHITE
        paintLine.style = Paint.Style.STROKE
        paintLine = Paint(Paint.ANTI_ALIAS_FLAG)
        paintLine.style = Paint.Style.STROKE
        paintLine.strokeWidth = 5f
        paintLine.color = Color.RED
        points = ArrayList()
        bfirstpoint = false
        flgPathDraw = true
        invalidate()
    }

    private fun showcropdialog() {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            var intent: Intent
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> (mContext as MainActivity).cropImage()
                DialogInterface.BUTTON_NEGATIVE ->                             /*// No button clicked

     intent = new Intent(mContext, DisplayCropActivity.class); intent.putExtra("crop", false); mContext.startActivity(intent);
     bfirstpoint = false;*/resetView()
            }
        }
        val builder = AlertDialog.Builder(mContext)
        builder.setMessage("Do you Want to save Crop or Non-crop image?")
            .setPositiveButton("Crop", dialogClickListener)
            .setNegativeButton("Non-crop", dialogClickListener).show()
            .setCancelable(false)
    }

    fun getPoints(): List<Point?> {
        return points
    }
}