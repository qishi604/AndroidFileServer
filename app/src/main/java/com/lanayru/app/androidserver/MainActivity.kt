package com.lanayru.app.androidserver

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mIsInit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFileEx()
    }

    override fun onResume() {
        super.onResume()
        refreshIp()
    }

    private fun initFileEx() {
        val checkSelfPermission = PermissionChecker.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
        if (PERMISSION_GRANTED != checkSelfPermission) {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), REQ_STORAGE)
            return
        }

        FileEx.startWithLifeCycle(lifecycle)
        mIsInit = true
    }

    private fun refreshIp() {
        if (!mIsInit) {
            return
        }

        val ip = FileEx.getIp()

        tv_text.text = if (ip != null) {
            "浏览器输入 ${ip.hostAddress}:${PORT}"
        } else {
            "请链接到网络！"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_STORAGE-> {
                if (grantResults[0] == PERMISSION_GRANTED) {
                    initFileEx()
                    refreshIp()
                }
            }
        }
    }

    companion object {
        private const val REQ_STORAGE = 0x10
    }
}