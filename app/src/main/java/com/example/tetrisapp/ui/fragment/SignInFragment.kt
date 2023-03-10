package com.example.tetrisapp.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.example.tetrisapp.R
import com.example.tetrisapp.databinding.FragmentSigninBinding
import com.example.tetrisapp.ui.activity.MainActivity
import com.example.tetrisapp.util.OnTouchListener
import com.google.android.gms.auth.api.identity.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSigninBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var firebaseAuth: FirebaseAuth

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(
                            requireActivity()
                        ) { task: Task<AuthResult?> ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "signInWithCredential:success")
                                val user = firebaseAuth.currentUser
                                if (user != null && isInSignInFragment) {
                                    findNavController(binding.root).navigate(R.id.action_signInFragment_to_profileFragment)
                                }
                            } else {
                                Log.w(TAG, "signInWithCredential:failure", task.exception)
                            }
                            stopLoading()
                        }
                    Log.d(TAG, "Got ID token.")
                }
            } catch (e: ApiException) {
                Log.d(TAG, e.localizedMessage ?: "")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSigninBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnClickListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOnClickListeners() {
        binding.btnSignIn.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            startLoading()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            resetFields()
            if (checkInputs(email, password)) {
                signIn(email, password)
            } else {
                stopLoading()
            }
        })
        binding.btnSignInGoogle.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            startLoading()
            oneTapClient.beginSignIn(signInRequest)
                .addOnCompleteListener(requireActivity()) { result: Task<BeginSignInResult> ->
                    try {
                        activityResultLauncher.launch(
                            IntentSenderRequest.Builder(result.result.pendingIntent.intentSender)
                                .build()
                        )
                    } catch (e: Throwable) {
                        Log.e(TAG, e.localizedMessage ?: "")
                    }
                }
                .addOnFailureListener(requireActivity()) { e: Exception ->
                    Log.e(TAG, e.localizedMessage ?: "")
                }
        })
        binding.btnSwitchToSignUp.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            findNavController(binding.root).navigate(R.id.action_signInFragment_to_signUpFragment)
        })
        binding.btnBack.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            findNavController(binding.root).popBackStack()
        })
    }

    private fun checkInputs(email: String, password: String): Boolean {
        val emailRegex = Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")

        if (!emailRegex.matches(email)) {
            Snackbar.make(
                binding.root,
                "Please enter a valid email address.",
                Snackbar.LENGTH_LONG
            ).show()
            return false
        }
        if (!passwordRegex.matches(password)) {
            if (password.length < 8) {
                Snackbar.make(
                    binding.root,
                    "Password has to be at least 8 characters long.",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(
                    binding.root,
                    "Password has to contain at least one number, letter and special character.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            return false
        }
        return true
    }

    private val isInSignInFragment: Boolean
        get() = findNavController(binding.root).currentDestination === findNavController(
            binding.root
        ).findDestination(R.id.signInFragment)

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = firebaseAuth.currentUser
                    if (user != null && isInSignInFragment) {
                        findNavController(binding.root).navigate(R.id.action_signInFragment_to_profileFragment)
                    }
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Snackbar.make(
                        binding.root,
                        "Incorrect email or password.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                stopLoading()
            }
    }

    private fun startLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.btnSignIn.isEnabled = false
            binding.btnSignInGoogle.isEnabled = false
            binding.btnSwitchToSignUp.isEnabled = false
            binding.etPassword.isEnabled = false
            binding.etEmail.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun stopLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.btnSignIn.isEnabled = true
            binding.btnSignInGoogle.isEnabled = true
            binding.btnSwitchToSignUp.isEnabled = true
            binding.etPassword.isEnabled = true
            binding.etEmail.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun resetFields() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.etEmail.setText("")
            binding.etPassword.setText("")
        }
    }

    companion object {
        private const val TAG = "SignInFragment"
    }
}