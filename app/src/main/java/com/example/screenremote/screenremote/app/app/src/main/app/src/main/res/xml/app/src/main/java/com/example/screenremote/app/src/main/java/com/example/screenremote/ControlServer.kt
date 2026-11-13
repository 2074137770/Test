package com.example.screenremote

import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

object ControlServer {

    private var running = false

    fun start(port: Int = 8080) {
        if (running) return
        running = true

        thread(isDaemon = true) {
            try {
                val server = ServerSocket(port)
                while (true) {
                    val socket = server.accept()
                    thread { handle(socket) }
                }
            } catch (_: Exception) {
                running = false
            }
        }
    }

    private fun handle(socket: Socket) {
        socket.use {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val requestLine = reader.readLine() ?: return
            val path = requestLine.split(" ").getOrNull(1) ?: "/"

            val writer = PrintWriter(
                OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8),
                true
            )

            when {
                path.startsWith("/swipe") -> {
                    val uri = Uri.parse(path)
                    val dir = uri.getQueryParameter("dir") ?: "up"
                    SwipeAccessibilityService.instance?.swipe(dir)

                    writer.print(
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain; charset=utf-8\r\n" +
                                "Connection: close\r\n\r\nOK"
                    )
                }

                path == "/" -> {
                    writer.print(
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/html; charset=utf-8\r\n" +
                                "Connection: close\r\n\r\n$HTML"
                    )
                }

                else -> {
                    writer.print(
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Connection: close\r\n\r\n"
                    )
                }
            }
            writer.flush()
        }
    }

    private val HTML = """
        <!doctype html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width,initial-scale=1">
          <title>平板刷视频遥控</title>
          <style>
            body { font-family: sans-serif; padding: 16px; }
            button {
              width: 100%; font-size: 28px; padding: 28px 0;
              margin: 10px 0; border-radius: 14px; border: 0;
              background: #222; color: #fff;
            }
            #pad {
              height: 50vh; margin-top: 16px; border-radius: 14px;
              background: #eee; display: flex; align-items: center;
              justify-content: center; font-size: 20px; color: #666;
              touch-action: none;
            }
          </style>
        </head>
        <body>
          <h2>平板刷视频遥控</h2>
          <button onclick="cmd('up')">⬆️ 上滑 / 下一个</button>
          <button onclick="cmd('down')">⬇️ 下滑 / 上一个</button>
          <div id="pad">在这里上下滑动</div>

          <script>
            function cmd(d) {
              fetch('/swipe?dir=' + d).catch(() => {});
            }

            let startY = null;
            const pad = document.getElementById('pad');

            pad.addEventListener('touchstart', e => {
              startY = e.touches[0].clientY;
              e.preventDefault();
            }, { passive: false });

            pad.addEventListener('touchend', e => {
              if (startY === null) return;
              const endY = e.changedTouches[0].clientY;
              const dy = endY - startY;
              if (Math.abs(dy) > 30) {
                cmd(dy < 0 ? 'up' : 'down');
              }
              startY = null;
              e.preventDefault();
            }, { passive: false });
          </script>
        </body>
        </html>
    """.trimIndent()
}
