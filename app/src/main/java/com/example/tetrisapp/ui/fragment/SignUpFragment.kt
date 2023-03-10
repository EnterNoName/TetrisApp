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
import com.example.tetrisapp.databinding.FragmentSignupBinding
import com.example.tetrisapp.ui.activity.MainActivity
import com.example.tetrisapp.util.OnTouchListener
import com.google.android.gms.auth.api.identity.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding
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
                                Log.d(TAG, "signUpWithCredential:success")
                                val user = firebaseAuth.currentUser
                                if (user != null && isInSignUpFragment) {
                                    findNavController(binding.root).navigate(R.id.action_signUpFragment_to_profileFragment)
                                }
                            } else {
                                Log.w(TAG, "signUpWithCredential:failure", task.exception)
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
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
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
        binding.btnSignUp.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            startLoading()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val passwordConfirm = binding.etPasswordConfirm.text.toString()
            val username = binding.etUsername.text.toString()
            if (password == passwordConfirm) {
                resetFields()
                if (checkInputs(email, password, username)) {
                    createAccount(email, password, username)
                } else {
                    stopLoading()
                }
            } else {
                binding.etPassword.setText("")
                binding.etPasswordConfirm.setText("")
                Snackbar.make(
                    binding.root,
                    "Password and confirm password fields don't match.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
        binding.btnSignUpGoogle.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
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
                    Log.d(TAG, e.localizedMessage ?: "")
                }
        })
        binding.btnSwitchToSignIn.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            findNavController(binding.root).navigate(R.id.action_signUpFragment_to_signInFragment)
        })
        binding.btnBack.setOnClickListener(OnTouchListener((requireActivity() as MainActivity)) {
            findNavController(binding.root).popBackStack()
        })
    }

    private fun checkInputs(email: String, password: String, username: String): Boolean {
        val emailRegex =  Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
        val usernameRegex =  Regex("^\\w{3,16}$")
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
        if (!usernameRegex.matches(username)) {
            if (username.length < 3) {
                Snackbar.make(
                    binding.root,
                    "Username has to be at least 3 characters long.",
                    Snackbar.LENGTH_LONG
                ).show()
            } else if (username.length > 16) {
                Snackbar.make(
                    binding.root,
                    "Username cannot be longer than 16 characters",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(
                    binding.root,
                    "Username can only contain characters, numbers and underscores.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            return false
        }
        return true
    }

    private val isInSignUpFragment: Boolean
        get() = findNavController(binding.root).currentDestination === findNavController(
            binding.root
        ).findDestination(R.id.signUpFragment)

    private fun startLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.btnSignUp.isEnabled = false
            binding.btnSignUpGoogle.isEnabled = false
            binding.btnSwitchToSignIn.isEnabled = false
            binding.etUsername.isEnabled = false
            binding.etEmail.isEnabled = false
            binding.etPassword.isEnabled = false
            binding.etPasswordConfirm.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun stopLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.btnSignUp.isEnabled = true
            binding.btnSignUpGoogle.isEnabled = true
            binding.btnSwitchToSignIn.isEnabled = true
            binding.etUsername.isEnabled = true
            binding.etEmail.isEnabled = true
            binding.etPassword.isEnabled = true
            binding.etPasswordConfirm.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun resetFields() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.etUsername.setText("")
            binding.etEmail.setText("")
            binding.etPassword.setText("")
            binding.etPasswordConfirm.setText("")
        }
    }

    private fun createAccount(email: String, password: String, username: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        user.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build()
                        )
                        if (isInSignUpFragment) {
                            findNavController(binding.root).navigate(R.id.action_signUpFragment_to_profileFragment)
                        }
                    }
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Snackbar.make(
                        binding.root,
                        "Something went wrong.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                stopLoading()
            }
    }

    companion object {
        private const val TAG = "SignInFragment"
    }
}