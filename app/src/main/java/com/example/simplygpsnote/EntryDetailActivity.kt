package com.example.simplygpsnote

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.simplygpsnote.data.AppDatabase
import com.example.simplygpsnote.data.Entry
import com.example.simplygpsnote.databinding.ActivityEntryDetailBinding
import kotlinx.coroutines.launch
import java.io.InputStream

class EntryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntryDetailBinding
    private var currentEntry: Entry? = null // Der aktuell geladene Eintrag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Berechtigungen sicherstellen
        requestStoragePermission()

        // Hole die Entry-ID aus dem Intent
        val entryId = intent.getIntExtra("entryId", -1)
        if (entryId == -1) {
            Toast.makeText(this, "Eintrag nicht gefunden!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Lade die Details des Eintrags
        loadEntryDetails(entryId)

        // Klick-Listener für den Speichern-Button
        binding.btnSaveChanges.setOnClickListener { saveChanges() }

        // Klick-Listener für den Löschen-Button
        binding.btnDelete.setOnClickListener { deleteEntry() }

        // Klick-Listener für Favoriten-Status
        binding.btnFavorite.setOnClickListener { toggleFavoriteStatus() }
    }

    /**
     * Lädt die Details des Eintrags aus der Datenbank.
     */
    private fun loadEntryDetails(entryId: Int) {
        val dao = AppDatabase.getDatabase(this).entryDao()

        lifecycleScope.launch {
            try {
                val entry = dao.getEntryById(entryId)
                if (entry != null) {
                    currentEntry = entry
                    binding.editTextDescription.setText(entry.description)
                    binding.textViewLocation.text = "Lat: ${entry.latitude}, Lon: ${entry.longitude}"
                    binding.btnFavorite.text = if (entry.isFavorite) "Entfernen aus Favoriten" else "Zu Favoriten hinzufügen"

                    // Debug-Log für Bild-URI
                    println("DEBUG: Geladene Bild-URI: ${entry.imageUri}")

                    // Lade Bild, falls URI vorhanden
                    if (!entry.imageUri.isNullOrEmpty()) {
                        val imageUri = Uri.parse(entry.imageUri)
                        loadImageFromUri(imageUri)
                        checkSavedUri(imageUri) // Prüft, ob die URI zugänglich ist
                    } else {
                        binding.imageViewPhoto.setImageResource(R.drawable.ic_launcher_background)
                    }
                } else {
                    Toast.makeText(this@EntryDetailActivity, "Eintrag konnte nicht geladen werden!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EntryDetailActivity, "Fehler beim Laden der Details: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    /**
     * Lädt ein Bild von der angegebenen URI und zeigt es in der ImageView an.
     */
    private fun loadImageFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            binding.imageViewPhoto.setImageBitmap(bitmap)
            inputStream?.close()
        } catch (e: Exception) {
            binding.imageViewPhoto.setImageResource(R.drawable.ic_launcher_background)
            Toast.makeText(this, "Fehler beim Laden des Bildes: ${e.message}", Toast.LENGTH_SHORT).show()
            println("DEBUG: Fehler beim Laden des Bildes: ${e.message}")
        }
    }

    /**
     * Prüft, ob die gespeicherte URI korrekt und zugänglich ist.
     */
    private fun checkSavedUri(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.close()
            println("DEBUG: URI ist zugänglich: $uri")
        } catch (e: Exception) {
            println("DEBUG: URI nicht zugänglich: ${e.message}")
            Toast.makeText(this, "Gespeicherte URI ist nicht zugänglich.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Speichert die Änderungen des aktuellen Eintrags.
     */
    private fun saveChanges() {
        val updatedDescription = binding.editTextDescription.text.toString().trim()

        if (updatedDescription.isEmpty()) {
            Toast.makeText(this, "Beschreibung darf nicht leer sein!", Toast.LENGTH_SHORT).show()
            return
        }

        currentEntry?.let {
            val updatedEntry = it.copy(description = updatedDescription)
            val dao = AppDatabase.getDatabase(this).entryDao()

            lifecycleScope.launch {
                try {
                    dao.update(updatedEntry)
                    println("DEBUG: Änderungen gespeichert für ID ${it.id}, Beschreibung: $updatedDescription")
                    Toast.makeText(this@EntryDetailActivity, "Änderungen gespeichert!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@EntryDetailActivity, "Fehler beim Speichern: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Löscht den aktuellen Eintrag aus der Datenbank.
     */
    private fun deleteEntry() {
        currentEntry?.let { entry ->
            val dao = AppDatabase.getDatabase(this).entryDao()

            lifecycleScope.launch {
                try {
                    dao.delete(entry)
                    println("DEBUG: Eintrag gelöscht: ID ${entry.id}")
                    Toast.makeText(this@EntryDetailActivity, "Eintrag gelöscht!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@EntryDetailActivity, "Fehler beim Löschen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(this, "Löschen fehlgeschlagen!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Fragt die erforderlichen Berechtigungen zur Laufzeit ab.
     */
    private fun requestStoragePermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return // Berechtigungen sind bereits erteilt
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Berechtigung zum Lesen von Medien abgelehnt!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Wechselt den Favoritenstatus des aktuellen Eintrags.
     */
    private fun toggleFavoriteStatus() {
        currentEntry?.let { entry ->
            val dao = AppDatabase.getDatabase(this).entryDao()
            lifecycleScope.launch {
                val updatedEntry = entry.copy(isFavorite = !entry.isFavorite)
                dao.update(updatedEntry)
                binding.btnFavorite.text =
                    if (updatedEntry.isFavorite) "Entfernen aus Favoriten" else "Zu Favoriten hinzufügen"
                currentEntry = updatedEntry
                Toast.makeText(
                    this@EntryDetailActivity,
                    if (updatedEntry.isFavorite) "Zu Favoriten hinzugefügt" else "Aus Favoriten entfernt",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}