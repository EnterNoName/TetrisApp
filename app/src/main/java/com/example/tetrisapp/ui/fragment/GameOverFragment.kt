package com.example.tetrisapp.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.example.tetrisapp.R
import com.example.tetrisapp.data.local.dao.LeaderboardDao
import com.example.tetrisapp.databinding.FragmentGameOverBinding
import com.example.tetrisapp.model.local.LeaderboardEntry
import com.example.tetrisapp.util.FirebaseTokenUtil
import com.example.tetrisapp.util.LeaderboardUtil
import com.example.tetrisapp.util.OnTouchListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
open class GameOverFragment : Fragment() {
    protected lateinit var binding: FragmentGameOverBinding
    private lateinit var args: GameOverFragmentArgs

    @Inject lateinit var leaderboardDao: LeaderboardDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController(binding.root).popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameOverBinding.inflate(inflater, container, false)
        args = GameOverFragmentArgs.fromBundle(requireArguments())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
        initClickListeners()
    }

    @SuppressLint("SetTextI18n")
    protected open fun updateUI() {
        binding.score.text = args.data.score.toString()
        binding.level.text = args.data.level.toString()
        binding.lines.text = args.data.lines.toString()
        lifecycleScope.launch(Dispatchers.IO) {
            leaderboardDao.getBest().let {
                val currentHighScore = it?.score ?: 0
                binding.tvHighScore.text =
                    if (currentHighScore >= args.data.score)
                        getString(R.string.current_high_score).format(currentHighScore)
                    else
                        getString(R.string.new_high_score).format(args.data.score)

                if (args.data.score > 0) {
                    insertScoreInDB(args.data.score, args.data.level, args.data.lines, args.data.timer)
                }
            }
        }
    }

    protected fun insertScoreInDB(score: Int, level: Int, lines: Int, timeInGame: Int) {
        val entry = LeaderboardEntry()
        entry.score = score
        entry.level = level
        entry.lines = lines
        entry.date = Date()
        entry.timeInGame = timeInGame
        FirebaseTokenUtil.getFirebaseToken { token: String? ->
            val leaderboardUtil = LeaderboardUtil(token, getString(R.string.api_url), leaderboardDao)
            leaderboardUtil.insert(entry)
        }
    }

    protected open fun initClickListeners() {
        binding.btnLeave.setOnClickListener(OnTouchListener(requireActivity()) {
            findNavController(binding.root).navigate(R.id.action_gameOverFragment_to_mainMenuFragment)
        })
        binding.btnRetry.setOnClickListener(OnTouchListener(requireActivity()) {
            findNavController(binding.root).navigate(R.id.action_gameOverFragment_to_gameFragment)
        })
        binding.btnShare.setOnClickListener(OnTouchListener(requireActivity()) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
            shareIntent.putExtra(
                Intent.EXTRA_TEXT, String.format(
                    getString(R.string.share_text),
                    args.data.score
                )
            )
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_using)))
        })
    }

    companion object {
        const val TAG = "GameOverFragment"
    }
}