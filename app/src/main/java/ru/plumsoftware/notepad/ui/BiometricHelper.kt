package ru.plumsoftware.notepad.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberBiometricPrompt(onSuccess: () -> Unit, onFail: () -> Unit): BiometricPromptWrapper {
    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity ?: return BiometricPromptWrapper(null)

    val executor = androidx.core.content.ContextCompat.getMainExecutor(context)

    val biometricPrompt = remember {
        androidx.biometric.BiometricPrompt(activity, executor, object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED) {
                    // Юзер отменил или выбрал "Код", ничего не делаем, остаемся на экране ввода кода
                } else {
                    onFail()
                }
            }
        })
    }

    return remember { BiometricPromptWrapper(biometricPrompt) }
}

class BiometricPromptWrapper(private val prompt: androidx.biometric.BiometricPrompt?) {
    fun authenticate() {
        val info = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Вход в секретную папку")
            .setSubtitle("Используйте Touch ID или Face ID")
            .setNegativeButtonText("Использовать код") // Обязательно для API < 30
            .build()
        prompt?.authenticate(info)
    }
}