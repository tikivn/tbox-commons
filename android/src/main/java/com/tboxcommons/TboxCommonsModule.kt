package com.tboxcommons

import android.R
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.inputmethod.InputMethodManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.facebook.react.bridge.*
import com.facebook.react.views.text.ReactFontManager
import kotlin.math.roundToInt

class TboxCommonsModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext),
  LifecycleEventListener {
  private var addToHomePromise: Promise? = null
  private val addToHomeReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      Log.d("TboxCommonsModule", "onAddShortcutSuccess")
      addToHomePromise?.resolve(true)
    }
  }
  private val addToHomeReceiverFilter = IntentFilter(ACTION_SHORTCUT_ADDED)

  init {
    reactContext.addLifecycleEventListener(this)
    reactApplicationContext.registerReceiver(addToHomeReceiver, addToHomeReceiverFilter)
  }

  override fun onHostResume() {
  }

  override fun onHostPause() {
  }

  override fun onHostDestroy() {
    reactApplicationContext.unregisterReceiver(addToHomeReceiver)
    addToHomePromise = null
  }

  override fun getName(): String {
    return "TboxCommons"
  }

  @ReactMethod
  fun measure(options: ReadableArray, promise: Promise) {
    val textBreakStrategy =
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) LineBreaker.BREAK_STRATEGY_SIMPLE else LineBreaker.BREAK_STRATEGY_HIGH_QUALITY
    val results: WritableArray = Arguments.createArray()
    for (i in 0 until options.size()) {
      val option = options.getMap(i)!!
      val width = if (option.hasKey("width")) option.getDouble("width").roundToInt() else 0
      val height = if (option.hasKey("height")) option.getDouble("height").roundToInt() else 0
      val isMeasureWidth = width == 0
      val text = if (option.hasKey("text")) option.getString("text") ?: "" else ""
      val fontSize = if (option.hasKey("fontSize")) option.getDouble("fontSize") else 0.0
      val lineHeight = if (option.hasKey("lineHeight")) option.getDouble("lineHeight") else 0.0
      val fontFamily = if (option.hasKey("fontFamily")) option.getString("fontFamily") ?: "" else ""
      val fontWeight = if (option.hasKey("fontWeight")) option.getString("fontWeight") ?: "" else ""
      val paint: TextPaint = createTextPaint(fontSize, fontFamily, fontWeight)
      val spacingMultiplier = 1f

      val spacingAddition = 0f
      val includePadding = true
      var layout: Layout
      if (isMeasureWidth) {
        results.pushDouble(paint.measureText(text).toDouble())
      } else {
        layout = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
          StaticLayout(
            text,
            paint,
            width,
            Layout.Alignment.ALIGN_CENTER,
            spacingMultiplier,
            spacingAddition.toFloat(),
            includePadding
          )
        } else {
          var builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(spacingAddition.toFloat(), spacingMultiplier)
            .setIncludePad(includePadding)
            .setBreakStrategy(textBreakStrategy)
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_FULL)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setUseLineSpacingFromFallbacks(true)
          }
          builder.build()
        }
        results.pushDouble(layout.height.toDouble())
      }
    }
    promise.resolve(results)
  }

  @ReactMethod
  fun addToHome(option: ReadableMap, promise: Promise) {
    this.addToHomePromise = promise
    val i = Intent()
    i.action = Intent.ACTION_VIEW

    if (option.hasKey("url")) {
      val url = option.getString("url") ?: ""
      i.data = Uri.parse(url)
      val iconUrl = option.getString("icon") ?: ""
      val appName = if (option.hasKey("appName")) option.getString("appName") ?: "" else ""
      val appId = if (option.hasKey("appId")) option.getString("appId") ?: "" else ""
      val downloader = ImageDownloader(ImageDownloaderCallback {
        val size = reactApplicationContext.resources.getDimension(R.dimen.app_icon_size).toInt()
        val resizeIcon = Bitmap.createScaledBitmap(it, size, size, false)
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(reactApplicationContext)) {
          val shortcutInfo = ShortcutInfoCompat.Builder(reactApplicationContext, appId)
            .setIntent(i)
            .setIcon(IconCompat.createWithBitmap(resizeIcon))
            .setShortLabel(appName)
            .build()

          val targetIntent =
            ShortcutManagerCompat.createShortcutResultIntent(reactApplicationContext, shortcutInfo)
              .apply {
                action = ACTION_SHORTCUT_ADDED
                setPackage(reactApplicationContext.packageName)
              }

          val pendingIntent =
            PendingIntent.getBroadcast(reactApplicationContext, 0, targetIntent, 0)

          ShortcutManagerCompat.requestPinShortcut(
            reactApplicationContext,
            shortcutInfo,
            pendingIntent.intentSender
          )
        } else {
          Toast.makeText(
            reactApplicationContext,
            "Creating Shortcuts is not Supported on this Launcher",
            Toast.LENGTH_SHORT
          ).show()
        }
      })
      downloader.execute(iconUrl)
    }
  }

  @ReactMethod
  fun dismiss(promise: Promise) {
    val imm: InputMethodManager = reactApplicationContext.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    reactApplicationContext.currentActivity?.currentFocus?.let {
      imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
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

  companion object {
    private const val ACTION_SHORTCUT_ADDED = "com.tboxcommons.TboxCommonsModule.SHORTCUT_ADDED"
  }
}
