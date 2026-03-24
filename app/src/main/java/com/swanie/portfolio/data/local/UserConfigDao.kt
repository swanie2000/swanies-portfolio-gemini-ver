package com.swanie.portfolio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserConfigDao {
    @Query("SELECT * FROM user_config WHERE id = 1")
    fun getUserConfig(): Flow<UserConfigEntity?>

    @Query("SELECT * FROM user_config WHERE id = 1")
    suspend fun getUserConfigOnce(): UserConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: UserConfigEntity)

    @Update
    suspend fun updateConfig(config: UserConfigEntity)

    @Query("UPDATE user_config SET showWidgetTotal = :show WHERE id = 1")
    suspend fun updateShowWidgetTotal(show: Boolean)

    @Query("UPDATE user_config SET selectedWidgetAssets = :assets WHERE id = 1")
    suspend fun updateSelectedWidgetAssets(assets: String)

    @Query("UPDATE user_config SET widgetBgColor = :color WHERE id = 1")
    suspend fun updateWidgetBgColor(color: String)

    @Query("UPDATE user_config SET widgetBgTextColor = :color WHERE id = 1")
    suspend fun updateWidgetBgTextColor(color: String)

    @Query("UPDATE user_config SET widgetCardColor = :color WHERE id = 1")
    suspend fun updateWidgetCardColor(color: String)

    @Query("UPDATE user_config SET widgetCardTextColor = :color WHERE id = 1")
    suspend fun updateWidgetCardTextColor(color: String)
}
