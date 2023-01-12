package com.example.tetrisapp.util;

import com.example.tetrisapp.interfaces.TokenListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseTokenUtil {
    public static void getFirebaseToken(TokenListener tokenListener){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        user.getIdToken(true)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                        tokenListener.getToken(task.getResult().getToken());
                });
    }
}
