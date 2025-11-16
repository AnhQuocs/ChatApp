package com.example.chatapp.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.newknowledge.realtime_chat.model.Channel
import com.google.firebase.Firebase
import com.google.firebase.database.database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.collections.forEach
import kotlin.toString

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val firebaseDatabase = Firebase.database
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels = _channels.asStateFlow()

    init {
        getChannels()
    }

    private fun getChannels() {
        Log.d("HomeViewModel", "Home View Model")

        val ref = firebaseDatabase.getReference("channel")
        Log.d("HomeViewModel", "Bắt đầu get channel từ path: ${ref.path}")

        ref.get()
            .addOnSuccessListener { snapshot ->
                Log.d("HomeViewModel", "addOnSuccessListener được gọi")
                val list = mutableListOf<Channel>()
                if (snapshot.exists()) {
                    snapshot.children.forEach { data ->
                        val channel = Channel(data.key!!, data.value.toString())
                        list.add(channel)
                        Log.d("HomeViewModel", "Child key: ${data.key}, value: ${data.value}")
                    }
                    Log.d("HomeViewModel", "Tìm thấy ${list.size} channel: $list")
                } else {
                    Log.d("HomeViewModel", "Không tìm thấy channel nào")
                }

                _channels.value = list
            }
            .addOnFailureListener { error ->
                Log.e("HomeViewModel", "Lỗi khi lấy channel: ${error.message}")
            }
    }


}