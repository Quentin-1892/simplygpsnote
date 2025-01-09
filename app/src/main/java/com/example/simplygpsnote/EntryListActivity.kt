package com.example.simplygpsnote

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplygpsnote.adapter.EntryAdapter
import com.example.simplygpsnote.data.AppDatabase
import com.example.simplygpsnote.data.Entry
import com.example.simplygpsnote.databinding.ActivityEntryListBinding

class EntryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntryListBinding
    private lateinit var adapter: EntryAdapter

    // Status, ob nur Favoriten angezeigt werden sollen
    private var showFavoritesOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView einrichten
        adapter = EntryAdapter { entry ->
            // Hier können Sie beim Klick auf einen Eintrag Detailansicht öffnen
            val intent = Intent(this, EntryDetailActivity::class.java)
            intent.putExtra("entryId", entry.id)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Button für Favoriten-Filter einrichten
        binding.btnShowFavorites.setOnClickListener {
            showFavoritesOnly = !showFavoritesOnly
            updateEntryList()
        }

        // Initial Daten anzeigen
        updateEntryList()
    }

    /**
     * Aktualisiert die Liste basierend auf dem Favoritenstatus-Filter.
     */
    private fun updateEntryList() {
        val dao = AppDatabase.getDatabase(this).entryDao()
        val entries: LiveData<List<Entry>> = if (showFavoritesOnly) {
            dao.getFavoriteEntries()
        } else {
            dao.getAllEntries()
        }

        entries.observe(this) { entryList ->
            adapter.submitList(entryList) // Daten an den Adapter übergeben
        }

        // Button-Text entsprechend aktualisieren
        binding.btnShowFavorites.text = if (showFavoritesOnly) {
            "Alle anzeigen"
        } else {
            "Favoriten anzeigen"
        }
    }
}