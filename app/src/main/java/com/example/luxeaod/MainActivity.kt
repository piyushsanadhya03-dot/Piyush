package com.example.luxeaod

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.graphics.Typeface
import android.widget.FrameLayout

class MainActivity : Activity() {
    private var photoUri: Uri? = null
    private var accent = Color.rgb(201, 164, 92)
    private var clockSize = 58f
    private var zoom = 1f
    private var clockStyle = 0
    private lateinit var preview: ImageView
    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var styleLabel: TextView
    private val pickPhotoRequest = 101
    private val prefs by lazy { getSharedPreferences("luxe", MODE_PRIVATE) }

    @Deprecated("Uses the platform picker for compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickPhotoRequest && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            photoUri = uri
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            try { preview.setImageURI(uri) } catch (_: Exception) {}
            prefs.edit().putString("photo", uri.toString()).apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accent = prefs.getInt("accent", Color.rgb(201,164,92))
        clockSize = prefs.getFloat("clockSize", 58f)
        zoom = prefs.getFloat("zoom", 1f)
        clockStyle = prefs.getInt("clockStyle", 0)
        window.statusBarColor = Color.rgb(8,8,8)
        window.navigationBarColor = Color.rgb(8,8,8)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 16, 20, 16); setBackgroundColor(Color.rgb(12,12,12)) }
        root.addView(TextView(this).apply { text = "LUXE AOD"; textSize = 25f; letterSpacing = .18f; setTextColor(accent); typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL) })
        root.addView(TextView(this).apply { text = "Your photo • luxury clock • AMOLED black"; textSize = 13f; setTextColor(Color.LTGRAY); setPadding(0,4,0,12) })

        val frame = FrameLayout(this).apply { background = android.graphics.drawable.GradientDrawable().apply { cornerRadius = 24f; setColor(Color.BLACK); setStroke(1, Color.rgb(75,65,45)) }; clipToOutline = true }
        preview = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP; alpha = .70f; setBackgroundColor(Color.rgb(18,18,18)); scaleX = zoom; scaleY = zoom }
        frame.addView(preview, FrameLayout.LayoutParams(-1, -1))
        frame.addView(View(this).apply { background = android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0x33000000, 0xCC000000.toInt())) }, FrameLayout.LayoutParams(-1,-1))
        val overlay = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(10,10,10,10) }
        clock = TextView(this).apply { text = "10:08"; textSize = clockSize; gravity = Gravity.CENTER; setTextColor(accent); typeface = Typeface.create("sans-serif-light", Typeface.NORMAL); letterSpacing = -.04f; setShadowLayer(18f,0f,0f,accent) }
        date = TextView(this).apply { text = "THURSDAY  •  29 OCTOBER"; textSize = 12f; gravity = Gravity.CENTER; setTextColor(Color.WHITE); letterSpacing = .12f; setPadding(0,8,0,18) }
        overlay.addView(clock); overlay.addView(date)
        overlay.addView(TextView(this).apply { text = "◇  87%"; textSize = 13f; gravity = Gravity.CENTER; setTextColor(accent) })
        frame.addView(overlay, FrameLayout.LayoutParams(-1,-1))
        root.addView(frame, LinearLayout.LayoutParams(-1, 0, 1f).apply { setMargins(0, 12, 0, 10) })

        root.addView(Button(this).apply { text = "Choose photo / wallpaper"; setOnClickListener { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION), pickPhotoRequest) } }, LinearLayout.LayoutParams(-1, 46))
        root.addView(label("Wallpaper zoom / crop"))
        val zoomSlider = SeekBar(this).apply { max = 100; progress = ((zoom - 1f) / 1.5f * 100).toInt().coerceIn(0,100); setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { zoom = 1f + progress / 100f * 1.5f; preview.scaleX = zoom; preview.scaleY = zoom; prefs.edit().putFloat("zoom", zoom).apply() }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}; override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }) }
        root.addView(zoomSlider)
        root.addView(label("Clock size"))
        root.addView(SeekBar(this).apply { max = 100; progress = ((clockSize - 36f) / .55f).toInt().coerceIn(0,100); setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { clockSize = 36f + progress * .55f; clock.textSize = clockSize; prefs.edit().putFloat("clockSize", clockSize).apply() }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}; override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }) })

        val colors = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        listOf(Color.rgb(201,164,92), Color.WHITE, Color.rgb(130,190,255), Color.rgb(230,145,190)).forEachIndexed { i, c ->
            colors.addView(Button(this).apply { text = listOf("Gold", "White", "Ice", "Rose")[i]; setTextColor(c); setOnClickListener { accent = c; clock.setTextColor(c); clock.setShadowLayer(18f,0f,0f,c); prefs.edit().putInt("accent", c).apply() } }, LinearLayout.LayoutParams(0,44,1f))
        }
        root.addView(colors)
        styleLabel = label("")
        val styleButton = Button(this).apply { text = "Change clock style"; setOnClickListener { clockStyle = (clockStyle + 1) % 3; updateStyle(); prefs.edit().putInt("clockStyle", clockStyle).apply() } }
        root.addView(styleButton); root.addView(styleLabel)
        root.addView(Button(this).apply { text = "Start full-screen AOD"; setOnClickListener {
            prefs.edit().putInt("accent", accent).putFloat("clockSize", clockSize).putFloat("zoom", zoom).putInt("clockStyle", clockStyle).apply()
            startActivity(Intent(this@MainActivity, AodActivity::class.java))
        } }, LinearLayout.LayoutParams(-1, 50).apply { setMargins(0, 8, 0, 0) })
        root.addView(TextView(this).apply { text = "Note: Android does not let ordinary apps replace the phone's hardware/system screen-off AOD. This mode keeps a dim full-screen clock visible while the AOD activity is open; battery use may be higher."; textSize = 11f; setTextColor(Color.GRAY); setPadding(2,8,2,0) })
        setContentView(root)
        val saved = prefs.getString("photo", null)
        if (saved != null) try { photoUri = Uri.parse(saved); preview.setImageURI(photoUri) } catch (_: Exception) {}
        updateStyle()
    }

    private fun label(text: String) = TextView(this).apply { this.text = text; setTextColor(Color.WHITE); setPadding(2,6,0,0); textSize = 13f }
    private fun updateStyle() {
        when (clockStyle) {
            0 -> { clock.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL); clock.letterSpacing = -.04f; clock.text = "10:08"; styleLabel.text = "Style: Elegant thin" }
            1 -> { clock.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL); clock.letterSpacing = .02f; clock.text = "10:08"; styleLabel.text = "Style: Modern bold" }
            else -> { clock.typeface = Typeface.create("serif", Typeface.NORMAL); clock.letterSpacing = -.02f; clock.text = "10·08"; styleLabel.text = "Style: Luxury serif" }
        }
        clock.setTextColor(accent); clock.setShadowLayer(18f,0f,0f,accent)
    }
}
