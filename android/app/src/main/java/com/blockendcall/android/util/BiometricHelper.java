package com.blockendcall.android.util;

import android.content.Context;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class BiometricHelper {

    public static boolean isBiometricAvailable(Context context) {
        BiometricManager manager = BiometricManager.from(context);
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static void authenticate(FragmentActivity activity, String title, String subtitle,
                                    BiometricPrompt.AuthenticationCallback callback) {
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText("Cancelar")
                .build();
        new BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
                .authenticate(info);
    }
}
