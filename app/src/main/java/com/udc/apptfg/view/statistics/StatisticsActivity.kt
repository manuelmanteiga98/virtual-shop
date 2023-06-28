package com.udc.apptfg.view.statistics

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityStatisticsBinding
import com.udc.apptfg.viewmodel.statistics.StatisticsViewModel

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private val statisticsViewModel: StatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        this.title = getString(R.string.statistics)

        statisticsViewModel.statistics.observe(this){ stats->
            if(stats.registrationDate!=null){
                binding.statRegistrationDate.text = stats.registrationDate
                binding.registrationDate.visibility = View.VISIBLE
            }
            if(stats.employees!=null){
                binding.statEmployees.text = stats.employees.toString()
                binding.employees.visibility = View.VISIBLE
            }
            if(stats.maxSale!=null){
                binding.statMaxSale.text = stats.maxSale
                binding.maxSale.visibility = View.VISIBLE
            }
            if(stats.minSale!=null){
                binding.statMinSale.text = stats.minSale
                binding.minSale.visibility = View.VISIBLE
            }
            if(stats.maxSalesDay!=null){
                binding.statMaxSalesDay.text = stats.maxSalesDay
                binding.maxSalesDay.visibility = View.VISIBLE
            }
            if(stats.minSalesDay!=null){
                binding.statMinSalesDay.text = stats.minSalesDay
                binding.minSalesDay.visibility = View.VISIBLE
            }
            if(stats.maxCost!=null){
                binding.statMaxCostItem.text = stats.maxCost
                binding.maxCostItem.visibility = View.VISIBLE
            }
            if(stats.minCost!=null){
                binding.statMinCostItem.text = stats.minCost
                binding.minCostItem.visibility = View.VISIBLE
            }
            if(stats.maxPrice!=null){
                binding.statMaxPriceItem.text = stats.maxPrice
                binding.maxPriceItem.visibility = View.VISIBLE
            }
            if(stats.minPrice!=null) {
                binding.statMinPriceItem.text = stats.minPrice
                binding.minPriceItem.visibility = View.VISIBLE
            }
            if(stats.biggestCategory!=null) {
                binding.statBiggestCategory.text = stats.biggestCategory
                binding.biggestCategory.visibility = View.VISIBLE
            }
        }
        statisticsViewModel.getStats()
    }
}