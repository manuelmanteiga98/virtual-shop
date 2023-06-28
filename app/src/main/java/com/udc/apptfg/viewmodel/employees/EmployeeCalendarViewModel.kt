package com.udc.apptfg.viewmodel.employees

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.udc.apptfg.model.ErrorModel
import com.udc.apptfg.model.employees.AssignmentModel

class EmployeeCalendarViewModel: ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var assignmentsArray = ArrayList<AssignmentModel>()
    val assignments = MutableLiveData<ArrayList<AssignmentModel>>()
    val error = MutableLiveData<ErrorModel>()

    fun getAll(id:String, date:String){
        if(auth.currentUser!=null){
            db.collection("users").document(id)
                .collection("calendar").document(date)
                .collection("assignments").get().addOnSuccessListener { assignmentsRes->
                    assignmentsArray = ArrayList()
                    for (assignment in assignmentsRes){
                        assignmentsArray.add(
                            AssignmentModel(
                                assignment.id,
                                assignment.getString("entry").toString(),
                                assignment.getString("exit").toString(),
                                assignment.getString("details").toString()
                            )
                        )
                    }
                    assignments.postValue(assignmentsArray)
                }
        }
    }

    fun addAssignment(id:String, date:String, entry:String, exit:String, details:String?){
        if(auth.currentUser!=null){
            val userRef =  db.collection("users").document(id)
            userRef.collection("calendar").document(date)
                .collection("assignments").add(
                    hashMapOf(
                        "entry" to entry,
                        "exit" to exit,
                        "details" to details.toString()
                    )
                ).addOnSuccessListener {
                    val notification = hashMapOf(
                        "subject" to "new_assignment",
                        "content" to date,
                        "read" to false
                    )
                    userRef.collection("notifications").add(notification)
                }
        } else{
            error.postValue(ErrorModel())
        }
    }

    fun deleteAssignment(idUser:String, date:String, idAssignment:String){
        if(auth.currentUser!=null){
            db.collection("users").document(idUser)
                .collection("calendar").document(date)
                .collection("assignments").document(idAssignment).delete().addOnSuccessListener {
                    val assignment = assignmentsArray.find{assignment -> assignment.id == idAssignment}
                    assignmentsArray.remove(assignment)
                    assignments.postValue(assignmentsArray)
                }

        } else{
            error.postValue(ErrorModel())
        }
    }

}