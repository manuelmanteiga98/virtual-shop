package com.udc.apptfg.model.sales

data class SaleModel(
    val id: String,
    val date: String,
    val completed: Boolean,
    val amount: Double
)