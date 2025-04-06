package com.example.androidapp_part22.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.androidapp_part22.R
import com.example.androidapp_part22.models.Patient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

 public final class ReportsFragment : Fragment() {

    private var patient: Patient? = null
    private lateinit var noReportsView: TextView
    private lateinit var reportsContainer: ViewGroup
    private lateinit var uploadReportButton: FloatingActionButton

    private val mockReports = mutableListOf<Report>()
    private var currentPhotoUri: Uri? = null

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_PICK_IMAGE = 102

        fun newInstance(patient: Patient): ReportsFragment {
            val fragment = ReportsFragment()
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
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        noReportsView = view.findViewById(R.id.noReportsText)
        reportsContainer = view.findViewById(R.id.reportsContainer)
        uploadReportButton = view.findViewById(R.id.uploadReportButton)

        // Set up upload button
        uploadReportButton.setOnClickListener {
            showUploadOptions()
        }

        // Load patient reports
        patient?.let { patient ->
            loadPatientReports(patient)
        }
    }

    private fun loadPatientReports(patient: Patient) {
        // Clear any existing reports
        mockReports.clear()
        reportsContainer.removeAllViews()

        // Create mock reports based on patient ID
        // In a real app, you would fetch these from a database
        if (patient.id.contains("1") || patient.id.contains("3") || patient.id.contains("5")) {
            createMockReports(patient.id)

            // Display reports
            if (mockReports.isNotEmpty()) {
                noReportsView.visibility = View.GONE
                reportsContainer.visibility = View.VISIBLE

                for (report in mockReports) {
                    addReportToView(report)
                }
            } else {
                showNoReports()
            }
        } else {
            showNoReports()
        }
    }

    private fun showNoReports() {
        noReportsView.visibility = View.VISIBLE
        reportsContainer.visibility = View.GONE
    }

    private fun createMockReports(patientId: String) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val today = Date()
        val cal = java.util.Calendar.getInstance()

        if (patientId.contains("1")) {
            mockReports.add(
                Report(
                    "Blood Test Results",
                    "PDF",
                    dateFormat.format(today),
                    "Dr. Smith"
                )
            )

            cal.time = today
            cal.add(java.util.Calendar.DAY_OF_MONTH, -15)
            mockReports.add(
                Report(
                    "Chest X-Ray",
                    "JPG",
                    dateFormat.format(cal.time),
                    "Dr. Johnson"
                )
            )
        } else if (patientId.contains("3")) {
            mockReports.add(
                Report(
                    "Cardiac Evaluation",
                    "PDF",
                    dateFormat.format(today),
                    "Dr. Williams"
                )
            )

            cal.time = today
            cal.add(java.util.Calendar.DAY_OF_MONTH, -7)
            mockReports.add(
                Report(
                    "ECG Results",
                    "JPG",
                    dateFormat.format(cal.time),
                    "Dr. Williams"
                )
            )

            cal.add(java.util.Calendar.DAY_OF_MONTH, -23)
            mockReports.add(
                Report(
                    "Blood Pressure Chart",
                    "PNG",
                    dateFormat.format(cal.time),
                    "Dr. Baker"
                )
            )
        } else if (patientId.contains("5")) {
            mockReports.add(
                Report(
                    "CT Scan Results",
                    "PDF",
                    dateFormat.format(today),
                    "Dr. Garcia"
                )
            )
        }
    }

    private fun addReportToView(report: Report) {
        val inflater = LayoutInflater.from(context)
        val reportView = inflater.inflate(R.layout.item_report, reportsContainer, false) as CardView

        // Set report details
        val titleText = reportView.findViewById<TextView>(R.id.reportTitleText)
        val typeText = reportView.findViewById<TextView>(R.id.reportTypeText)
        val dateText = reportView.findViewById<TextView>(R.id.reportDateText)
        val doctorText = reportView.findViewById<TextView>(R.id.reportDoctorText)

        titleText.text = report.title
        typeText.text = "Type: ${report.type}"
        dateText.text = "Date: ${report.date}"
        doctorText.text = "Doctor: ${report.doctor}"

        // Set click listener to view report
        reportView.setOnClickListener {
            viewFullReport(report)
        }

        reportsContainer.addView(reportView)
    }

    private fun viewFullReport(report: Report) {
        // In a real app, this would open the actual report file
        // For now, we'll just show a dialog with the report details

        AlertDialog.Builder(requireContext())
            .setTitle(report.title)
            .setMessage(
                "Type: ${report.type}\n" +
                        "Date: ${report.date}\n" +
                        "Doctor: ${report.doctor}\n\n" +
                        "This is a placeholder for viewing the full report. In a real app, this would open the actual ${report.type} file."
            )
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showUploadOptions() {
        val options = arrayOf("Take a Picture", "Upload from Gallery")

        AlertDialog.Builder(requireContext())
            .setTitle("Upload Report")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePicture()
                    1 -> pickFromGallery()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(requireActivity().packageManager)?.also {
                // Create a file to save the image
                val photoFile = createImageFile()
                photoFile?.also {
                    currentPhotoUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.androidapp_part22.fileprovider",
                        it
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
            } ?: run {
                Snackbar.make(requireView(), "Camera app not found", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickFromGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { intent ->
            intent.type = "image/* application/pdf"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "application/pdf"))
            startActivityForResult(intent, REQUEST_PICK_IMAGE)
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = requireContext().getExternalFilesDir(null)
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        } catch (ex: Exception) {
            Snackbar.make(requireView(), "Error creating image file", Snackbar.LENGTH_SHORT).show()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    // Handle captured image
                    handleNewReport(currentPhotoUri, "JPG")
                }
                REQUEST_PICK_IMAGE -> {
                    // Handle picked image or pdf
                    val uri = data?.data
                    val mimeType = uri?.let { requireContext().contentResolver.getType(it) }
                    val fileType = when {
                        mimeType?.contains("pdf") == true -> "PDF"
                        mimeType?.contains("image") == true -> {
                            when {
                                mimeType.contains("jpeg") || mimeType.contains("jpg") -> "JPG"
                                mimeType.contains("png") -> "PNG"
                                else -> "Image"
                            }
                        }
                        else -> "Unknown"
                    }

                    handleNewReport(uri, fileType)
                }
            }
        }
    }

    private fun handleNewReport(uri: Uri?, fileType: String) {
        if (uri != null) {
            // In a real app, you would save this uri in a database or upload to server

            // Create a new report object
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val newReport = Report(
                "New ${fileType} Report",
                fileType,
                dateFormat.format(Date()),
                "Dr. " + patient?.name?.split(" ")?.getOrNull(1) ?: "Unknown"
            )

            // Add to our list
            mockReports.add(0, newReport)

            // Update UI
            if (reportsContainer.visibility == View.GONE) {
                noReportsView.visibility = View.GONE
                reportsContainer.visibility = View.VISIBLE
            }

            // Add to view
            addReportToView(newReport)

            Snackbar.make(requireView(), "Report uploaded successfully", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(requireView(), "Failed to upload report", Snackbar.LENGTH_SHORT).show()
        }
    }

    // Model class for patient reports
    data class Report(
        val title: String,
        val type: String,  // PDF, JPG, PNG
        val date: String,
        val doctor: String
    )
}