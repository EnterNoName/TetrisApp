package com.example.tetrisapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.example.tetrisapp.R
import com.example.tetrisapp.data.local.dao.LeaderboardDao
import com.example.tetrisapp.databinding.FragmentProfileBinding
import com.example.tetrisapp.model.remote.response.DefaultPayload
import com.example.tetrisapp.util.FirebaseTokenUtil
import com.example.tetrisapp.util.OnTouchListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null
    private var token: String? = null

    private var call: HttpResponse? = null
    @Inject lateinit var leaderboardDao: LeaderboardDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth!!.currentUser
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (firebaseUser == null) {
            findNavController(binding.root).navigate(R.id.action_profileFragment_to_signUpFragment)
            return
        }
        FirebaseTokenUtil.getFirebaseToken { token ->
            this.token = token
            sendGetCurrencyRequest()
            sendGetPlacementRequest()
        }
        initOnClickListeners()
        updateUI(firebaseUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let { u ->
            binding.tvEmailAddress.text = u.email
            binding.tvUsername.text = u.displayName
            u.photoUrl?.let { photoUri ->
                binding.ivProfileImage.load(photoUri) {
                    placeholder(R.drawable.ic_round_account_circle_24)
                    error(R.drawable.ic_round_account_circle_24)
                    transformations(CircleCropTransformation())
                    crossfade(true)
                    target(onSuccess = {
                        binding.ivProfileImage.imageTintList = null
                        binding.ivProfileImage.setImageDrawable(it)
                    })
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            leaderboardDao.getTimeInGame().let {
                withContext(Dispatchers.Main) {
                    binding.tvPlaytime.text = getString(R.string.time_in_game).format((it ?: 0) / 3600f)
                }
            }

            leaderboardDao.getGamesCount()?.let {
                withContext(Dispatchers.Main) {
                    binding.tvGamesCount.text = getString(R.string.games_played).format(it)
                }
            }
        }
    }

    private fun initOnClickListeners() {
        binding.btnSignOut.setOnClickListener(OnTouchListener(requireActivity()) {
            firebaseAuth!!.signOut()
            findNavController(binding.root).navigate(R.id.action_profileFragment_to_signUpFragment)
        })
        binding.btnStatistics.setOnClickListener(OnTouchListener(requireActivity()) {
            findNavController(binding.root).navigate(R.id.action_profileFragment_to_scoresFragment)
        })
        binding.btnBack.setOnClickListener(OnTouchListener(requireActivity()) {
            findNavController(binding.root).popBackStack()
        })
    }

    private fun sendGetCurrencyRequest() {
        val coroutineExceptionHandler = CoroutineExceptionHandler{_, t ->
            Log.e(TAG, t.localizedMessage ?: "")
        }

        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            HttpClient() {
                install(ContentNegotiation) {
                    gson()
                }
            }.use { client ->
                call = client.post(getString(R.string.api_url) + "currency/get") {
                    setBody(
                        mapOf("idToken" to token)
                    )
                    contentType(ContentType.Application.Json)
                }
                call?.let { response ->
                    val body: DefaultPayload<Int> = response.body()

                    if (response.status.isSuccess() && body.success) {
                        withContext(Dispatchers.Main) {
                            binding.tvCoins.text = body.data.toString()
                        }
                    } else {
                        throw(Exception(body.message))
                    }
                }
            }
        }
    }
    
    private fun sendGetPlacementRequest() {
        val coroutineExceptionHandler = CoroutineExceptionHandler{_, t ->
            Log.e(TAG, t.localizedMessage ?: "")
        }

        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            HttpClient() {
                install(ContentNegotiation) {
                    gson()
                }
            }.use { client ->
                call = client.post(getString(R.string.api_url) + "leaderboard/placement") {
                    setBody(
                        mapOf("idToken" to token)
                    )
                    contentType(ContentType.Application.Json)
                }
                call?.let { response ->
                    val body: DefaultPayload<Int> = response.body()

                    if (response.status.isSuccess() && body.success) {
                        withContext(Dispatchers.Main) {
                            binding.tvPlacement.text = getString(R.string.leaderboard_placement).format(body.data)
                        }
                    } else {
                        throw(Exception(body.message))
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "ProfileFragment"
    }
}