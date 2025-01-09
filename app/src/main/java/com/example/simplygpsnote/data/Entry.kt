package com.example.simplygpsnote.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageUri: String? = null,
    val isFavorite: Boolean = false
)