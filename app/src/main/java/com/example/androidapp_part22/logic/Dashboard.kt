package com.example.androidapp_part22.logic

import com.example.androidapp_part22.activities.PatientType
import com.example.androidapp_part22.models.Patient
import kotlin.math.min

public  final class Dashboard {
    private val currentDoctorId = "doc123"
    private val allPatients = listOf(
        Patient("P1001", "John Doe", 45, "Male", "doc123"),
        Patient("P1002", "Jane Smith", 32, "Female", "doc456"),
        Patient("P1003", "Michael Johnson", 68, "Male", "doc123"),
        Patient("P1004", "Emily Brown", 29, "Female", "doc789"),
        Patient("P1005", "David Wilson", 55, "Male", "doc123")
    )

    fun getPatients(page: Int, pageSize: Int, type: PatientType): List<Patient> {
        val source = when (type) {
            PatientType.MY_PATIENTS -> allPatients.filter { it.doctorId == currentDoctorId }
            PatientType.ALL_PATIENTS -> allPatients
        }

        val start = (page - 1) * pageSize
        val end = min(start + pageSize, source.size)
        return if (start < source.size) source.subList(start, end) else emptyList()
    }
}