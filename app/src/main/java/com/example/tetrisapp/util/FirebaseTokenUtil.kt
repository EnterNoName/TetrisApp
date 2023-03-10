package com.example.tetrisapp.util

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult

object FirebaseTokenUtil {
    fun getFirebaseToken(tokenListener: (token: String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            tokenListener(null)
            return
        }
        user.getIdToken(true)
            .addOnCompleteListener { task: Task<GetTokenResult> ->
                if (task.isSuccessful) {
                    tokenListener(task.result.token)
                } else {
                    tokenListener(null)
                }
            }
    }
}