package com.example.luxeaod

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

class AodActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clock: TextView
    private lateinit var day: TextView
    private lateinit var content: LinearLayout
    private var seconds = 0
    private val tick = object : Runnable {
        override fun run() {
            val prefs = getSharedPreferences("luxe", MODE_PRIVATE)
            val style = prefs.getInt("clockStyle", 0)
            val pattern = if (style == 2) "HH·mm" else if (prefs.getBoolean("use12Hour", false)) "hh:mm" else "HH:mm"
            clock.text = SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
            day.text = SimpleDateFormat("EEEE  •  dd MMMM", Locale.getDefault()).format(Date()).uppercase(Locale.getDefault())
            // Subtle position shift every 30 seconds to reduce static-pixel exposure.
            seconds += 15
            val dx = (sin(seconds / 30.0) * 12).toFloat()
            val dy = (sin(seconds / 41.0) * 16).toFloat()
            content.translationX = dx
            content.translationY = dy
            handler.postDelayed(this, 15_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        window.statusBarColor = Color.BLACK; window.navigationBarColor = Color.BLACK
        window.attributes = window.attributes.apply { screenBrightness = 0.06f }
        val prefs = getSharedPreferences("luxe", MODE_PRIVATE)
        val accent = prefs.getInt("accent", Color.rgb(201,164,92))
        val frame = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val img = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP; alpha = .40f; setBackgroundColor(Color.BLACK) }
        val uriString = prefs.getString("photo", null)
        if (uriString != null) try { img.setImageURI(Uri.parse(uriString)) } catch (_: Exception) {}
        img.scaleX = prefs.getFloat("zoom", 1f); img.scaleY = prefs.getFloat("zoom", 1f)
        frame.addView(img, FrameLayout.LayoutParams(-1,-1))
        frame.addView(View(this).apply { setBackgroundColor(0xAA000000.toInt()) }, FrameLayout.LayoutParams(-1,-1))
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(18,18,18,18) }
        val style = prefs.getInt("clockStyle", 0)
        clock = TextView(this).apply {
            textSize = prefs.getFloat("clockSize", 60f); setTextColor(accent); gravity = Gravity.CENTER
            typeface = when (style) { 1 -> Typeface.create("sans-serif-medium", Typeface.NORMAL); 2 -> Typeface.create("serif", Typeface.NORMAL); else -> Typeface.create("sans-serif-light", Typeface.NORMAL) }
            letterSpacing = if (style == 1) .02f else -.04f
            setShadowLayer(10f,0f,0f,accent)
        }
        day = TextView(this).apply { textSize = 13f; setTextColor(Color.WHITE); gravity = Gravity.CENTER; letterSpacing = .10f; setPadding(0,10,0,22) }
        val battery = TextView(this).apply { text = "◇  ${getBatteryPercent()}%"; textSize = 13f; setTextColor(accent); gravity = Gravity.CENTER }
        content.addView(clock); content.addView(day); content.addView(battery)
        frame.addView(content, FrameLayout.LayoutParams(-1,-1))
        val hint = TextView(this).apply { text = "Tap anywhere to exit"; textSize = 10f; setTextColor(0x88FFFFFF.toInt()); gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; setPadding(0,0,0,20) }
        frame.addView(hint, FrameLayout.LayoutParams(-1,-1))
        setContentView(frame)
        tick.run()
    }
    private fun getBatteryPercent(): Int {
        val i = registerReceiver(null, IntentFilterCompat.batteryFilter())
        val level = i?.getIntExtra("level", -1) ?: -1
        val scale = i?.getIntExtra("scale", -1) ?: -1
        return if (level >= 0 && scale > 0) level * 100 / scale else 0
    }
    override fun onTouchEvent(event: MotionEvent): Boolean { if (event.action == MotionEvent.ACTION_UP) finish(); return true }
    override fun onDestroy() { handler.removeCallbacks(tick); super.onDestroy() }
}

private object IntentFilterCompat {
    fun batteryFilter() = android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
}
