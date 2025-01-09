package com.example.simplygpsnote.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EntryDao {

    // Abfrage aller Einträge
    @Query("SELECT * FROM entries ORDER BY id DESC")
    fun getAllEntries(): LiveData<List<Entry>>

    // Abfrage aller Favoriten
    @Query("SELECT * FROM entries WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteEntries(): LiveData<List<Entry>>

    // Abfrage eines Eintrags anhand der ID
    @Query("SELECT * FROM entries WHERE id = :entryId")
    suspend fun getEntryById(entryId: Int): Entry?

    // Eintrag hinzufügen
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: Entry)

    // Eintrag löschen
    @Delete
    suspend fun delete(entry: Entry)

    // Eintrag aktualisieren
    @Update
    suspend fun update(entry: Entry)

    // Favoritenstatus eines Eintrags aktualisieren (optional, falls benötigt)
    @Query("UPDATE entries SET isFavorite = :isFavorite WHERE id = :entryId")
    suspend fun updateFavoriteStatus(entryId: Int, isFavorite: Boolean)
}