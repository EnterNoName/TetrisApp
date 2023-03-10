package com.example.tetrisapp.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.navigation.Navigation.findNavController
import com.example.tetrisapp.R
import com.example.tetrisapp.databinding.FragmentLeaderboardBinding
import com.example.tetrisapp.model.remote.response.DefaultPayload
import com.example.tetrisapp.model.remote.response.LeaderboardData
import com.example.tetrisapp.model.remote.response.PublicRecord
import com.example.tetrisapp.ui.adapters.ScoresRecyclerViewAdapter
import com.example.tetrisapp.ui.viewmodel.LeaderboardViewModel
import com.example.tetrisapp.util.FirebaseTokenUtil
import com.example.tetrisapp.util.OnTouchListener
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.*
import java.util.*

class LeaderboardFragment : Fragment() {
    private lateinit var binding: FragmentLeaderboardBinding
    private val viewModel by viewModels<LeaderboardViewModel>()

    private var call: HttpResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.scores.observe(viewLifecycleOwner) {
            binding.list.adapter = ScoresRecyclerViewAdapter(requireContext(), it)
            binding.list.adapter?.notifyDataSetChanged()
        }

        FirebaseTokenUtil.getFirebaseToken { token ->
            viewModel.token = token
            sendRequest()
        }

        startLoading()
        initOnClickListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        call?.cancel()
    }

    private fun startLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.btnNextPage.isEnabled = false
            binding.btnPrevPage.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun finishLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.tvPage.text = "Page: ${viewModel.page} of ${viewModel.pageCount}"
            binding.btnNextPage.isEnabled = viewModel.page < viewModel.pageCount
            binding.btnPrevPage.isEnabled = viewModel.page > 1
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun initOnClickListeners() {
        binding.btnBack.setOnClickListener(OnTouchListener(requireActivity()) {
            findNavController(binding.root).popBackStack()
        })
        binding.btnPrevPage.setOnClickListener(OnTouchListener(requireActivity()) {
            if (viewModel.page <= 1) return@OnTouchListener
            viewModel.page -= 1
            sendRequest()
        })
        binding.btnNextPage.setOnClickListener(OnTouchListener(requireActivity()) {
            if (viewModel.page >= viewModel.pageCount) return@OnTouchListener
            viewModel.page += 1
            sendRequest()
        })
    }

    private fun sendRequest() {
        startLoading()

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
            Log.e(TAG, t.localizedMessage ?: "")
            finishLoading()
        }

        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            HttpClient() {
                install(ContentNegotiation) {
                    gson()
                }
            }.use { client ->
                call = client.post(getString(R.string.api_url) + "leaderboard/get") {
                    setBody(
                        mapOf(
                            "idToken" to viewModel.token,
                            "page" to viewModel.page,
                            "limit" to LIMIT
                        )
                    )
                    contentType(ContentType.Application.Json)
                }
                call?.let { response ->
                    val body: DefaultPayload<LeaderboardData> = response.body()

                    if (response.status.isSuccess() && body.success) {
                        withContext(Dispatchers.Main) {
                            viewModel.page = body.data!!.currentPage
                            viewModel.pageCount = body.data!!.pageCount
                            viewModel.updateScores(body.data!!.data.sortedBy { -it.score })
                            finishLoading()
                        }
                    } else {
                        throw(Exception(body.message))
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "LeaderboardFragment"
        private const val LIMIT = 25
    }
}