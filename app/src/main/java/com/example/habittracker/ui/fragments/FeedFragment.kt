package com.example.habittracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.habittracker.R
import com.example.habittracker.data.models.Post
import com.example.habittracker.ui.adapters.FeedAdapter
import com.example.habittracker.ui.viewmodels.FeedViewModel

class FeedFragment : Fragment() {

    private val viewModel: FeedViewModel by viewModels()
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var rvFeedPosts: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun initViews(view: View) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        rvFeedPosts = view.findViewById(R.id.rvFeedPosts)
    }

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter(
            onLikeClick = { post -> viewModel.toggleLike(post.id) },
            onCommentClick = { post -> openComments(post) },
            onPostClick = { post -> openPostDetail(post) },
            onMoreClick = { post, anchor -> showMoreOptions(post, anchor) },
            onShareInputClick = { openCreatePost() }
        )

        rvFeedPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            setHasFixedSize(true)

            // Optimize RecyclerView performance
            setItemViewCacheSize(20)
            isNestedScrollingEnabled = true

            // Create a RecycledViewPool for better memory management
            val viewPool = RecyclerView.RecycledViewPool()
            viewPool.setMaxRecycledViews(0, 5) // Header type
            viewPool.setMaxRecycledViews(1, 15) // Post type
            setRecycledViewPool(viewPool)

            // Infinite scroll
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                        && firstVisibleItemPosition >= 0
                    ) {
                        viewModel.loadMore()
                    }
                }
            })
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(
            R.color.accent_blue,
            R.color.primary_blue
        )
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun observeViewModel() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            feedAdapter.submitList(posts)
            swipeRefresh.isRefreshing = false
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefresh.isRefreshing = isLoading
        }
    }

    private fun openCreatePost() {
        Toast.makeText(requireContext(), "Create new post", Toast.LENGTH_SHORT).show()
    }

    private fun openComments(post: Post) {
        Toast.makeText(requireContext(), "Open comments for: ${post.author.name}", Toast.LENGTH_SHORT).show()
    }

    private fun openPostDetail(post: Post) {
        Toast.makeText(requireContext(), "Open post detail", Toast.LENGTH_SHORT).show()
    }

    private fun showMoreOptions(post: Post, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_post_options, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_report -> {
                    Toast.makeText(requireContext(), "Report post", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_share -> {
                    Toast.makeText(requireContext(), "Share post", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = FeedFragment()
    }
}
