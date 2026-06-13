package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "high_scores")
data class HighScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val score: Int,
    val level: Int,
    val gameMode: String, // "SOLO", "CO_OP", "VERSUS"
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 20")
    fun getTopScores(): Flow<List<HighScore>>

    @Insert
    suspend fun insertScore(score: HighScore)

    @Query("DELETE FROM high_scores")
    suspend fun clearAll()
}

@Database(entities = [HighScore::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract val highScoreDao: HighScoreDao
}

class HighScoreRepository(private val dao: HighScoreDao) {
    val topScores: Flow<List<HighScore>> = dao.getTopScores()

    suspend fun saveScore(score: HighScore) {
        dao.insertScore(score)
    }

    suspend fun clearScores() {
        dao.clearAll()
    }
}
