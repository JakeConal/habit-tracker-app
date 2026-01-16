package com.example.habittracker.ui.setting.subsettings

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentReviewChallengeBinding
import com.example.habittracker.ui.challenge.ChallengeDetailActivity
import com.example.habittracker.ui.challenge.ChallengeReviewAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ReviewChallengeFragment : Fragment() {

    private var _binding: FragmentReviewChallengeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReviewChallengeViewModel by viewModels()
    private lateinit var adapter: ChallengeReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeToAction()
        setupListeners()
        observeViewModel()
        viewModel.loadPendingChallenges()
    }

    private fun setupRecyclerView() {
        adapter = ChallengeReviewAdapter { challenge ->
            val intent = Intent(requireContext(), ChallengeDetailActivity::class.java).apply {
                putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_ID, challenge.id)
                putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_TITLE, challenge.title)
                putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_DESCRIPTION, challenge.description)
                putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_DETAIL, challenge.detail)
                putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_IMAGE_URL, challenge.imgURL)
                putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_DURATION, challenge.duration.name)
                putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_REWARD, challenge.reward)
                putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_KEY_RESULTS, challenge.keyResults)
                putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_CREATOR_ID, challenge.creatorId)
                putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_CREATED_AT, challenge.createdAt)
                putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_PARTICIPANT_COUNT, challenge.participantCount)
                putExtra(ChallengeDetailActivity.EXTRA_HIDE_JOIN_BUTTON, true)
            }
            startActivity(intent)
        }
        binding.rvPendingChallenges.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ReviewChallengeFragment.adapter
        }
    }

    private fun setupSwipeToAction() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                // Trigger action when swiped 25% of the screen width
                return 0.25f
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val challenge = adapter.currentList[position]

                if (direction == ItemTouchHelper.RIGHT) {
                    // Swipe Right to Approve
                    viewModel.approveChallenge(challenge)
                } else if (direction == ItemTouchHelper.LEFT) {
                    // Swipe Left to Reject
                    viewModel.rejectChallenge(challenge)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top

                // Limit the visible displacement to keep the revealed area roughly square
                val swipeLimit = itemHeight.toFloat()
                val limitedDX = if (dX > 0) min(dX, swipeLimit) else max(dX, -swipeLimit)

                val isCanceled = dX == 0f && !isCurrentlyActive

                if (isCanceled) {
                    super.onChildDraw(c, recyclerView, viewHolder, limitedDX, dY, actionState, isCurrentlyActive)
                    return
                }

                val paint = Paint()
                val cornerRadius = 24 * resources.displayMetrics.density
                if (dX > 0) { // Swiping to the right (Approve)
                    paint.color = ContextCompat.getColor(requireContext(), R.color.habit_completed)
                    val background = RectF(
                        itemView.left.toFloat(),
                        itemView.top.toFloat(),
                        itemView.left.toFloat() + limitedDX,
                        itemView.bottom.toFloat()
                    )
                    c.drawRoundRect(background, cornerRadius, cornerRadius, paint)

                    // Draw check icon centered in revealed square
                    val icon: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
                    icon?.let {
                        val iconSize = (itemHeight * 0.25).toInt()
                        val iconTop = itemView.top + (itemHeight - iconSize) / 2
                        val iconLeft = itemView.left + (limitedDX - iconSize) / 2
                        it.setBounds(iconLeft.toInt(), iconTop, (iconLeft + iconSize).toInt(), iconTop + iconSize)
                        it.setTint(Color.WHITE)
                        it.draw(c)
                    }
                } else if (dX < 0) { // Swiping to the left (Reject)
                    paint.color = ContextCompat.getColor(requireContext(), R.color.destructive_red)
                    val background = RectF(
                        itemView.right.toFloat() + limitedDX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                    c.drawRoundRect(background, cornerRadius, cornerRadius, paint)

                    // Draw trash icon centered in revealed square
                    val icon: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.ic_trash)
                    icon?.let {
                        val iconSize = (itemHeight * 0.25).toInt()
                        val iconTop = itemView.top + (itemHeight - iconSize) / 2
                        val iconRight = itemView.right - (abs(limitedDX) - iconSize) / 2
                        it.setBounds((iconRight - iconSize).toInt(), iconTop, iconRight.toInt(), iconTop + iconSize)
                        it.setTint(Color.WHITE)
                        it.draw(c)
                    }
                }

                // If currently active and past limit, tell ItemTouchHelper to keep the limited position
                super.onChildDraw(c, recyclerView, viewHolder, limitedDX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvPendingChallenges)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPendingChallenges()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.pendingChallenges.collect { challenges ->
                adapter.submitList(challenges)
                binding.tvEmptyState.visibility = if (challenges.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.swipeRefresh.isRefreshing = isLoading
            }
        }

        lifecycleScope.launch {
            viewModel.message.collect { message ->
                message?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
