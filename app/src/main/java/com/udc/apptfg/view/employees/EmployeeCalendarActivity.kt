package com.udc.apptfg.view.employees

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityEmployeeCalendarBinding
import com.udc.apptfg.model.employees.AssignmentModel
import com.udc.apptfg.view.employees.adapter.AssignmentAdapter
import com.udc.apptfg.viewmodel.employees.EmployeeCalendarViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EmployeeCalendarActivity : AppCompatActivity() {

    lateinit var binding: ActivityEmployeeCalendarBinding
    lateinit var idEmployee: String
    private val assignments = ArrayList<AssignmentModel>()
    private val employeeCalendarViewModel: EmployeeCalendarViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("d-M-y", Locale.getDefault())
    private var mgr = false

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEmployeeCalendarBinding.inflate(layoutInflater)
        idEmployee = this.intent.getStringExtra("idEmployee").toString()
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        this.title = getString(R.string.calendar)
        mgr = intent.getBooleanExtra("mgr", false)

        // If its not a manager account
        if(!mgr){
            binding.employeeAddAssignment.visibility = View.GONE

            binding.employeeAssignments.setOnItemClickListener { _, _, i, _ ->
                showAssignmentDetails(i-1)
                false
            }

        } else{
            // Assignments long click listener
            binding.employeeAssignments.setOnItemLongClickListener { _, _, i, _ ->
                assignmentsLongClick(i - 1)
                false
            }

            // Add Listener
            binding.employeeAddAssignment.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(this)
                val dialog = dialogBuilder.create()
                val view = layoutInflater.inflate(R.layout.dialog_add_assignment, null)

                dialog.setView(view)
                view.findViewById<TimePicker>(R.id.employeeEntryHour).setIs24HourView(true)
                view.findViewById<TimePicker>(R.id.employeeExitHour).setIs24HourView(true)
                view.findViewById<Button>(R.id.addAssignmentDialogAdd).setOnClickListener {
                    val date = dateFormat.format(binding.employeeCalendar.date)
                    val entry =
                        view.findViewById<TimePicker>(R.id.employeeEntryHour).hour.toString() +
                                ":" +
                                view.findViewById<TimePicker>(R.id.employeeEntryHour).minute.toString()
                    val exit =
                        view.findViewById<TimePicker>(R.id.employeeExitHour).hour.toString() +
                                ":" +
                                view.findViewById<TimePicker>(R.id.employeeExitHour).minute.toString()
                    employeeCalendarViewModel.addAssignment(
                        idEmployee,
                        date,
                        entry,
                        exit,
                        view.findViewById<EditText>(R.id.employeeAssignmentDetails).text.toString()
                    )
                    employeeCalendarViewModel.getAll(idEmployee, date)
                    dialog.dismiss()
                }

                view.findViewById<Button>(R.id.addAssignmentDialogCancel).setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }
        }

        // Date list config
        val header = TextView(this.baseContext)
        header.text = getString(R.string.assignments)
        header.textAlignment = View.TEXT_ALIGNMENT_CENTER
        binding.employeeAssignments.addHeaderView(header)
        binding.employeeAssignments.adapter = AssignmentAdapter(
            this,
            android.R.layout.simple_list_item_1,
            assignments
        )

        // Assignments observer
        employeeCalendarViewModel.assignments.observe(this) { list ->
            binding.employeeAssignments.adapter = AssignmentAdapter(
                this,
                android.R.layout.simple_list_item_1,
                list
            )
        }


        // Calendar listener
        binding.employeeCalendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val monthNumber = (month + 1).toString()
            binding.employeeCalendar.date = dateFormat.parse("$dayOfMonth-$monthNumber-$year").time
            employeeCalendarViewModel.getAll(idEmployee, "$dayOfMonth-$monthNumber-$year")
        }

        // Initial loading
        employeeCalendarViewModel.getAll(
            idEmployee,
            dateFormat.format(binding.employeeCalendar.date)
        )

    }

    private fun addAssignment() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialog = dialogBuilder.create()
        val view = layoutInflater.inflate(R.layout.dialog_add_assignment, null)

        dialogBuilder.setView(view)
        view.findViewById<TimePicker>(R.id.employeeEntryHour).setIs24HourView(true)
        view.findViewById<TimePicker>(R.id.employeeExitHour).setIs24HourView(true)
        view.findViewById<Button>(R.id.addAssignmentDialogAdd).setOnClickListener {
            val date = dateFormat.format(binding.employeeCalendar.date)
            val entry =
                view.findViewById<TimePicker>(R.id.employeeEntryHour).hour.toString() +
                        ":" +
                        view.findViewById<TimePicker>(R.id.employeeEntryHour).minute.toString()
            val exit =
                view.findViewById<TimePicker>(R.id.employeeExitHour).hour.toString() +
                        ":" +
                        view.findViewById<TimePicker>(R.id.employeeExitHour).minute.toString()
            employeeCalendarViewModel.addAssignment(
                idEmployee,
                date,
                entry,
                exit,
                view.findViewById<EditText>(R.id.employeeAssignmentDetails).text.toString()
            )
            employeeCalendarViewModel.getAll(idEmployee, date)
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.addAssignmentDialogCancel).setOnClickListener {
            dialog.dismiss()
        }
        println("Holaaaa")
        dialog.show()
    }

    private fun assignmentsLongClick(index: Int) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.actions))
        dialog.setItems(
            R.array.assignment_opts,
            DialogInterface.OnClickListener { _, i ->
                if (i == 0) {
                    showAssignmentDetails(index)
                } else {
                    deleteAssignment(index)
                }
            }
        )
        dialog.show()
    }

    private fun showAssignmentDetails(index: Int) {
        val details = employeeCalendarViewModel.assignments.value!![index].details.toString()
        if (details.isNotBlank()) {
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage(details)
            dialog.setPositiveButton(getString(R.string.accept)) { _, _ -> }
            dialog.show()
        }
    }

    private fun deleteAssignment(index: Int) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.warning))
        dialog.setMessage(getString(R.string.delete_assignment_msg))
        dialog.setPositiveButton(getString(R.string.accept)) { _, _ ->
            val assignmentID = employeeCalendarViewModel.assignments.value!![index].id
            val date = dateFormat.format(binding.employeeCalendar.date)
            employeeCalendarViewModel.deleteAssignment(idEmployee, date, assignmentID)
        }
        dialog.setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        dialog.show()
    }

}