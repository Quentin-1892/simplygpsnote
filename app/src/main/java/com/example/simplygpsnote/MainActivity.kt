package com.example.simplygpsnote

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplygpsnote.adapter.EntryAdapter
import com.example.simplygpsnote.data.AppDatabase
import com.example.simplygpsnote.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: EntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView Setup
        adapter = EntryAdapter { entry ->
            val intent = Intent(this, EntryDetailActivity::class.java)
            intent.putExtra("entryId", entry.id)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Load data from database
        val dao = AppDatabase.getDatabase(this).entryDao()
        dao.getAllEntries().observe(this) { entries ->
            adapter.submitList(entries)
        }

        // Floating Action Button für neue Einträge
        binding.fabAddEntry.setOnClickListener {
            startActivity(Intent(this, AddEditEntryActivity::class.java))
        }
    }
}