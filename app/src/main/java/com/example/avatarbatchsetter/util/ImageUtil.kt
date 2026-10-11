package com.example.avatarbatchsetter.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object ImageUtil {

    fun decodeFile(path: String, maxDim: Int = 512): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxDim
            && bounds.outHeight / (sample * 2) >= maxDim
        ) {
            sample *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        val src = BitmapFactory.decodeFile(path, decodeOpts)
            ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        val w = src.width
        val h = src.height
        val scale = minOf(1f, maxDim.toFloat() / w, maxDim.toFloat() / h)
        if (scale >= 1f) return src

        val newW = (w * scale).toInt().coerceAtLeast(1)
        val newH = (h * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(src, newW, newH, true)
        if (scaled != src) src.recycle()
        return scaled
    }

    fun toJpegBytes(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos)
        return bos.toByteArray()
    }
}
