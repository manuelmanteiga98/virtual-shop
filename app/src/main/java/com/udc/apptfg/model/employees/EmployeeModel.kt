package com.udc.apptfg.model.employees

import android.graphics.Bitmap

data class EmployeeModel(
    var email: String,
    var name: String,
    var lastName: String,
    var photo: Bitmap? = null
)