package com.example.avatarbatchsetter.data

/**
 * 通讯录联系人模型。
 */
data class Contact(
    val id: Long,
    val name: String,
    val hasPhone: Boolean,
    val rawContactIds: List<Long>
)
