package com.example.habittracker.ui.leaderboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.habittracker.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LeaderboardFragment : Fragment() {

    private lateinit var rvLeaderboard: RecyclerView
    private lateinit var swipeRefreshLeaderboard: SwipeRefreshLayout
    private lateinit var adapter: LeaderboardAdapter
    private val viewModel: LeaderboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        observeViewModel(view)
    }

    private fun setupRecyclerView(view: View) {
        rvLeaderboard = view.findViewById(R.id.rv_leaderboard)
        swipeRefreshLeaderboard = view.findViewById(R.id.swipeRefreshLeaderboard)
        adapter = LeaderboardAdapter()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        rvLeaderboard.layoutManager = linearLayoutManager
        rvLeaderboard.adapter = adapter

        swipeRefreshLeaderboard.setOnRefreshListener {
            viewModel.loadTopUsers(isRefresh = true)
        }

        rvLeaderboard.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val totalItemCount = linearLayoutManager.itemCount
                val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()

                if (!viewModel.isLoading.value && viewModel.hasMoreData &&
                    totalItemCount <= (lastVisibleItem + 5)) {
                    viewModel.loadTopUsers(isRefresh = false)
                }
            }
        })
    }

    private fun observeViewModel(view: View) {
        val firstAvatar = view.findViewById<ImageView>(R.id.iv_first_avatar)
        val firstName = view.findViewById<TextView>(R.id.tv_first_name)
        val firstPoints = view.findViewById<TextView>(R.id.tv_first_points)

        val secondAvatar = view.findViewById<ImageView>(R.id.iv_second_avatar)
        val secondName = view.findViewById<TextView>(R.id.tv_second_name)
        val secondPoints = view.findViewById<TextView>(R.id.tv_second_points)

        val thirdAvatar = view.findViewById<ImageView>(R.id.iv_third_avatar)
        val thirdName = view.findViewById<TextView>(R.id.tv_third_name)
        val thirdPoints = view.findViewById<TextView>(R.id.tv_third_points)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                swipeRefreshLeaderboard.isRefreshing = isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topUsers.collectLatest { topUsers ->
                if (topUsers.isNotEmpty()) {
                    // Update top 3
                    if (topUsers.size >= 1) {
                        firstName.text = topUsers[0].name
                        firstPoints.text = getString(R.string.points_format, topUsers[0].points)
                        loadAvatar(firstAvatar, topUsers[0].avatarUrl)
                    }
                    if (topUsers.size >= 2) {
                        secondName.text = topUsers[1].name
                        secondPoints.text = getString(R.string.points_format, topUsers[1].points)
                        loadAvatar(secondAvatar, topUsers[1].avatarUrl)
                    }
                    if (topUsers.size >= 3) {
                        thirdName.text = topUsers[2].name
                        thirdPoints.text = getString(R.string.points_format, topUsers[2].points)
                        loadAvatar(thirdAvatar, topUsers[2].avatarUrl)
                    }

                    // Feed the rest to the adapter
                    if (topUsers.size > 3) {
                        // Create a list with ranks assigned based on position
                        val rankedUsers = topUsers.subList(3, topUsers.size).mapIndexed { index, user ->
                            user.copy(rank = index + 4)
                        }
                        adapter.submitList(rankedUsers)
                    } else {
                        adapter.submitList(emptyList())
                    }
                }
            }
        }
    }

    private fun loadAvatar(imageView: ImageView, url: String?) {
        if (!url.isNullOrEmpty()) {
            Glide.with(this)
                .load(url)
                .circleCrop()
                .placeholder(R.drawable.ic_person)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.ic_person)
        }
    }
}
