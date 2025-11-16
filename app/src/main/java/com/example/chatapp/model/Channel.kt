package com.example.newknowledge.realtime_chat.model

data class Channel (
    val id: String = "",
    val name: String,
    val createAt: Long = System.currentTimeMillis()
)