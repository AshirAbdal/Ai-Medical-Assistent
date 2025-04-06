package com.example.androidapp_part22.fragments


import android.widget.Toast
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.androidapp_part22.R
import com.example.androidapp_part22.activities.VoiceActivity
import com.example.androidapp_part22.models.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var historyAdapter: HistoryAdapter
    private val historyEntries = mutableListOf<HistoryEntry>()

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
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        // Set up RecyclerView
        setupRecyclerView()

        // Setup Swipe Refresh
        setupSwipeRefresh()

        // Load dummy history data
        loadDummyHistoryData()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = HistoryAdapter(historyEntries) { historyEntry ->
            copyToSpeechToText(historyEntry)
        }
        recyclerView.adapter = historyAdapter
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            loadDummyHistoryData()
        }
    }

    private fun loadDummyHistoryData() {
        // Simulate a refresh
        swipeRefreshLayout.isRefreshing = true

        // Clear previous entries
        historyEntries.clear()

        // Add dummy history entries
        historyEntries.addAll(generateDummyHistoryEntries())

        // Update UI
        activity?.runOnUiThread {
            swipeRefreshLayout.isRefreshing = false
            updateHistoryUI()
        }
    }

    private fun generateDummyHistoryEntries(): List<HistoryEntry> {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val currentTime = System.currentTimeMillis()

        return listOf(
            HistoryEntry(
                "Patient presented with symptoms of seasonal allergy including nasal congestion and itchy eyes.",
                dateFormat.format(Date(currentTime - 1000 * 60 * 30)) // 30 minutes ago
            ),
            HistoryEntry(
                "Follow-up examination shows improvement in respiratory function after two weeks on the prescribed medication.",
                dateFormat.format(Date(currentTime - 1000 * 60 * 60 * 2)) // 2 hours ago
            ),
            HistoryEntry(
                "Patient presented with symptoms of seasonal allergy including nasal congestion and itchy eyes.",
                dateFormat.format(Date(currentTime - 1000 * 60 * 30)) // 30 minutes ago
            ),
            HistoryEntry(
                "Follow-up examination shows improvement in respiratory function after two weeks on the prescribed medication.",
                dateFormat.format(Date(currentTime - 1000 * 60 * 60 * 2)) // 2 hours ago
            ),    HistoryEntry(
                "Patient presented with symptoms of seasonal allergy including nasal congestion and itchy eyes.",
                dateFormat.format(Date(currentTime - 1000 * 60 * 30)) // 30 minutes ago
            ),
            HistoryEntry(
                "Follow-up examination shows improvement in respiratory function after two weeks on the prescribed medication.",
                dateFormat.format(Date(currentTime - 1000 * 60 * 60 * 2)) // 2 hours ago
            ),
            HistoryEntry(
                "Recommended increasing fluid intake and rest for the next 48 hours. Patient should return if fever persists.",
                dateFormat.format(Date(currentTime - 1000 * 60 * 60 * 24)) // 1 day ago
            ),
            HistoryEntry(
                "Blood pressure readings: 120/80, 118/78, 122/82. Overall within normal range.",
                dateFormat.format(Date(currentTime - 1000 * 60 * 60 * 24 * 2)) // 2 days ago
            ),
            HistoryEntry(
                "Patient reports improved mobility following physical therapy sessions. Recommend continuing exercises at home.",
                dateFormat.format(Date(currentTime - 1000 * 60 * 60 * 24 * 3)) // 3 days ago
            )
        )
    }

    private fun updateHistoryUI() {
        if (historyEntries.isEmpty()) {
            showEmptyState()
        } else {
            // Show history list
            recyclerView.visibility = View.VISIBLE
            emptyStateTextView.visibility = View.GONE

            // Notify adapter of data changes
            historyAdapter.notifyDataSetChanged()
        }
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateTextView.visibility = View.VISIBLE
        emptyStateTextView.text = "No history entries found"
    }

    private fun copyToSpeechToText(historyEntry: HistoryEntry) {
        // Find the VoiceActivity
        (activity as? VoiceActivity)?.let { voiceActivity ->
            // Switch to the Speech to Text tab
            voiceActivity.tabLayout.getTabAt(0)?.select()

            // Find the SpeechToTextFragment
            val fragmentManager = parentFragmentManager
            val speechToTextFragment = fragmentManager.fragments
                .firstOrNull { it is SpeechToTextFragment } as? SpeechToTextFragment

            // If fragment exists and has a public method to set text, use it
            if (speechToTextFragment != null) {
                try {
                    // Use reflection to call the method if direct method call fails
                    val method = speechToTextFragment.javaClass.getMethod("setTextFromHistory", String::class.java)
                    method.invoke(speechToTextFragment, historyEntry.text)
                } catch (e: Exception) {
                    // Fallback: show the text directly
                    Toast.makeText(
                        requireContext(),
                        "Copied: ${historyEntry.text.take(100)}...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Unable to copy text. Speech to Text fragment not found.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

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