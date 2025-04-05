package com.example.androidapp_part22.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import com.example.androidapp_part22.R
import com.example.androidapp_part22.models.Patient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MedicationsFragment : Fragment() {

    private var patient: Patient? = null
    private lateinit var noMedicationsText: TextView
    private lateinit var medicationsContainer: LinearLayout
    private lateinit var addMedicationFab: FloatingActionButton

    companion object {
        fun newInstance(patient: Patient): MedicationsFragment {
            val fragment = MedicationsFragment()
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
        return inflater.inflate(R.layout.fragment_medications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        noMedicationsText = view.findViewById(R.id.noMedicationsText)
        medicationsContainer = view.findViewById(R.id.medicationsContainer)
        addMedicationFab = view.findViewById(R.id.addMedicationFab)

        // Set up FAB
        addMedicationFab.setOnClickListener {
            Snackbar.make(view, "Adding new medication feature coming soon", Snackbar.LENGTH_SHORT).show()
        }

        // Load medications
        patient?.let { patient ->
            loadMedicationsForPatient(patient)
        }
    }

    private fun loadMedicationsForPatient(patient: Patient) {
        // In a real app, you would fetch medications from a database
        // For demo, we'll create sample medications based on patient ID

        val hasMedications = patient.id.contains("1") || patient.id.contains("3") || patient.id.contains("5")

        if (hasMedications) {
            noMedicationsText.visibility = View.GONE
            medicationsContainer.visibility = View.VISIBLE

            // Clear any existing medications first
            medicationsContainer.removeAllViews()

            // Create and add sample medications
            val medications = getMockMedicationsForPatient(patient)
            for (medication in medications) {
                val medicationCard = createMedicationCard(medication)
                medicationsContainer.addView(medicationCard)
            }
        } else {
            noMedicationsText.visibility = View.VISIBLE
            medicationsContainer.visibility = View.GONE
        }
    }

    private fun createMedicationCard(medication: Medication): View {
        val inflater = LayoutInflater.from(context)
        val cardView = inflater.inflate(R.layout.item_medication, medicationsContainer, false) as CardView

        // Set medication details
        cardView.findViewById<TextView>(R.id.medicationNameText).text = medication.name
        cardView.findViewById<TextView>(R.id.medicationDosageText).text = "Dosage: ${medication.dosage}"
        cardView.findViewById<TextView>(R.id.medicationFrequencyText).text = "Frequency: ${medication.frequency}"
        cardView.findViewById<TextView>(R.id.medicationPurposeText).text = "Purpose: ${medication.purpose}"

        // Add refill button click handler
        val refillButton = cardView.findViewById<Button>(R.id.refillButton)
        refillButton.setOnClickListener {
            Snackbar.make(requireView(), "Prescription refill requested", Snackbar.LENGTH_SHORT).show()
        }

        return cardView
    }

    private fun getMockMedicationsForPatient(patient: Patient): List<Medication> {
        val medications = mutableListOf<Medication>()

        // Generate medications based on patient ID
        when {
            patient.id.contains("1") -> {
                medications.add(
                    Medication(
                        "Amoxicillin",
                        "500mg",
                        "3 times daily for 7 days",
                        "Bacterial infection",
                        "Dr. Smith"
                    )
                )
                medications.add(
                    Medication(
                        "Ibuprofen",
                        "400mg",
                        "Every 6 hours as needed",
                        "Pain and inflammation",
                        "Dr. Smith"
                    )
                )
            }
            patient.id.contains("3") -> {
                medications.add(
                    Medication(
                        "Lisinopril",
                        "20mg",
                        "Once daily",
                        "Hypertension",
                        "Dr. Williams"
                    )
                )
                medications.add(
                    Medication(
                        "Atorvastatin",
                        "40mg",
                        "Once daily at bedtime",
                        "High cholesterol",
                        "Dr. Williams"
                    )
                )
                medications.add(
                    Medication(
                        "Metformin",
                        "500mg",
                        "Twice daily with meals",
                        "Type 2 Diabetes",
                        "Dr. Williams"
                    )
                )
            }
            patient.id.contains("5") -> {
                medications.add(
                    Medication(
                        "Levothyroxine",
                        "88mcg",
                        "Once daily on empty stomach",
                        "Hypothyroidism",
                        "Dr. Garcia"
                    )
                )
                medications.add(
                    Medication(
                        "Ferrous Sulfate",
                        "325mg",
                        "Twice daily with food",
                        "Iron deficiency anemia",
                        "Dr. Garcia"
                    )
                )
            }
            else -> { /* No medications */ }
        }

        return medications
    }

    // Data class for medications
    data class Medication(
        val name: String,
        val dosage: String,
        val frequency: String,
        val purpose: String,
        val prescribedBy: String
    )
}