package io.trtc.uikit.videochat.page.meet

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tencent.imsdk.v2.V2TIMUserFullInfo
import io.trtc.uikit.videochat.R
import io.trtc.uikit.videochat.common.Theme
import io.trtc.uikit.videochat.common.utils.VideoChatDataReporter
import io.trtc.uikit.videochat.manager.UserInfoStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 1v1 社交核心大厅单页组件。
 *
 * 外部只需调用 [setPageUsers] 投喂全量数据快照。
 */
class MeetPage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class Template { LIST, GRID }

    interface Listener {
        fun onRefreshRequested()
        fun onLoadMoreRequested()
    }

    private var recyclerView: RecyclerView? = null
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var emptyView: View? = null
    private var listener: Listener? = null
    private val adapter = MeetUserAdapter()
    private var isLoadingMore = false
    private var hasMore = true
    private var currentTemplate = Template.LIST
    private var collectionJob: Job? = null

    private val stopLoadingRunnable = Runnable {
        isLoadingMore = false
        swipeRefresh.isRefreshing = false
    }

    private val dataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            updateEmptyView(adapter.itemCount == 0)
        }
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            updateEmptyView(adapter.itemCount == 0)
        }
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            updateEmptyView(adapter.itemCount == 0)
        }
    }

    init {
        initView()
        adapter.registerAdapterDataObserver(dataObserver)
        VideoChatDataReporter.reportMetrics(VideoChatDataReporter.MEET_PAGE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        collectionJob = CoroutineScope(Dispatchers.Main).launch {
            UserInfoStore.shared.selfFollowingUsers.collect {
                for (i in 0 until adapter.itemCount) {
                    adapter.notifyItemChanged(i, MeetUserAdapter.PAYLOAD_FOLLOW_STATE)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelLoadingTimeout()
        collectionJob?.cancel()
    }

    fun setListener(listener: Listener) { this.listener = listener }

    fun setLayoutTemplate(template: Template) {
        if (currentTemplate == template) return
        currentTemplate = template
        val isGrid = template == Template.GRID
        adapter.isCardLayout = isGrid
        recyclerView?.layoutManager = if (isGrid) GridLayoutManager(context, 2) else LinearLayoutManager(context)
        adapter.notifyDataSetChanged()
    }

    fun setPageUsers(users: List<V2TIMUserFullInfo>, hasMore: Boolean) {
        cancelLoadingTimeout()
        this.hasMore = hasMore
        isLoadingMore = false
        swipeRefresh.isRefreshing = false
        adapter.submitList(users)
    }

    private fun initView() {
        inflateLayout()
        setupSwipeRefresh()
        setupRecyclerView()
    }

    private fun inflateLayout() {
        LayoutInflater.from(context).inflate(R.layout.videochat_page_user_list, this, true)
        recyclerView = findViewById(R.id.recycler_view)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        emptyView = findViewById(R.id.empty_view)
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(Theme.PINK, Theme.PURPLE)
        swipeRefresh.setOnRefreshListener {
            scheduleLoadingTimeout()
            listener?.onRefreshRequested()
        }
    }

    private fun setupRecyclerView() {
        adapter.isCardLayout = false
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.addOnScrollListener(createLoadMoreListener())
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        emptyView?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView?.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun createLoadMoreListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (isLoadingMore || !hasMore) return
                val lm = rv.layoutManager ?: return
                val lastVisible = when (lm) {
                    is GridLayoutManager -> lm.findLastVisibleItemPosition()
                    is LinearLayoutManager -> lm.findLastVisibleItemPosition()
                    else -> return
                }
                if (lastVisible >= lm.itemCount - 2 && lm.itemCount > 0) {
                    isLoadingMore = true
                    scheduleLoadingTimeout()
                    listener?.onLoadMoreRequested()
                }
            }
        }
    }

    private fun scheduleLoadingTimeout() {
        removeCallbacks(stopLoadingRunnable)
        postDelayed(stopLoadingRunnable, Companion.LOADING_TIMEOUT_MS)
    }

    private fun cancelLoadingTimeout() {
        removeCallbacks(stopLoadingRunnable)
    }

    companion object {
        private const val LOADING_TIMEOUT_MS = 15_000L
    }
}
