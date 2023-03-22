package com.example.tetrisapp.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.tetrisapp.R
import com.example.tetrisapp.data.local.dao.LeaderboardDao
import com.example.tetrisapp.databinding.FragmentScoresListBinding
import com.example.tetrisapp.model.remote.response.PublicRecord
import com.example.tetrisapp.ui.adapters.ScoresRecyclerViewAdapter
import com.example.tetrisapp.ui.viewmodel.ScoresViewModel
import com.example.tetrisapp.util.ItemTouchCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ScoresFragment : Fragment() {
    private lateinit var binding: FragmentScoresListBinding
    @Inject lateinit var viewModel: ScoresViewModel

    @Inject lateinit var leaderboardDao: LeaderboardDao
    private var swipeHelper: ItemTouchHelper? = null
    private var firebaseUser: FirebaseUser? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val metrics = resources.displayMetrics

        swipeHelper = ItemTouchHelper(object : ItemTouchCallback(requireActivity()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                viewModel.deleteScore(pos)
            }
        }.also {
            it.width = metrics.widthPixels
            it.deleteIcon = requireActivity().getDrawable(R.drawable.ic_round_delete_24)
            it.deleteIcon?.setTint(requireActivity().getColor(R.color.white))
        })
    }

    @SuppressLint("UseCompatLoadingForDrawables", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScoresListBinding.inflate(inflater, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        binding.list.adapter = ScoresRecyclerViewAdapter(requireContext(), viewModel.scores)

        viewModel.uiState.asLiveData(Dispatchers.Main).observe(viewLifecycleOwner) { action ->
            when(action) {
                is ScoresViewModel.ScoresListAction.Load -> {
                    viewModel.scores.clear()
                    viewModel.scores.addAll(action.data.map { entry ->
                        PublicRecord(
                            score = entry.score,
                            level = entry.level,
                            lines = entry.lines,
                            date = entry.date!!,
                            name = "",
                            userId = ""
                        )
                    })
                    binding.list.adapter?.notifyDataSetChanged()
                }
                is ScoresViewModel.ScoresListAction.Delete -> {
                    viewModel.scores.removeAt(action.pos)
                    binding.list.adapter?.notifyItemRemoved(action.pos)
                }
                else -> {}
            }

            updateUi(firebaseUser)
        }


        // RecyclerView swipe handler
        if (firebaseUser == null) {
            swipeHelper?.attachToRecyclerView(binding.list)
        }

        // RecyclerView divider
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        divider.setDrawable(requireActivity().getDrawable(R.drawable.recyclerview_divider)!!)
        binding.list.addItemDecoration(divider)

        binding.btnBack.setOnClickListener {
            findNavController(binding.root).popBackStack()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUi(firebaseUser)
    }

    private fun updateUi(user: FirebaseUser?) {
        if (user != null) {
            binding.tvUsername.text = user.displayName
            binding.tvEmailAddress.text = user.email
            binding.tvStatistics.text = getString(R.string.users_statistics).format(user.displayName)
            binding.ivProfileImage.scaleType = ImageView.ScaleType.FIT_CENTER
            user.photoUrl?.let { photoUri ->
                binding.ivProfileImage.load(photoUri) {
                    placeholder(R.drawable.ic_round_account_circle_24)
                    error(R.drawable.ic_round_account_circle_24)
                    transformations(CircleCropTransformation())
                    crossfade(true)
                    target(onSuccess = {
                        binding.ivProfileImage.setImageDrawable(it)
                        binding.ivProfileImage.imageTintList = null
                    })
                }
            }
        } else {
            binding.userDataGroup.visibility = View.GONE
            binding.tvStatistics.text = getString(R.string.your_statistics)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            leaderboardDao.getGamesCount().let { count ->
                withContext(Dispatchers.Main) {
                    binding.tvGamesCount.text = getString(R.string.games_played).format(count ?: 0)
                }
            }


            leaderboardDao.getBestScore().let { bestScore ->
                withContext(Dispatchers.Main) {
                    binding.tvBestScore.text = getString(R.string.max_value).format(bestScore ?: 0)
                }
            }

            leaderboardDao.getAverageScore().let { avgScore ->
                withContext(Dispatchers.Main) {
                    binding.tvAverageScore.text = getString(R.string.average_value).format(avgScore ?: 0)
                }
            }

            leaderboardDao.getBestLevel().let { bestLevel ->
                withContext(Dispatchers.Main) {
                    binding.tvBestLevel.text = getString(R.string.max_value).format(bestLevel ?: 0)
                }
            }

            leaderboardDao.getAverageLevel().let { avgLevel ->
                withContext(Dispatchers.Main) {
                    binding.tvAverageLevel.text = getString(R.string.average_value).format(avgLevel ?: 0)
                }
            }

            leaderboardDao.getBestLines().let { bestLines ->
                withContext(Dispatchers.Main) {
                    binding.tvBestLines.text = getString(R.string.max_value).format(bestLines ?: 0)
                }
            }

            leaderboardDao.getAverageLines().let { avgLines ->
                withContext(Dispatchers.Main) {
                    binding.tvAverageLines.text = getString(R.string.average_value).format(avgLines ?: 0)
                }
            }
        }
    }

    companion object {
        private const val TAG = "ScoresFragment"
    }
}