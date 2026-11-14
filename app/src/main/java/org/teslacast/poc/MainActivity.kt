package org.teslacast.poc

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val pool = Executors.newCachedThreadPool()
    private var server: ServerSocket? = null
    private lateinit var output: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 48, 32, 24) }
        output = TextView(this).apply { textSize = 17f; text = "TeslaCast network diagnostic\nNot yet a screen-mirroring app." }
        val start = Button(this).apply { text = "START LOCAL TEST SERVER"; setOnClickListener { startServer() } }
        val stop = Button(this).apply { text = "STOP SERVER"; setOnClickListener { stopServer() } }
        layout.addView(output); layout.addView(start); layout.addView(stop); setContentView(layout)
    }

    private fun addresses(): String = try {
        NetworkInterface.getNetworkInterfaces().toList().flatMap { nic ->
            nic.inetAddresses.toList().filterIsInstance<Inet4Address>().filter { !it.isLoopbackAddress }
                .map { "${nic.name}: http://${it.hostAddress}:9090" }
        }.joinToString("\n")
    } catch (e: Exception) { "IP discovery failed: ${e.message}" }

    private fun startServer() {
        if (server != null) return
        pool.execute {
            try {
                val s = ServerSocket(9090, 16, java.net.InetAddress.getByName("0.0.0.0"))
                server = s
                runOnUiThread { output.text = "LISTENING 0.0.0.0:9090\n${addresses()}\n\nTest from another phone first, then Tesla." }
                while (!s.isClosed) {
                    val client = s.accept()
                    pool.execute { handle(client) }
                }
            } catch (e: Exception) {
                runOnUiThread { output.text = "Server stopped/error: ${e.message}" }
            } finally { server = null }
        }
    }

    private fun handle(socket: Socket) {
        socket.use { client ->
            try {
                client.soTimeout = 3000
                val input = client.getInputStream().bufferedReader()
                val request = input.readLine() ?: return
                val remote = client.inetAddress.hostAddress ?: "unknown"
                val html = """<!doctype html><html><meta name="viewport" content="width=device-width,initial-scale=1"><body style="background:#101927;color:#74f3d4;font:20px sans-serif;padding:24px"><h2>TeslaCast Network Test</h2><p>LOCAL HTTP CONNECTION OK</p><p>Client: $remote</p><p>Request: ${request.replace("<", "&lt;")}</p></body></html>"""
                val bytes = html.toByteArray(Charsets.UTF_8)
                client.getOutputStream().apply {
                    write("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\nCache-Control: no-store\r\n\r\n".toByteArray())
                    write(bytes); flush()
                }
                runOnUiThread { output.text = "Last client: $remote\n${addresses()}" }
            } catch (_: Exception) { }
        }
    }

    private fun stopServer() { try { server?.close() } catch (_: Exception) {} ; server = null; output.text = "Stopped" }
    override fun onDestroy() { stopServer(); pool.shutdownNow(); super.onDestroy() }
}
