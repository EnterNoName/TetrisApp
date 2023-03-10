package com.example.tetrisapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.tetrisapp.R
import com.example.tetrisapp.databinding.FragmentUserBinding
import com.example.tetrisapp.model.local.model.UserInfo

class UsersRecyclerViewAdapter(private val mValues: List<UserInfo>) :
    RecyclerView.Adapter<UsersRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.user = mValues[position]
        holder.setViews()
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    class ViewHolder(binding: FragmentUserBinding) : RecyclerView.ViewHolder(binding.root) {
        val mUserName: TextView
        val mUserPhoto: ImageView
        var user: UserInfo? = null

        init {
            mUserName = binding.tvUsername
            mUserPhoto = binding.ivProfileImage
        }

        fun setViews() {
            mUserName.text = user!!.name
            user?.photoUrl?.let { photoUri ->
                mUserPhoto.load(photoUri) {
                    placeholder(R.drawable.ic_round_account_circle_24)
                    error(R.drawable.ic_round_account_circle_24)
                    transformations(CircleCropTransformation())
                    crossfade(true)
                    target(onSuccess = {
                        mUserPhoto.imageTintList = null
                        mUserPhoto.setImageDrawable(it)
                    })
                }
            }
        }

        override fun toString(): String {
            return super.toString() + "'" + user.toString() + "'"
        }
    }
}