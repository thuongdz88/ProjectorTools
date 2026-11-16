package com.example.projectortools

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val shortcuts = listOf(
        "Cập nhật hệ thống" to "android.settings.SYSTEM_UPDATE_SETTINGS",
        "Cài đặt chung" to Settings.ACTION_SETTINGS,
        "Wi-Fi" to Settings.ACTION_WIFI_SETTINGS,
        "Bluetooth" to Settings.ACTION_BLUETOOTH_SETTINGS,
        "Hiển thị" to Settings.ACTION_DISPLAY_SETTINGS,
        "Âm thanh" to Settings.ACTION_SOUND_SETTINGS,
        "Ngày giờ" to Settings.ACTION_DATE_SETTINGS,
        "Ngôn ngữ" to Settings.ACTION_LOCALE_SETTINGS,
        "Bàn phím" to Settings.ACTION_INPUT_METHOD_SETTINGS,
        "Quản lý ứng dụng" to Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS,
        "Bộ nhớ" to Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
        "Thông tin thiết bị" to Settings.ACTION_DEVICE_INFO_SETTINGS,
        "Tùy chọn nhà phát triển" to Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
        "Chiếu màn hình (Cast)" to Settings.ACTION_CAST_SETTINGS
    )

    private fun dp(v: Int) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(16), dp(24), dp(16))
        }

        val dm = resources.displayMetrics
        root.addView(TextView(this).apply {
            textSize = 16f
            text = "Máy: ${Build.MANUFACTURER} ${Build.MODEL}\n" +
                "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n" +
                "Bản build: ${Build.DISPLAY}\n" +
                "Màn hình: ${dm.widthPixels}x${dm.heightPixels}, ${dm.densityDpi} dpi"
        })

        root.addView(makeButton("Mở ứng dụng khác (tìm app cài đặt máy chiếu)") { showAppPicker() })
        shortcuts.forEach { (label, action) ->
            root.addView(makeButton(label) { openAction(action) })
        }

        setContentView(ScrollView(this).apply { addView(root) })
        root.getChildAt(1)?.requestFocus()
    }

    private fun makeButton(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        isFocusable = true
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(8) }
        setOnClickListener { onClick() }
    }

    private fun openAction(action: String) {
        try {
            startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Máy này không có mục này", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Không đủ quyền mở mục này", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAppPicker() {
        val pm = packageManager
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(query, 0)
            .sortedBy { it.loadLabel(pm).toString().lowercase() }
        val names = apps.map { it.loadLabel(pm).toString() }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Chọn ứng dụng để mở")
            .setItems(names) { _, i ->
                pm.getLaunchIntentForPackage(apps[i].activityInfo.packageName)?.let { startActivity(it) }
            }
            .setNegativeButton("Đóng", null)
            .show()
    }
}
