package com.udc.apptfg.model.employees

data class AssignmentModel(
    var id: String,
    var entry: String,
    var exit: String,
    var details: String? = null
)