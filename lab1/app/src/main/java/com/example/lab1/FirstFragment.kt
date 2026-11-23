package com.example.lab1

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.lab1.databinding.FragmentFirstBinding
import com.google.crypto.tink.Aead
import com.google.crypto.tink.integration.android.AndroidKeystore
import java.io.File

class FirstFragment : Fragment() {

    companion object {
        private const val KEY_ALIAS = "file_encryption_tink_key"
        private const val FILE_NAME = "tajne.bin"
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

    private fun getAead(): Aead {
        if (!AndroidKeystore.hasKey(KEY_ALIAS)) {
            AndroidKeystore.generateNewAes256GcmKey(KEY_ALIAS)
        }
        return AndroidKeystore.getAead(KEY_ALIAS)
    }

    private fun saveEncryptedData() {
        try {
            val plaintext = binding.inputEditText.text.toString().toByteArray(Charsets.UTF_8)
            val associatedData = FILE_NAME.toByteArray(Charsets.UTF_8)
            val encryptedData = getAead().encrypt(plaintext, associatedData)
            File(requireContext().filesDir, FILE_NAME).writeBytes(encryptedData)
            Toast.makeText(requireContext(), R.string.encrypted_data_saved, Toast.LENGTH_SHORT).show()
        } catch (exception: Exception) {
            Toast.makeText(requireContext(), R.string.encrypted_data_save_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun loadEncryptedData() {
        try {
            val file = File(requireContext().filesDir, FILE_NAME)
            if (!file.exists()) {
                binding.outputTextView.text = getString(R.string.encrypted_file_not_found)
                return
            }

            val associatedData = FILE_NAME.toByteArray(Charsets.UTF_8)
            val decryptedData = getAead().decrypt(file.readBytes(), associatedData)
            binding.outputTextView.text = decryptedData.toString(Charsets.UTF_8)
        } catch (exception: Exception) {
            binding.outputTextView.text = getString(R.string.encrypted_data_load_error)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
