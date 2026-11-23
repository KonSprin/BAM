package com.example.lab1

import android.os.Bundle
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.lab1.databinding.FragmentFirstBinding
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class FirstFragment : Fragment() {

    companion object {
        private const val KEY_ALIAS = "file_encryption_authenticated_key"
        private const val FILE_NAME = "tajne_manual.txt"
        private const val SECRET_TEXT = "To jest zaszyfrowana informacja."
        private const val LOG_TAG = "EncryptedFile"
    }

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveButton.setOnClickListener { saveEncryptedData() }
        binding.loadButton.setOnClickListener { loadEncryptedData() }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_DEVICE_CREDENTIAL
                )
                .build()
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                init(spec)
                generateKey()
            }
        }
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private fun saveEncryptedData() {
        try {
            requireApi30()
            val plaintext = SECRET_TEXT.toByteArray(Charsets.UTF_8)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.ENCRYPT_MODE, getSecretKey())
            }
            authenticate(cipher, getString(R.string.authentication_encrypt_reason)) { authenticatedCipher ->
                val encryptedData = authenticatedCipher.doFinal(plaintext)
                File(requireContext().filesDir, FILE_NAME).outputStream().use { output ->
                    output.write(authenticatedCipher.iv.size)
                    output.write(authenticatedCipher.iv)
                    output.write(encryptedData)
                }
                Log.d(LOG_TAG, "Zaszyfrowane dane zapisano")
            }
        } catch (exception: Exception) {
            Log.e(LOG_TAG, "Nie udało się zaszyfrować i zapisać danych", exception)
        }
    }

    private fun loadEncryptedData() {
        try {
            requireApi30()
            val file = File(requireContext().filesDir, FILE_NAME)
            require(file.exists()) { "Brak zaszyfrowanego pliku" }
            val (iv, encryptedData) = file.inputStream().use { input ->
                val ivSize = input.read()
                require(ivSize in 1..32) { "Nieprawidłowy rozmiar IV" }
                val iv = ByteArray(ivSize)
                var bytesRead = 0
                while (bytesRead < ivSize) {
                    val read = input.read(iv, bytesRead, ivSize - bytesRead)
                    require(read != -1) { "Nie udało się odczytać IV" }
                    bytesRead += read
                }
                val encryptedData = input.readBytes()
                require(encryptedData.isNotEmpty()) { "Brak zaszyfrowanych danych" }
                iv to encryptedData
            }
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
            }
            authenticate(cipher, getString(R.string.authentication_decrypt_reason)) { authenticatedCipher ->
                val decryptedData = authenticatedCipher.doFinal(encryptedData)
                Log.d(LOG_TAG, "Odszyfrowana informacja: ${decryptedData.toString(Charsets.UTF_8)}")
            }
        } catch (exception: Exception) {
            Log.e(LOG_TAG, "Nie udało się odczytać lub odszyfrować danych", exception)
        }
    }

    private fun authenticate(cipher: Cipher, reason: String, onSuccess: (Cipher) -> Unit) {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                try {
                    result.cryptoObject?.cipher?.let(onSuccess)
                        ?: Log.e(LOG_TAG, "Brak odblokowanego obiektu Cipher")
                } catch (exception: Exception) {
                    Log.e(LOG_TAG, "Operacja na uwierzytelnionym kluczu nie powiodła się", exception)
                }
            }

            override fun onAuthenticationError(errorCode: Int, errorMessage: CharSequence) {
                super.onAuthenticationError(errorCode, errorMessage)
                Log.e(LOG_TAG, "Autoryzacja anulowana lub nieudana: $errorMessage")
            }
        })
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.authentication_title))
            .setSubtitle(reason)
            .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun requireApi30() {
        require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "Autoryzacja PIN-em wymaga Androida 11 (API 30) lub nowszego"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
