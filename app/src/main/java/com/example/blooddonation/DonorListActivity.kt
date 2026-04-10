package com.example.blooddonation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blooddonation.databinding.ActivityDonorListBinding
import java.util.*

class DonorListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDonorListBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: DonorAdapter

    private val voiceSearchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                val spokenText = results[0]
                binding.etSearchDonor.setText(spokenText)
                filterList(spokenText)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        
        setupToolbar()
        setupRecyclerView()
        loadDonors()
        setupSearch()
    }

    private fun setupToolbar() {
        binding.toolbarDonors.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = DonorAdapter(mutableListOf())
        binding.rvDonors.layoutManager = LinearLayoutManager(this)
        binding.rvDonors.adapter = adapter
    }

    private fun loadDonors() {
        val donors = dbHelper.getDonorsFiltered()
        val processedList = donors.mapIndexed { index, donor ->
            donor.copy(isBestMatch = index == 0 && donor.donationCount > 0)
        }
        adapter.updateData(processedList)
        updateUIState(processedList.isEmpty())
    }

    private fun setupSearch() {
        binding.etSearchDonor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.btnVoiceSearch.setOnClickListener {
            startVoiceRecognition()
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak blood group or location...")
        }
        try {
            voiceSearchLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice search not supported", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterList(query: String) {
        adapter.filter.filter(query)
        binding.rvDonors.postDelayed({
            updateUIState(adapter.itemCount == 0)
        }, 100)
    }

    private fun updateUIState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvDonors.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvDonors.visibility = View.VISIBLE
        }
    }
}
