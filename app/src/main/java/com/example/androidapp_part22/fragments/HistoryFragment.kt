package com.example.androidapp_part22.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapp_part22.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private lateinit var historyAdapter: HistoryAdapter

    companion object {
        fun newInstance(): HistoryFragment {
            return HistoryFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.historyRecyclerView)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Load history data
        loadHistoryData()
    }

    private fun loadHistoryData() {
        // In a real app, you would fetch this from SharedPreferences, database, or API
        val historyEntries = getDummyHistoryEntries()

        if (historyEntries.isEmpty()) {
            // Show empty state
            recyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.VISIBLE
        } else {
            // Show history list
            recyclerView.visibility = View.VISIBLE
            emptyStateTextView.visibility = View.GONE

            // Set up adapter
            historyAdapter = HistoryAdapter(historyEntries) { historyEntry ->
                // Handle click on history item
                copyToSpeechToText(historyEntry)
            }
            recyclerView.adapter = historyAdapter
        }
    }

    private fun getDummyHistoryEntries(): List<HistoryEntry> {
        // Create dummy data with realistic transcriptions
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        return listOf(
            HistoryEntry(
                "Patient presented with symptoms of seasonal allergy including nasal congestion and itchy eyes.",
                dateFormat.format(Date(System.currentTimeMillis() - 1000 * 60 * 30)) // 30 minutes ago
            ),
            HistoryEntry(
                "Follow-up examination shows improvement in respiratory function after two weeks on the prescribed medication.",
                dateFormat.format(Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2)) // 2 hours ago
            ),
            HistoryEntry(
                "Recommended increasing fluid intake and rest for the next 48 hours. Patient should return if fever persists.",
                dateFormat.format(Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)) // 1 day ago
            ),
            HistoryEntry(
                "Blood pressure readings: 120/80, 118/78, 122/82. Overall within normal range.",
                dateFormat.format(Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2)) // 2 days ago
            ),
            HistoryEntry(
                "Patient reports improved mobility following physical therapy sessions. Recommend continuing exercises at home.",
                dateFormat.format(Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3)) // 3 days ago
            ),
            HistoryEntry(
                "Scheduled follow-up appointment for next month to reassess medication dosage and effectiveness.",
                dateFormat.format(Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 5)) // 5 days ago
            ),
            HistoryEntry(
                "Discussed diet modifications to reduce inflammation. Suggested Mediterranean diet with emphasis on omega-3 rich foods.",
                dateFormat.format(Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7)) // 1 week ago
            )
        )
    }

    private fun copyToSpeechToText(historyEntry: HistoryEntry) {
        // In a real app, you would communicate with SpeechToTextFragment to paste this text
        // For now, just show a toast
        Toast.makeText(
            requireContext(),
            "Text copied to editor: ${historyEntry.text.take(20)}...",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Model class for history entries
    data class HistoryEntry(
        val text: String,
        val timestamp: String
    )

    // Adapter for the RecyclerView
    inner class HistoryAdapter(
        private val entries: List<HistoryEntry>,
        private val onClick: (HistoryEntry) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.historyTextView)
            val timestampView: TextView = itemView.findViewById(R.id.timestampTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = entries[position]
            holder.textView.text = entry.text
            holder.timestampView.text = entry.timestamp

            holder.itemView.setOnClickListener {
                onClick(entry)
            }
        }

        override fun getItemCount(): Int = entries.size
    }
}