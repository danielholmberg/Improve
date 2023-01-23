package dev.danielholmberg.improve.clean.core.util

import android.graphics.*
import com.squareup.picasso.Transformation
import kotlin.math.min


class RoundedCornersTransform(
    private val radius: Int
) : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        val size = min(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
        if (squaredBitmap != source) {
            source.recycle()
        }
        val bitmap = Bitmap.createBitmap(size, size, source.config)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val shader =
            BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        paint.isAntiAlias = true

        canvas.drawRoundRect(
            RectF(0.0f, 0.0f, source.width.toFloat(), source.height.toFloat()),
            radius.toFloat(),
            radius.toFloat(),
            paint
        )
        squaredBitmap.recycle()
        return bitmap
    }

    override fun key(): String {
        return "rounded_corners"
    }
}