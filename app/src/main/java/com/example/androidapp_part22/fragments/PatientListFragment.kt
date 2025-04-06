package com.example.androidapp_part22.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.androidapp_part22.logic.Dashboard
import com.example.androidapp_part22.adapters.PatientAdapter
import com.example.androidapp_part22.R
import com.example.androidapp_part22.activities.PatientProfileActivity
import com.example.androidapp_part22.activities.PatientType
import com.example.androidapp_part22.activities.SearchListener
import com.example.androidapp_part22.activities.VoiceActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

 public abstract class PatientListFragment : Fragment(), SearchListener {
    protected lateinit var adapter: PatientAdapter
    protected lateinit var dashboardLogic: Dashboard
    protected var currentPage = 1
    protected val pageSize = 5
    protected var isLoading = false
    protected var hasMoreData = true
    abstract val patientType: PatientType

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_patient_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardLogic = Dashboard()

        // FAB Setup
        view.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(requireContext(), VoiceActivity::class.java))
        }

        // Hide FAB for AllPatientsFragment
        if (this is AllPatientsFragment) {
            view.findViewById<FloatingActionButton>(R.id.fabAdd).visibility = View.GONE
        }

        setupListView()
        loadInitialData()
    }

    private fun setupListView() {
        val listView = view?.findViewById<ListView>(R.id.patientListView)
        adapter = PatientAdapter(
            context = requireContext(),
            patients = mutableListOf(),
            onPatientClicked = { patient ->
                val profileIntent =
                    Intent(requireContext(), PatientProfileActivity::class.java).apply {
                        putExtra("SELECTED_PATIENT", patient)
                    }
                startActivity(profileIntent)
            }
        )
        listView?.adapter = adapter

        listView?.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                if (!isLoading && hasMoreData &&
                    (firstVisibleItem + visibleItemCount >= totalItemCount)) {
                    loadMoreData()
                }
            }
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}
        })
    }

    private fun loadInitialData() {
        isLoading = true
        val patients = dashboardLogic.getPatients(currentPage, pageSize, patientType)
        adapter.updatePatients(patients)
        isLoading = false
        currentPage++
    }

    private fun loadMoreData() {
        isLoading = true
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAdded) return@postDelayed // Check if fragment is attached

            val newPatients = dashboardLogic.getPatients(currentPage, pageSize, patientType)

            if (newPatients.isEmpty()) {
                hasMoreData = false
                context?.let { // Safely show Toast
                    Toast.makeText(it, "No more patients", Toast.LENGTH_SHORT).show()
                }
            } else {
                adapter.addPatients(newPatients)
                currentPage++
            }

            view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
            isLoading = false
        }, 1500)
    }

    override fun onResume() {
        super.onResume()
        applyFontSettings()
    }

    // Change from private to internal
    internal fun applyFontSettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val textSize = prefs.getFloat("textSize", 18f)
        adapter.updateTextSize(textSize)
        adapter.notifyDataSetChanged()
    }

    override fun onSearch(query: String) {
        if (::adapter.isInitialized) {
            adapter.filter.filter(query)
        }
    }

    fun refreshPatientList() {
        currentPage = 1
        hasMoreData = true
        adapter.clearPatients()
        loadInitialData()
    }
}