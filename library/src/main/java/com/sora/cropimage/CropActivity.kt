package com.sora.cropimage;

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sora.cropimage.utils.MathUtils.getMaxGY
import com.sora.cropimage.widget.cropiwa.AspectRatio
import com.sora.cropimage.widget.cropiwa.CropIwaView
import com.sora.cropimage.widget.cropiwa.config.CropIwaSaveConfig
import com.sora.cropimage.widget.cropiwa.shape.CropIwaRectShape
import java.io.File

class CropActivity : AppCompatActivity(), CropNavigator {
    val RESELECT = 10


    lateinit var url: String

    private lateinit var cropUrl: Uri

    private var screenWidth: Int = 0

    private var screenHeight: Int = 0
    lateinit var file: File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)
        setSupportActionBar(findViewById(R.id.toolbar))
        val cropView: CropIwaView = findViewById(R.id.crop_view)
        supportActionBar?.setHomeButtonEnabled(true)
        url =
            Environment.getExternalStorageDirectory().path + "/temp/" + System.currentTimeMillis() + ".png"
        cropUrl = intent.getParcelableExtra("cropUrl")!!
        screenHeight = intent.getIntExtra("screenHeight", 1080)
        screenWidth = intent.getIntExtra("screenWidth", 1920 / 4)
        //获取最大公约数
        val gy = getMaxGY(screenWidth, screenHeight)
        //用最大公约数求出剪切控件应该显示的比列
        val width = screenWidth / gy
        val height = screenHeight / gy
        file = File(url)
        if (!file.exists())
            file.parentFile?.mkdirs()
        cropView.setImageUri(cropUrl)
        cropView.configureOverlay()
            .setCropShape(CropIwaRectShape(cropView.configureOverlay()))
            .setAspectRatio(AspectRatio(width, height))
            .setDynamicCrop(false)
            .apply()
        cropView.configureImage()
            .setImageScaleEnabled(true)
            .setImageTranslationEnabled(true)
            .apply()
        cropView.setCropSaveCompleteListener { bitmapUri ->
            val intent = Intent().putExtra("url", bitmapUri.path)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        cropView.setErrorListener {
            Toast.makeText(this@CropActivity, it.message, Toast.LENGTH_SHORT).show()

        }
    }

    override fun confirm() {
        val cropView: CropIwaView = findViewById(R.id.crop_view)
        cropView.crop(
            CropIwaSaveConfig.Builder(Uri.fromFile(file))
                .setCompressFormat(Bitmap.CompressFormat.PNG)
                .setSize(screenWidth, screenHeight)
                .setQuality(100)
                .build()
        )
        Log.d("CropView", "screenWidth=$screenWidth,screenHeight=$screenHeight")
    }

    override fun reselect() {
        setResult(RESELECT)
        finish()
    }

    private fun zoomBitmap(bitmap: Bitmap, width: Float, height: Float): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val sx = width / w.toFloat()
        val sy = height / h.toFloat()
        val matrix = Matrix()
        //也可以按两者之间最大的比例来设置放大比例，这样不会是图片压缩
//        float bigerS = Math.max(sx,sy);
//        matrix.postScale(bigerS,bigerS);
        matrix.postScale(sx, sy); // 长和宽放大缩小的比例
        val resizeBmp = Bitmap.createBitmap(
            bitmap, 0, 0, w,
            h, matrix, true
        )
        bitmap.recycle()
        return resizeBmp
    }

}