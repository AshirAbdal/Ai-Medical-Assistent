package com.example.androidapp_part22.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.androidapp_part22.R
import com.example.androidapp_part22.models.JournalEntry
import com.example.androidapp_part22.models.Patient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JournalFragment : Fragment() {

    private var patient: Patient? = null
    private lateinit var patientInfoText: TextView
    private lateinit var newEntryEdit: EditText
    private lateinit var saveEntryButton: Button
    private lateinit var addEntryFab: FloatingActionButton
    private lateinit var newEntryLayout: ViewGroup
    private lateinit var entriesContainer: LinearLayout
    private lateinit var editControls: LinearLayout
    private lateinit var saveEditButton: Button
    private lateinit var cancelEditButton: Button

    // Store journal entries to manage editing
    private val journalEntries = mutableListOf<JournalEntry>()

    // Currently edited entry
    private var currentlyEditedEntry: JournalEntry? = null
    private var currentlyEditedView: View? = null

    companion object {
        fun newInstance(patient: Patient): JournalFragment {
            val fragment = JournalFragment()
            val args = Bundle()
            args.putParcelable("patient", patient)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        patient = arguments?.getParcelable("patient")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        patientInfoText = view.findViewById(R.id.patientInfoText)
        newEntryEdit = view.findViewById(R.id.newEntryEdit)
        saveEntryButton = view.findViewById(R.id.saveEntryButton)
        addEntryFab = view.findViewById(R.id.addEntryFab)
        newEntryLayout = view.findViewById(R.id.newEntryLayout)
        entriesContainer = view.findViewById(R.id.entriesContainer)
        editControls = view.findViewById(R.id.editControls)
        saveEditButton = view.findViewById(R.id.saveEditButton)
        cancelEditButton = view.findViewById(R.id.cancelEditButton)

        // Set patient information - removed as requested

        // Load initial entries
        patient?.let { patient ->
            loadJournalEntries(patient)
        }

        // Initially hide the new entry form and edit controls
        newEntryLayout.visibility = View.GONE
        editControls.visibility = View.GONE

        // Setup FAB click to show the new entry form
        addEntryFab.setOnClickListener {
            // Hide edit controls and any current editing
            cancelActiveEdit()

            newEntryLayout.visibility = View.VISIBLE
            addEntryFab.visibility = View.GONE
            newEntryEdit.requestFocus()
        }

        // Setup new entry save button
        saveEntryButton.setOnClickListener {
            saveNewEntry()
        }

        // Setup edit controls
        saveEditButton.setOnClickListener {
            saveEditedEntry()
        }

        cancelEditButton.setOnClickListener {
            cancelActiveEdit()
        }
    }

    private fun loadJournalEntries(patient: Patient) {
        // Clear any existing entries
        journalEntries.clear()
        entriesContainer.removeAllViews()

        // In a real app, fetch entries from database
        // For demo, create mock entries based on patient ID
        val mockEntries = when {
            patient.id.contains("1") -> getMockJournalEntries(1)
            patient.id.contains("3") -> getMockJournalEntries(3)
            patient.id.contains("5") -> getMockJournalEntries(5)
            else -> emptyList()
        }

        journalEntries.addAll(mockEntries)

        // Display entries in the UI
        displayJournalEntries()
    }

    private fun displayJournalEntries() {
        // Clear container first
        entriesContainer.removeAllViews()

        if (journalEntries.isEmpty()) {
            // Show empty state
            val emptyView = TextView(context).apply {
                text = "No journal entries yet."
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            entriesContainer.addView(emptyView)
        } else {
            // Create a card for each entry
            for (entry in journalEntries) {
                val entryView = createEntryView(entry)
                entriesContainer.addView(entryView)
            }
        }
    }

    private fun createEntryView(entry: JournalEntry): View {
        val inflater = LayoutInflater.from(context)
        val entryView = inflater.inflate(R.layout.item_journal_entry, entriesContainer, false)

        // Setup entry view with data
        val dateText = entryView.findViewById<TextView>(R.id.entryDateText)
        val contentText = entryView.findViewById<TextView>(R.id.entryContentText)
        val editButton = entryView.findViewById<ImageButton>(R.id.editEntryButton)
        val micButton = entryView.findViewById<ImageButton>(R.id.micEntryButton)
        val editText = entryView.findViewById<EditText>(R.id.entryEditText)

        dateText.text = "Date: ${entry.date}"
        contentText.text = entry.content

        // Setup edit button
        editButton.setOnClickListener {
            // Cancel any current editing first
            cancelActiveEdit()

            // Save reference to currently edited entry and view
            currentlyEditedEntry = entry
            currentlyEditedView = entryView

            // Show edit text with current content
            contentText.visibility = View.GONE
            editText.visibility = View.VISIBLE
            editText.setText(entry.content)
            editText.requestFocus()

            // Show edit controls
            editControls.visibility = View.VISIBLE
            addEntryFab.visibility = View.GONE
        }

        // Setup mic button
        micButton.setOnClickListener {
            // Implementation for voice input
            Snackbar.make(requireView(), "Voice input feature coming soon", Snackbar.LENGTH_SHORT).show()
        }

        return entryView
    }

    private fun saveNewEntry() {
        val newEntryContent = newEntryEdit.text.toString().trim()
        if (newEntryContent.isNotEmpty()) {
            // Get current date formatted
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            // Create new entry
            val newEntry = JournalEntry(currentDate, newEntryContent)

            // Add to the list (at the beginning for newest first)
            journalEntries.add(0, newEntry)

            // Update UI
            displayJournalEntries()

            // Clear and hide form
            newEntryEdit.text.clear()
            newEntryLayout.visibility = View.GONE
            addEntryFab.visibility = View.VISIBLE

            Snackbar.make(requireView(), "Journal entry saved", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(requireView(), "Entry cannot be empty", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun saveEditedEntry() {
        currentlyEditedEntry?.let { entry ->
            currentlyEditedView?.let { view ->
                // Get the edited content
                val editText = view.findViewById<EditText>(R.id.entryEditText)
                val contentText = view.findViewById<TextView>(R.id.entryContentText)
                val newContent = editText.text.toString().trim()

                if (newContent.isNotEmpty()) {
                    // Update the entry
                    entry.content = newContent

                    // Update view
                    contentText.text = newContent
                    contentText.visibility = View.VISIBLE
                    editText.visibility = View.GONE

                    // Hide edit controls
                    editControls.visibility = View.GONE
                    addEntryFab.visibility = View.VISIBLE

                    // Clear references
                    currentlyEditedEntry = null
                    currentlyEditedView = null

                    Snackbar.make(requireView(), "Changes saved", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(requireView(), "Entry cannot be empty", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cancelActiveEdit() {
        currentlyEditedView?.let { view ->
            // Reset view state
            view.findViewById<TextView>(R.id.entryContentText).visibility = View.VISIBLE
            view.findViewById<EditText>(R.id.entryEditText).visibility = View.GONE
        }

        // Hide edit controls
        editControls.visibility = View.GONE
        addEntryFab.visibility = View.VISIBLE

        // Clear references
        currentlyEditedEntry = null
        currentlyEditedView = null
    }

    private fun getMockJournalEntries(variant: Int): List<JournalEntry> {
        return when (variant) {
            1 -> listOf(
                JournalEntry(
                    "March 28, 2025",
                    "Patient presented with persistent cough and mild fever (38.1°C). Lungs clear upon auscultation. Prescribed amoxicillin 500mg TID for 7 days. Follow-up scheduled for April 4."
                ),
                JournalEntry(
                    "March 10, 2025",
                    "Routine check-up. Blood pressure 130/85. Patient reports increased stress at work affecting sleep. Discussed sleep hygiene measures and recommended stress reduction techniques."
                ),
                JournalEntry(
                    "February 15, 2025",
                    "Patient came in for annual physical. All vitals normal. Lab work ordered: CBC, CMP, lipid panel, A1C. Results reviewed on Feb 20 - all within normal limits."
                )
            )

            3 -> listOf(
                JournalEntry(
                    "March 30, 2025",
                    "Patient reports worsening joint pain in knees and fingers. Shows signs of early osteoarthritis. Recommended physical therapy and prescribed Naproxen 500mg BID PRN pain."
                ),
                JournalEntry(
                    "February 28, 2025",
                    "Follow-up for hypertension. BP 142/88. Medication adjusted - increased lisinopril to 20mg daily. Encouraged continued diet modifications and daily exercise."
                ),
                JournalEntry(
                    "January 17, 2025",
                    "Patient presented with symptoms of seasonal allergies. Prescribed loratadine 10mg daily. Discussed air purifier for home use."
                )
            )

            else -> listOf(
                JournalEntry(
                    "April 1, 2025",
                    "Patient reports improved energy levels since starting B12 supplements. Iron levels still low - continuing iron supplementation. Schedule follow-up in 3 months."
                ),
                JournalEntry(
                    "March 15, 2025",
                    "Discussed weight management strategies. Patient has lost 4kg since last visit. Encouraged continued progress with current diet and exercise regimen."
                ),
                JournalEntry(
                    "February 5, 2025",
                    "Initial diagnosis of iron-deficiency anemia. Hgb 10.2 g/dL. Started on ferrous sulfate 325mg daily. B12 level also low - started B12 supplementation."
                )
            )
        }
    }


}