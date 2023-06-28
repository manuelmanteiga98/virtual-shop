package com.udc.apptfg.viewmodel.notifications

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.udc.apptfg.model.notifications.NotificationModel

class NotificationsViewModel: ViewModel() {

    var notifications = MutableLiveData<ArrayList<NotificationModel>>()
    private var notificationsArray = ArrayList<NotificationModel>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getAll(){
        if(auth.currentUser!=null){
            val email = auth.currentUser!!.email.toString()
            db.collection("users").document(email)
                .collection("notifications").get().addOnSuccessListener { notificationsRes->
                    notificationsArray = ArrayList<NotificationModel>()
                    for (notification in notificationsRes){
                        val notificationModel = NotificationModel(
                            notification.id,
                            notification.getString("subject").toString(),
                            notification.getString("content").toString(),
                            notification.get("read") as Boolean
                        )
                        notificationsArray.add(notificationModel)
                    }
                    notifications.postValue(notificationsArray)
                }
        }
    }

    fun readNotification(id: String) {
        if(auth.currentUser!=null) {
            val email = auth.currentUser!!.email.toString()
            db.collection("users").document(email)
                .collection("notifications").document(id).update("read", true).addOnSuccessListener {
                    notificationsArray.find{notification -> notification.id == id}?.read = true
                    notifications.postValue(notificationsArray)
                }
        }
    }

}