package com.udc.apptfg.model.sales

import android.graphics.Bitmap

data class ItemSaleModel(
    val id: String,
    var units: Int,
    var price: Double,
    var name: String? = null,
    var image: Bitmap? = null
)