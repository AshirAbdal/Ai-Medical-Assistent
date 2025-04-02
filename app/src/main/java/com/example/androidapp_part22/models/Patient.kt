package com.example.androidapp_part22.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Patient(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val doctorId: String
) : Parcelable