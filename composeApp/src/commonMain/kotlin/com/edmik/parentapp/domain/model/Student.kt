package com.edmik.parentapp.domain.model

data class Student(
    val id: String,
    val name: String,
    val batch: String,
    val photoUrl: String? = null
)
