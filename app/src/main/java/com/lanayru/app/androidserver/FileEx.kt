package com.lanayru.app.androidserver

import android.os.Environment
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status.OK
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URLConnection

/**
 * 手机文件浏览器
 *
 * @author zhengqi
 * @version 1.0
 * @since 2020/8/9
 **/
const val PORT = 8888
object FileEx: NanoHTTPD(PORT), LifecycleObserver {

    private const val TAG = "FileServer"

    private val mRoot: File
        get() = File(Environment.getExternalStorageDirectory(), "")

    fun startWithLifeCycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
        start()
    }

    override fun serve(session: IHTTPSession?): Response {
        if (null == session) {
            return newFixedLengthResponse("Error!")
        }

        val uri = session.uri
        Log.i(TAG, "uri $uri")

        when (uri) {
            "/" -> {
                return getDirResponse(mRoot)
            }
            else -> {
                val file = getFile(uri)
                if (file.exists()) {
                    if (file.isDirectory) {
                        return getDirResponse(file)
                    }
                    return getFileResponse(file)
                }
            }
        }

        return getHtmlResponse("Error with $uri")
    }

    private fun getFile(name: String) = File(mRoot, name)

    private fun listFile(dir: File): String {
        val list = dir.listFiles()
        if (list?.isEmpty() == true) {
            return "No File at $dir"
        }

        val sb = StringBuilder()
        sb.append("<ul>")
        for (f in list) {
            val path = f.path.substring(mRoot.path.length)
            val li = """<li><a href="$path">${f.name}</a></li>"""
            sb.append(li)
        }
        sb.append("</ul>")
        return sb.toString()
    }

    private fun getFileResponse(file: File): Response {
        val fin = FileInputStream(file)
        var mime = URLConnection.guessContentTypeFromStream(fin)
        if (null == mime) {
            mime = URLConnection.guessContentTypeFromName(file.name)
        }
        Log.i(TAG, "mime_type $mime")
        return newFixedLengthResponse(OK, mime, fin, file.length())
    }

    private fun getDirResponse(file: File): Response {
        return getHtmlResponse(listFile(file))
    }

    private fun getHtmlResponse(body: String): Response {
        val html = "<html><body>$body</body><html>"
        return newFixedLengthResponse(html)
    }

    fun getIp(): InetAddress? {
        var networkInterfaces = NetworkInterface.getNetworkInterfaces()
        for (network in networkInterfaces) {
            if (!network.isLoopback) {
                for (inetAddress in network.inetAddresses) {
                    if (inetAddress.isSiteLocalAddress) {
                        return inetAddress;
                    }
                }
            }
        }
        return null;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        stop()
    }
}