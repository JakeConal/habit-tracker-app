package com.example.habittracker.ui.social.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentMyFriendBinding
import com.example.habittracker.ui.social.friend.FriendListAdapter
import kotlinx.coroutines.launch

class MyFriendFragment : Fragment() {

    private var _binding: FragmentMyFriendBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var friendListAdapter: FriendListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment())[ProfileViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        friendListAdapter = FriendListAdapter(
            onSearchQueryChanged = { query ->
                viewModel.updateSearchQuery(query)
            },
            onAcceptRequest = { request ->
                viewModel.acceptFriendRequest(request)
            },
            onRejectRequest = { request ->
                viewModel.rejectFriendRequest(request)
            },
            onViewProfile = { friend ->
                val bundle = Bundle().apply {
                    putString("friendId", friend.id)
                }
                findNavController().navigate(R.id.action_global_to_friend_profile, bundle)
            },
            onUnfriend = { friend ->
                viewModel.unfriend(friend)
            },
            onAddFriend = { user ->
                viewModel.sendFriendRequest(user)
            }
        )

        binding.rvFriendsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendListAdapter
            setHasFixedSize(false)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.filteredFriendListItems.collect { items ->
                val isEmpty = items.isEmpty() || (items.size == 1 && items[0] is FriendListItem.SearchHeader && viewModel.searchQuery.value.isEmpty())
                // Actually adapter handles empty state item, so list is rarely empty unless initial load?
                // The ViewModel adds EmptyState item if filtered list is empty.
                // So we can just submit list.
                // But we should toggle 'emptyFriendsState' visibility based on initial state or total emptiness?
                // ViewModel logic: Adds EmptyState item if no results.
                // But if "No friends yet", it also adds EmptyState.
                // So RecyclerView is always visible?
                // Let's rely on ViewModel items.
                
                binding.rvFriendsList.visibility = View.VISIBLE
                binding.emptyFriendsState.visibility = View.GONE
                friendListAdapter.submitList(items)
            }
        }

        lifecycleScope.launch {
            viewModel.message.collect { msg ->
                msg?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearMessage()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    // Create a clearError in VM or just leave it
                    // viewModel.clearError() 
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MyFriendFragment()
    }
}
