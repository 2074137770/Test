package com.example.screenremote

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class SwipeAccessibilityService : AccessibilityService() {

    companion object {
        var instance: SwipeAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        ControlServer.start(8080)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun swipe(direction: String) {
        val dm = resources.displayMetrics
        val w = dm.widthPixels
        val h = dm.heightPixels
        val x = w / 2f

        val startY: Float
        val endY: Float

        if (direction == "up") {
            // 手指从下往上滑：视频下一个
            startY = h * 0.75f
            endY = h * 0.25f
        } else {
            // 手指从上往下滑：视频上一个
            startY = h * 0.25f
            endY = h * 0.75f
        }

        val path = Path().apply {
            moveTo(x, startY)
            lineTo(x, endY)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, 300)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        dispatchGesture(gesture, null, null)
    }
}
