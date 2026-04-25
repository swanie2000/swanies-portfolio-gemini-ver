package com.swanie.portfolio.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Query("SELECT * FROM user_profile ORDER BY id ASC LIMIT 1")
    suspend fun getFirstUser(): UserProfileEntity?

    @Upsert
    suspend fun upsertUserProfile(profile: UserProfileEntity)
}
