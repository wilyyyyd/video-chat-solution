package io.trtc.uikit.videochat.page.beauty

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.utils.PermissionRequest
import io.trtc.uikit.videochat.page.beauty.tebeauty.TEBeautyManager
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.trtc.TRTCCloud
import com.trtc.tuikit.common.permission.PermissionCallback
import io.trtc.tuikit.atomicxcore.api.call.CallMediaType

class BeautyActivity : AppCompatActivity() {

    private lateinit var videoView: TXCloudVideoView
    private var isViewInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.videochat_activity_beauty)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        PermissionRequest.requestPermissions(application, CallMediaType.Video, object : PermissionCallback() {
            override fun onGranted() {
                if (!isViewInitialized) {
                    isViewInitialized = true
                    initViews()
                }
            }

            override fun onDenied() {
                finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        TEBeautyManager.clearBeautyView()
        TRTCCloud.sharedInstance(this).stopLocalPreview()
    }

    private fun initViews() {
        videoView = findViewById(R.id.video_view)

        findViewById<FrameLayout>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<FrameLayout>(R.id.fab_beauty).setOnClickListener {
            BeautyIntegration.showBeautyDialog(this)
        }

        TRTCCloud.sharedInstance(applicationContext).startLocalPreview(true, videoView)
        BeautyIntegration.setupVideoProcessor()
        videoView.post { BeautyIntegration.showBeautyDialog(this) }
    }
}
