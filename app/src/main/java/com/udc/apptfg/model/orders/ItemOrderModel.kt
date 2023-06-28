package com.udc.apptfg.model.orders

import android.graphics.Bitmap

data class ItemOrderModel(
    val id: String,
    var units: Int,
    var name: String? = null,
    var image: Bitmap? = null
)