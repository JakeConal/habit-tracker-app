package com.example.habittracker.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.data.model.Post
import com.example.habittracker.databinding.ItemPostBinding
import com.google.firebase.database.FirebaseDatabase

class PostAdapter(
    private val postList: MutableList<Post>,
    private val context: Context,
    private val currentUserId: String // Ensure you pass currentUserId to Adapter
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentPost = postList[position]

        holder.binding.tvAuthorName.text = currentPost.authorName
        holder.binding.tvContent.text = currentPost.content

        holder.binding.ivMoreOptions.setOnClickListener {
            val popupMenu = PopupMenu(context, holder.binding.ivMoreOptions)

            // Inflate logic based on ownership
            if (currentPost.userId == currentUserId) {
                // If it's my post, I can delete it (assuming this adapter is used in Profile too)
                popupMenu.menu.add("Delete")
            } else {
                // If it's not my post, I can hide it
                popupMenu.menu.add("Hide")
            }
            // Everyone can share
            popupMenu.menu.add("Share")

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "Delete" -> {
                        deletePost(currentPost.id, position) // Implement delete logic
                        true
                    }
                    "Hide" -> {
                        hidePost(position) // Implement hide logic (local removal)
                        true
                    }
                    "Share" -> {
                        sharePost(currentPost)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int = postList.size

    private fun sharePost(post: Post) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out this habit update!")
            putExtra(Intent.EXTRA_TEXT, "${post.content}\n\nShared from Habit Tracker App")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share post via"))
    }

    private fun hidePost(position: Int) {
        // Remove locally from the list
        if (position in postList.indices) {
            postList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, postList.size)
        }
        // Optionally save "hidden" state to SharedPreferences or Room to persist across restarts
    }

    private fun deletePost(postId: String, position: Int) {
        // Logic to delete from Firebase
        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postId)
        databaseReference.removeValue().addOnSuccessListener {
            if (position in postList.indices) {
                postList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, postList.size)
            }
            Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show()
        }
    }
}

