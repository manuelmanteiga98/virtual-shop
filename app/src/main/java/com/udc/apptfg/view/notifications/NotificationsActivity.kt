package com.udc.apptfg.view.notifications

import android.content.ClipData.Item
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityNotificationsBinding
import com.udc.apptfg.model.notifications.NotificationModel
import com.udc.apptfg.view.notifications.adapter.ItemAdapter
import com.udc.apptfg.viewmodel.notifications.NotificationsViewModel

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private val notificationsViewModel: NotificationsViewModel by viewModels()
    private var currentList = ArrayList<NotificationModel>()
    enum class State {
        READ, UNREAD
    }
    private var state: State = State.UNREAD

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        this.title = getString(R.string.notifications)

        // Recycler view config
        binding.notificationsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notificationsRecyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.notificationsRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        // Notification status changes
        binding.notificationsMenu.setOnItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.notificationsUnread -> {
                    binding.notificationsMenu.menu.getItem(0).isChecked = true
                    state = State.UNREAD
                    getUnread()
                }
                R.id.notificationsRead -> {
                    binding.notificationsMenu.menu.getItem(1).isChecked = true
                    state = State.READ
                    getRead()
                }
            }
            false
        }

        // Notifications observer
        notificationsViewModel.notifications.observe(this){ notificationsList->
            currentList = ArrayList()
            if(state == State.READ){
                for (notification in notificationsList){
                    if(notification.read){
                        currentList.add(notification)
                    }
                }
            } else{
                for (notification in notificationsList){
                    if(!notification.read){
                        currentList.add(notification)
                    }
                }
            }

            formatNotifications()

            binding.notificationsRecyclerView.adapter = ItemAdapter(currentList){id->
                readNotification(id)
            }
        }

        // Initial load
        notificationsViewModel.getAll()
    }

    private fun getRead() {
        currentList = ArrayList()
        for (notification in notificationsViewModel.notifications.value!!){
            if(notification.read){
                currentList.add(notification)
            }
        }
        formatNotifications()
        binding.notificationsRecyclerView.adapter = ItemAdapter(currentList){id->
            readNotification(id)
        }
    }

    private fun getUnread() {
        currentList = ArrayList()
        for (notification in notificationsViewModel.notifications.value!!){
            if(!notification.read){
                currentList.add(notification)
            }
        }
        formatNotifications()
        binding.notificationsRecyclerView.adapter = ItemAdapter(currentList){id->
            readNotification(id)
        }
    }

    private fun formatNotifications(){
        for(notification in currentList){
            when(notification.subject){
                "units_limit" ->{
                    notification.subject = getString(R.string.units_limit)
                    notification.content = getString(
                        R.string.units_limit_advice,
                        notification.content.split(",")[0].toInt(),
                        notification.content.split(",")[1]
                    )
                }
                "new_assignment" ->{
                    notification.subject = getString(R.string.new_assignment_notification)
                    notification.content = getString(R.string.new_assignment_notification_msg, notification.content)
                }
            }
        }
    }

    private fun readNotification(id: String) {
        notificationsViewModel.readNotification(id)
    }
}