package com.tboxcommons

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.facebook.react.bridge.*
import com.facebook.react.views.text.ReactFontManager
import kotlin.math.roundToInt


class TboxCommonsModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  override fun getName(): String {
    return "TboxCommons"
  }

  @ReactMethod
  fun measure(options: ReadableArray, promise: Promise) {
    val textBreakStrategy = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) Layout.BREAK_STRATEGY_SIMPLE else Layout.BREAK_STRATEGY_HIGH_QUALITY
    val results: WritableArray = Arguments.createArray()
    for (i in 0 until options.size()) {
      val option = options.getMap(i)!!
      val width = if (option.hasKey("width")) option.getDouble("width").roundToInt() else 0
      val height = if (option.hasKey("height")) option.getDouble("height").roundToInt() else 0
      val isMeasureWidth = width == 0
      val text = if (option.hasKey("text")) option.getString("text") ?: "" else ""
      val fontSize = if (option.hasKey("fontSize")) option.getDouble("fontSize") else 0.0
      val fontFamily = if (option.hasKey("fontFamily")) option.getString("fontFamily") ?: "" else ""
      val fontWeight = if (option.hasKey("fontWeight")) option.getString("fontWeight") ?: "" else ""
      val paint: TextPaint = createTextPaint(fontSize, fontFamily, fontWeight)
      val spacingMultiplier = 1f
      val spacingAddition = 0f
      val includePadding = false
      var layout: Layout
      if (isMeasureWidth) {
        results.pushDouble(paint.measureText(text).toDouble())
      } else {
        layout = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
          StaticLayout(
            text,
            paint,
            width,
            Layout.Alignment.ALIGN_NORMAL,
            spacingMultiplier,
            spacingAddition,
            includePadding
          )
        } else {
          StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(includePadding)
            .setBreakStrategy(textBreakStrategy)
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NORMAL)
            .build()
        }
        results.pushDouble(layout.height.toDouble())
      }
    }
    promise.resolve(results)
  }

  private fun createTextPaint(fontSize: Double, fontFamily: String?, fontWeight: String?): TextPaint {
    val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
    paint.textSize = (fontSize * reactApplicationContext.resources.configuration.fontScale).toFloat()
    val assetManager = reactApplicationContext.assets
    val typeface = ReactFontManager.getInstance().getTypeface(fontFamily, getFontWeight(fontWeight), assetManager)
    paint.typeface = typeface
    return paint
  }

  private fun getFontWeight(fontWeight: String?): Int {
    return when (fontWeight) {
      "bold", "500", "600", "700", "800", "900" -> Typeface.BOLD
      "normal", "100", "200", "300", "400" -> Typeface.NORMAL
      else -> Typeface.NORMAL
    }
  }
}
