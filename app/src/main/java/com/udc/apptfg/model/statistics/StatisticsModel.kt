package com.udc.apptfg.model.statistics

data class StatisticsModel(
    var registrationDate: String? = null,
    var employees: Int? = null,
    var maxSale: String? = null,
    var minSale: String? = null,
    var maxSalesDay: String? = null,
    var minSalesDay: String? = null,
    var maxCost: String? = null,
    var minCost: String? = null,
    var maxPrice: String? = null,
    var minPrice: String? = null,
    var biggestCategory: String? = null
)