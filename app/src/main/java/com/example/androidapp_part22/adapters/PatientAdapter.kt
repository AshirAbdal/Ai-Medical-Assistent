package com.example.androidapp_part22.adapters

import android.content.Context
import android.graphics.Typeface
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import com.example.androidapp_part22.models.Patient
import com.example.androidapp_part22.R

public final class PatientAdapter(
    private val context: Context,
    private var patients: MutableList<Patient>,
    private val onPatientClicked: (Patient) -> Unit
) : BaseAdapter(), Filterable {

    private var originalList = patients.toMutableList()
    private var filter: PatientFilter? = null
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private var textSize = prefs.getFloat("textSize", 18f)

    fun updateTextSize(newSize: Float) {
        textSize = newSize
        notifyDataSetChanged()
    }

    fun addPatients(newPatients: List<Patient>) {
        patients.addAll(newPatients)
        originalList.addAll(newPatients)
        notifyDataSetChanged()
    }

    fun updatePatients(newPatients: List<Patient>) {
        patients.clear()
        originalList.clear()
        patients.addAll(newPatients)
        originalList.addAll(newPatients)
        notifyDataSetChanged()
    }

    fun clearPatients() {
        patients.clear()
        originalList.clear()
        notifyDataSetChanged()
    }

    override fun getCount(): Int = patients.size
    override fun getItem(position: Int): Patient = patients[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_patient, parent, false)

        val patient = getItem(position)
        val genderIcon = view.findViewById<ImageView>(R.id.ivGender)

        // Set gender icon
        genderIcon.setImageResource(
            when(patient.gender.lowercase()) {
                "male" -> R.drawable.ic_male
                "female" -> R.drawable.ic_female
                else -> R.drawable.ic_male
            }
        )

        // Apply text size and style
        val nameView = view.findViewById<TextView>(R.id.tvPatientName)
        val ageView = view.findViewById<TextView>(R.id.tvAge)
        val genderView = view.findViewById<TextView>(R.id.tvGender)
        val idView = view.findViewById<TextView>(R.id.tvPatientId)

        nameView.textSize = textSize
        ageView.textSize = textSize - 2
        genderView.textSize = textSize - 2
        idView.textSize = textSize - 2

        // Apply font style
        when (prefs.getString("fontStyle", "Normal")) {
            "Bold" -> {
                nameView.setTypeface(null, Typeface.BOLD)
                ageView.setTypeface(null, Typeface.BOLD)
                genderView.setTypeface(null, Typeface.BOLD)
                idView.setTypeface(null, Typeface.BOLD)
            }
            "Italic" -> {
                nameView.setTypeface(null, Typeface.ITALIC)
                ageView.setTypeface(null, Typeface.ITALIC)
                genderView.setTypeface(null, Typeface.ITALIC)
                idView.setTypeface(null, Typeface.ITALIC)
            }
            else -> {
                nameView.setTypeface(null, Typeface.NORMAL)
                ageView.setTypeface(null, Typeface.NORMAL)
                genderView.setTypeface(null, Typeface.NORMAL)
                idView.setTypeface(null, Typeface.NORMAL)
            }
        }

        // Set patient data
        nameView.text = patient.name
        ageView.text = "Age: ${patient.age}"
        genderView.text = "Gender: ${patient.gender}"
        idView.text = patient.id

        view.setOnClickListener { onPatientClicked(patient) }

        return view
    }

    override fun getFilter(): Filter {
        if (filter == null) filter = PatientFilter()
        return filter as PatientFilter
    }

    inner class PatientFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            val filteredList = mutableListOf<Patient>()

            if (constraint.isNullOrEmpty()) {
                filteredList.addAll(originalList)
            } else {
                val filterPattern = constraint.toString().lowercase().trim()
                if (filterPattern.length >= 3) {
                    for (patient in originalList) {
                        if (patient.name.lowercase().contains(filterPattern)) {
                            filteredList.add(patient)
                        }
                    }
                }
            }

            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            patients.clear()
            patients.addAll(results?.values as? List<Patient> ?: emptyList())
            notifyDataSetChanged()
        }
    }
}