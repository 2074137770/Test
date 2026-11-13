package com.example.screenremote

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.Inet4Address
import java.net.NetworkInterface

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ip = getLocalIpAddress()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
        }

        val tv = TextView(this).apply {
            textSize = 20f
            text = """
                平板 IP：$ip

                手机浏览器打开：
                http://$ip:8080

                1. 先点下面按钮开启本应用的无障碍服务
                2. 保持平板亮屏、视频 App 在前台
                3. 手机和必须连同一个 WiFi
            """.trimIndent()
        }

        val btn = Button(this).apply {
            text = "打开无障碍设置"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        layout.addView(tv)
        layout.addView(btn)
        setContentView(layout)
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                for (addr in intf.inetAddresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "未知"
                    }
                }
            }
        } catch (_: Exception) {
        }
        return "未知"
    }
}
