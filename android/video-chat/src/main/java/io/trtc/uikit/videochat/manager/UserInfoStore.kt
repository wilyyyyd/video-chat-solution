package io.trtc.uikit.videochat.manager

import android.util.Log
import com.tencent.cloud.tuikit.engine.common.ContextProvider
import com.tencent.imsdk.common.IMLog
import com.tencent.imsdk.v2.V2TIMFollowInfo
import com.tencent.imsdk.v2.V2TIMFollowOperationResult
import com.tencent.imsdk.v2.V2TIMFriendshipListener
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMUserFullInfo
import com.tencent.imsdk.v2.V2TIMUserFullInfo.V2TIM_GENDER_FEMALE
import com.tencent.imsdk.v2.V2TIMUserFullInfo.V2TIM_GENDER_MALE
import com.tencent.imsdk.v2.V2TIMUserInfoResult
import com.tencent.imsdk.v2.V2TIMValueCallback
import io.trtc.uikit.videochat.R

import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

internal class UserInfoStore private constructor() {
    private var selfId = ""
    private val _selfFollowingUsers = MutableStateFlow<LinkedHashSet<String>>(LinkedHashSet())
    private val _selfFollowers = MutableStateFlow<LinkedHashSet<String>>(LinkedHashSet())
    internal val selfFollowingUsers: StateFlow<Set<String>> = _selfFollowingUsers.asStateFlow()
    internal val selfFollowers: StateFlow<Set<String>> = _selfFollowers.asStateFlow()

    private val friendshipListener = object : V2TIMFriendshipListener() {
        override fun onMyFollowingListChanged(userInfoList: List<V2TIMUserFullInfo>?, isAdd: Boolean) {
            if (userInfoList.isNullOrEmpty()) return
            _selfFollowingUsers.update {
                val newFollowingUsers = LinkedHashSet(it)
                userInfoList.forEach { info ->
                    val userId = info.userID ?: return@forEach
                    if (isAdd) newFollowingUsers.add(userId) else newFollowingUsers.remove(userId)
                }
                newFollowingUsers
            }
        }

        override fun onMutualFollowersListChanged(userInfoList: MutableList<V2TIMUserFullInfo>?, isAdd: Boolean) {
            super.onMutualFollowersListChanged(userInfoList, isAdd)
            if (userInfoList.isNullOrEmpty()) return
            _selfFollowers.update {
                val newFollowers =  LinkedHashSet(it)
                userInfoList.forEach { info ->
                    val userId = info.userID ?: return@forEach
                    if (isAdd) newFollowers.add(userId) else newFollowers.remove(userId)
                }
                newFollowers
            }
        }
    }

    interface FollowInfoCallback {
        fun onFailure(code: Int, desc: String)
        fun onSuccess(followingCount: Long, followersCount: Long)
    }

    init {
        V2TIMManager.getFriendshipManager().addFriendListener(friendshipListener)
        initSelfInfo()
        observeSelfUser()
    }

    internal fun getUserProfile(userId: String, onResult: (V2TIMUserFullInfo?) -> Unit, onError: () -> Unit = {}) {
        if (userId.isEmpty()) { onError(); return }
        V2TIMManager.getInstance().getUsersInfo(listOf(userId), object : V2TIMValueCallback<List<V2TIMUserFullInfo>> {
            override fun onSuccess(infoList: List<V2TIMUserFullInfo>?) {
                val info = infoList?.firstOrNull()
                onResult(info)
            }
            override fun onError(code: Int, desc: String?) {
                Log.e(TAG, "[getUserProfile] error: code=$code, desc=$desc")
                onError()
            }
        })
    }

    internal fun getUserFollowInfo(userId: String, callback: FollowInfoCallback?) {
        val userIds = listOf(userId)
        V2TIMManager.getFriendshipManager().getUserFollowInfo(userIds, object : V2TIMValueCallback<List<V2TIMFollowInfo>> {
            override fun onSuccess(followInfos: List<V2TIMFollowInfo>?) {
                followInfos?.forEach {
                    if (it.userID == userId) {
                        callback?.onSuccess(it.followingCount, it.followersCount)
                    }
                }
            }

            override fun onError(code: Int, message: String?) {
                callback?.onFailure(code, message ?: "")
            }
        })
    }

    internal fun isFollowing(userId: String) = _selfFollowingUsers.value.contains(userId)

    internal fun fellowUser(userIds: List<String>, onSuccess: (() -> Unit)? = null, onError: (() -> Unit)? = null) {
        V2TIMManager.getFriendshipManager().followUser(userIds, object : V2TIMValueCallback<List<V2TIMFollowOperationResult>> {
            override fun onSuccess(results: List<V2TIMFollowOperationResult>) {
                Log.i(TAG, "fellowUser success, list=$userIds")
                onSuccess?.invoke()
            }
            override fun onError(code: Int, message: String?) {
                Log.e(TAG, "fellowUser fail, code=$code message=$message")
                onError?.invoke()
            }
        })
    }

    internal fun unFellowUsers(userIds: List<String>, onSuccess: (() -> Unit)? = null, onError: (() -> Unit)? = null) {
        V2TIMManager.getFriendshipManager().unfollowUser(userIds, object : V2TIMValueCallback<List<V2TIMFollowOperationResult>> {
            override fun onSuccess(results: List<V2TIMFollowOperationResult>) {
                Log.i(TAG, "unFellowUsers success, list=$userIds")
                onSuccess?.invoke()
            }
            override fun onError(code: Int, message: String?) {
                Log.e(TAG, "unFellowUsers fail, code=$code message=$message")
                onError?.invoke()
            }
        })
    }

    internal fun getUserLabelList(userInfo: V2TIMUserFullInfo): List<String> {
        val context = ContextProvider.getApplicationContext()
        val genderLabel = when(userInfo.gender) {
            V2TIM_GENDER_MALE -> context.getString(R.string.videochat_gender_male)
            V2TIM_GENDER_FEMALE -> context.getString(R.string.videochat_gender_female)
            else -> context.getString(R.string.videochat_gender_secret)
        }
        val ageLabel = userInfo.birthday.let { "$it" }
        return listOf(genderLabel, ageLabel)
    }

    internal fun dataReport(type : Int) {
        val param = JSONObject().apply {
            put("UIComponentType", type.toLong())
        }.toString()
        V2TIMManager.getInstance()
            .callExperimentalAPI("reportTUIFeatureUsage", param, object : V2TIMValueCallback<Any> {
                override fun onSuccess(t: Any?) {
                    // do nothing
                }

                override fun onError(code: Int, desc: String?) {
                    IMLog.e("video-chat-DataReporter", "reportFeatureUsage failed: $code $desc")
                }
            })
    }

    private fun initSelfInfo() {
        selfId = LoginStore.shared.loginState.loginUserInfo.value?.userID ?: ""
        if (selfId.isNotEmpty()) {
            fetchFollowingIds()
        }
    }

    private fun observeSelfUser() {
        CoroutineScope(Dispatchers.Main).launch {
            LoginStore.shared.loginState.loginUserInfo.collect { loginUserInfo ->
                if (loginUserInfo == null) return@collect
                val loginUserId = loginUserInfo.userID
                if (loginUserId.isNotEmpty() && loginUserId != selfId) {
                    selfId = loginUserId
                    fetchFollowingIds()
                } else {
                    resetState()
                }
            }
        }
    }

    private fun fetchFollowingIds() {
        V2TIMManager.getFriendshipManager().getMyFollowingList("", object : V2TIMValueCallback<V2TIMUserInfoResult> {
            override fun onSuccess(result: V2TIMUserInfoResult) {
                val ids = result.userFullInfoList?.mapNotNull { it.userID } ?: emptyList()
                _selfFollowingUsers.value = LinkedHashSet(ids)
                Log.i(TAG, "fetchFollowingIds success, count: ${ids.size}")
            }
            override fun onError(code: Int, desc: String?) {
                Log.e(TAG, "fetchFollowingIds failed: code=$code, desc=$desc")
            }
        })
    }

    private fun resetState() {
        _selfFollowingUsers.value = LinkedHashSet()
    }

    companion object {
        private const val TAG = "VideoChatStore"
        val shared by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { UserInfoStore() }
    }
}
