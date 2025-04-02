package com.example.androidapp_part22.fragments


import com.example.androidapp_part22.activities.PatientType

class MyPatientsFragment : PatientListFragment() {
    override val patientType: PatientType = PatientType.MY_PATIENTS
}