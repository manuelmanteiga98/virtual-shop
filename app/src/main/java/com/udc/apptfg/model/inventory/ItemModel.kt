package com.udc.apptfg.model.inventory

import android.graphics.Bitmap

data class ItemModel(
    val id: String,
    val name: String,
    val cost: Double,
    val price: Double,
    val category: String,
    val units: Int,
    val unitsLimit: Int?,
    var image: Bitmap? = null
)