package com.swanie.portfolio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "",
    val displayName: String = "",
    val email: String = "",
    val loginPassword: String = "",
    val hasAcceptedTOS: Boolean = false,
    val subscriptionTier: Int = 0,
    val languageCode: String = "en",
    val preferredWeightUnit: String = "OZ"
)
