package com.mh.freehandcroptest

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Half.toFloat
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.mh.freehandcroptest.databinding.ActivityImageFreehandCropBinding
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityImageFreehandCropBinding.inflate(layoutInflater) }
    private var mBitmap: Bitmap = Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)
    private var imageUri: Uri? = null

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                binding.tempImage.visibility = View.VISIBLE
                imageUri = result.data?.data //이미지 경로 원본
                binding.tempImage.setImageURI(imageUri) //이미지 뷰를 바꿈
                val rect = getImageBounds(binding.tempImage)
                mBitmap = binding.tempImage.drawable.toBitmap(rect.width().roundToInt(), rect.height().roundToInt())

                binding.cropImage.bitmap = mBitmap
                binding.cropImage.requestLayout()
                binding.tempImage.visibility = View.GONE

                println("actual width/height ${rect.width()} : ${rect.height()} ${rect.width().toFloat() / rect.height().toFloat()}")
                println("원본 이미지 비율 ${mBitmap.width} : ${mBitmap.height} ${mBitmap.width.toFloat() / mBitmap.height.toFloat()}")
                println("이미지뷰의 비율 ${binding.tempImage.measuredWidth} : ${binding.tempImage.measuredHeight} ${binding.tempImage.measuredWidth.toFloat() / binding.tempImage.measuredHeight.toFloat()}")
                println("크롭 이미지 비율 ${binding.cropImage.bitmap.width} : ${binding.cropImage.bitmap.height} ${binding.cropImage.bitmap.width.toFloat() / binding.cropImage.bitmap.height.toFloat()}")
                println("크롭 이미지뷰 비율 ${binding.cropImage.measuredWidth} : ${binding.cropImage.measuredHeight} ${binding.cropImage.measuredWidth.toFloat() / binding.cropImage.measuredHeight.toFloat()}")

//                val layout = binding.layout
//                layout.removeView(mSomeView)
//                mSomeView = SomeView(this, mBitmap)
//                mSomeView!!.updatePadding(50,50,50,50)
//                mSomeView!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
//
//                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                lp.gravity = Gravity.CENTER
//                layout.addView(mSomeView, lp)
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
//        mBitmap = binding.tempImage.drawable.toBitmap()
//        binding.tempImage.setImageBitmap(mBitmap)
//        binding.tempImage.visibility = View.GONE
//
//        mSomeView = SomeView(this, mBitmap)
//
//        val layout = binding.layout
//        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//        layout.addView(mSomeView, lp)

        binding.getImgBtn.setOnClickListener {
            val intentImage = Intent(Intent.ACTION_PICK)
            intentImage.type = MediaStore.Images.Media.CONTENT_TYPE
            getContent.launch(intentImage)
        }
        binding.cropImgBtn.setOnClickListener {
            cropImage()
        }
    }

    private fun getImageBounds(imageView: ImageView): RectF {
        val bounds = RectF()
        val drawable = imageView.drawable
        if (drawable != null) {
            imageView.imageMatrix.mapRect(bounds, RectF(drawable.bounds))
        }
        return bounds
    }

    fun cropImage() {
        setContentView(R.layout.activity_picture_preview)
        val imageView: ImageView = findViewById(R.id.image)
        val fullScreenBitmap = Bitmap.createBitmap(binding.cropImage.width, binding.cropImage.height, mBitmap.config)
        val canvas = Canvas(fullScreenBitmap)
        val path = Path()
        val points: List<Point> = binding.cropImage.points
        for (i in points.indices) {
            println(points[i].x.toString() + " x " + points[i].y.toString())
            path.lineTo(points[i].x.toFloat(), points[i].y.toFloat())
        }

        // 선택 자르기의 경우
        path.lineTo(points[0].x.toFloat(), points[0].y.toFloat())

        // Cut out the selected portion of the image...
        val paint = Paint()
        canvas.drawPath(path, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(mBitmap, 0f, 0f, paint)

        /*
        // Frame the cut out portion...
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0f
        canvas.drawPath(path, paint)
        */

        // Create a bitmap with just the cropped area.
        val region = Region()
        val clip = Region(0, 0, fullScreenBitmap.width, fullScreenBitmap.height)
        region.setPath(path, clip)
        val bounds: Rect = region.bounds
        val croppedBitmap = Bitmap.createBitmap(fullScreenBitmap, bounds.left, bounds.top, bounds.width(), bounds.height())
        imageView.setImageBitmap(croppedBitmap)
    }
}