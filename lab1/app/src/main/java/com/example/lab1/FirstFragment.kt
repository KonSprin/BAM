package com.example.lab1

import android.content.ContentValues
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.lab1.databinding.FragmentFirstBinding
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class FirstFragment : Fragment() {

    companion object {
        private const val KEY_ALIAS = "file_encryption_master_key"
        private const val FILE_NAME = "tajne.bin"
        private const val PREFERENCES_NAME = "encrypted_file_preferences"
        private const val FILE_URI_KEY = "encrypted_file_uri"
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

        binding.saveButton.setOnClickListener {
            saveEncryptedData()
        }
        binding.loadButton.setOnClickListener {
            loadEncryptedData()
        }
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
                .build()
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                init(spec)
                generateKey()
            }
        }
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private fun saveEncryptedData() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(requireContext(), R.string.shared_storage_not_supported, Toast.LENGTH_LONG).show()
            return
        }

        try {
            val plaintext = binding.inputEditText.text.toString().toByteArray(Charsets.UTF_8)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.ENCRYPT_MODE, getSecretKey())
            }
            val encryptedData = cipher.doFinal(plaintext)
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, FILE_NAME)
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = requireContext().contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Nie udało się utworzyć pliku")

            try {
                resolver.openOutputStream(uri)?.use { output ->
                    output.write(cipher.iv.size)
                    output.write(cipher.iv)
                    output.write(encryptedData)
                } ?: error("Nie udało się otworzyć pliku do zapisu")
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                encryptedFilePreferences().edit().putString(FILE_URI_KEY, uri.toString()).apply()
            } catch (exception: Exception) {
                resolver.delete(uri, null, null)
                throw exception
            }

            Toast.makeText(requireContext(), R.string.encrypted_data_saved, Toast.LENGTH_SHORT).show()
        } catch (exception: Exception) {
            Toast.makeText(requireContext(), R.string.encrypted_data_save_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun loadEncryptedData() {
        try {
            val fileUri = encryptedFilePreferences().getString(FILE_URI_KEY, null)
            if (fileUri == null) {
                binding.outputTextView.text = getString(R.string.encrypted_file_not_found)
                return
            }

            val decryptedData = requireContext().contentResolver.openInputStream(
                android.net.Uri.parse(fileUri)
            )?.use { input ->
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
                Cipher.getInstance("AES/GCM/NoPadding").apply {
                    init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
                }.doFinal(encryptedData)
            } ?: error("Nie udało się otworzyć pliku do odczytu")
            binding.outputTextView.text = decryptedData.toString(Charsets.UTF_8)
        } catch (exception: Exception) {
            binding.outputTextView.text = getString(R.string.encrypted_data_load_error)
        }
    }

    private fun encryptedFilePreferences() = requireContext().getSharedPreferences(
        PREFERENCES_NAME,
        android.content.Context.MODE_PRIVATE
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
