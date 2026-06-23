package io.trtc.uikit.demo.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tencent.cloud.tuikit.engine.call.TUICallEngine
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.effect.beautykit.tuiextension.TUIBeautyKit
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.interfaces.TUICallback
import io.trtc.uikit.videochat.common.widget.toast.VideoChatToast
import io.trtc.uikit.demo.debug.GenerateTestUserSig
import io.trtc.uikit.demo.SocialMainActivity
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.uikit.demo.R

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var editUserId: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action == Intent.ACTION_MAIN
        ) {
            finish()
            return
        }
        setContentView(R.layout.activity_login)
        initView()
        initTencentBeauty()
    }

    private fun initTencentBeauty() {
        TUIBeautyKit.instance.init(
            this,
            GenerateTestUserSig.TENCENT_EFFECT_LICENSE_URL,
            GenerateTestUserSig.TENCENT_EFFECT_LICENSE_KEY
        )
    }

    private fun initView() {
        editUserId = findViewById(R.id.et_userId)
        btnLogin = findViewById(R.id.btn_login)
        btnLogin.setOnClickListener {
            val userId = editUserId.text.toString().trim()
            login(userId)
        }
    }

    private fun login(userId: String) {
        if (userId.isEmpty()) {
            VideoChatToast.show(this.getString(R.string.app_userid_empty))
            return
        }
        val userSig = GenerateTestUserSig.genTestUserSig(userId)
        TUILogin.login(this, GenerateTestUserSig.SDKAPPID, userId, userSig, object : TUICallback() {
            override fun onSuccess() {
                Log.i(TAG, "login onSuccess, userId: $userId")
                LoginStore.shared.login(this@LoginActivity, GenerateTestUserSig.SDKAPPID, userId, userSig, null)
                initCallEngine(userId, userSig)
            }

            override fun onError(errorCode: Int, errorMessage: String?) {
                VideoChatToast.show(getString(R.string.app_login_failed))
            }
        })
    }

    private fun initCallEngine(userId: String, userSig: String) {
        TUICallEngine.createInstance(this)
            .init(GenerateTestUserSig.SDKAPPID, userId, userSig, object : TUICommonDefine.Callback {
                override fun onSuccess() {
                    navigateToMain()
                }

                override fun onError(errCode: Int, errMsg: String?) {
                }
            })
    }

    private fun navigateToMain() {
        val intent = Intent(this, SocialMainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
