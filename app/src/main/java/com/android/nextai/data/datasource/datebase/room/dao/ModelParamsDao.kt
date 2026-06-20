package com.android.nextai.data.datasource.datebase.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.nextai.data.datasource.datebase.room.entity.ModelParamsEntity

@Dao
interface ModelParamsDao {

    /**
     * Save model parameters.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ModelParamsEntity): Long

    /**
     * Query model parameters based on your own primary key ID.
     */
    @Query(
        """
        SELECT * FROM model_params 
        WHERE id = :id
    """
    )
    suspend fun getParamsById(id: String): ModelParamsEntity?

    /**
     * Query model parameters by id list.
     */
    @Query(
        """
        SELECT * FROM model_params 
        WHERE id IN (:ids)
    """
    )
    suspend fun getParamsByIds(ids: List<String>): List<ModelParamsEntity>

}