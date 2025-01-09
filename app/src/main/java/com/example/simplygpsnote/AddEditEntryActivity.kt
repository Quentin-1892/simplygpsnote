package com.example.simplygpsnote

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.simplygpsnote.data.AppDatabase
import com.example.simplygpsnote.data.Entry
import com.example.simplygpsnote.databinding.ActivityAddEditEntryBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddEditEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditEntryBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0
    private var currentPhotoPath: String? = null
    private var selectedImageUri: Uri? = null
    private var isEditMode: Boolean = false
    private var currentEntry: Entry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Prüfen, ob eine entryId übergeben wurde (Bearbeitungsmodus)
        val entryId = intent.getIntExtra("entryId", -1)
        isEditMode = entryId != -1

        if (isEditMode) {
            loadEntryData(entryId)
        } else {
            requestLocationPermission()
        }

        binding.btnCapturePhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.btnChooseImage.setOnClickListener {
            chooseImageFromGallery()
        }

        binding.btnSave.setOnClickListener {
            saveEntry()
        }
    }

    private fun loadEntryData(entryId: Int) {
        val dao = AppDatabase.getDatabase(this).entryDao()

        lifecycleScope.launch {
            val entry = dao.getEntryById(entryId)
            if (entry != null) {
                currentEntry = entry
                binding.editTextDescription.setText(entry.description)
                currentLat = entry.latitude
                currentLon = entry.longitude
                binding.textViewLocation.text = "Lat: $currentLat, Lon: $currentLon"

                // Foto anzeigen
                entry.imageUri?.let { uri ->
                    loadImageFromUri(Uri.parse(uri))
                }
            } else {
                Toast.makeText(this@AddEditEntryActivity, "Eintrag nicht gefunden!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            binding.imageViewPhoto.setImageBitmap(bitmap)
        } catch (e: Exception) {
            binding.imageViewPhoto.setImageResource(R.drawable.ic_launcher_background)
            Toast.makeText(this, "Fehler beim Laden des Bildes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Standortberechtigung abgelehnt!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLon = location.longitude
                binding.textViewLocation.text = "Lat: $currentLat, Lon: $currentLon"
            } else {
                Toast.makeText(this, "Standort konnte nicht ermittelt werden!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, "Fehler beim Erstellen der Bilddatei", Toast.LENGTH_SHORT).show()
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.simplygpsnote.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                grantUriPermission(
                    packageName,
                    photoURI,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                takePictureLauncher.launch(intent)
            }
        } else {
            Toast.makeText(this, "Keine Kamera-App gefunden", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            currentPhotoPath?.let {
                binding.imageViewPhoto.setImageURI(Uri.fromFile(File(it)))
                selectedImageUri = Uri.fromFile(File(it))
                Toast.makeText(this, "Foto erfolgreich aufgenommen", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Fotoaufnahme abgebrochen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun chooseImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryPickerLauncher.launch(intent)
    }

    private val galleryPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.imageViewPhoto.setImageURI(selectedImageUri)
        } else {
            Toast.makeText(this, "Kein Bild ausgewählt", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun saveEntry() {
        val description = binding.editTextDescription.text.toString().trim()

        if (description.isEmpty()) {
            Toast.makeText(this, "Beschreibung darf nicht leer sein!", Toast.LENGTH_SHORT).show()
            return
        }

        val dao = AppDatabase.getDatabase(this).entryDao()
        lifecycleScope.launch {
            if (isEditMode) {
                currentEntry?.let {
                    val updatedEntry = it.copy(
                        description = description,
                        latitude = currentLat,
                        longitude = currentLon,
                        imageUri = selectedImageUri?.toString()
                    )
                    dao.update(updatedEntry)
                    Toast.makeText(this@AddEditEntryActivity, "Eintrag aktualisiert!", Toast.LENGTH_SHORT).show()
                }
            } else {
                val newEntry = Entry(
                    description = description,
                    latitude = currentLat,
                    longitude = currentLon,
                    imageUri = selectedImageUri?.toString()
                )
                dao.insert(newEntry)
                Toast.makeText(this@AddEditEntryActivity, "Eintrag hinzugefügt!", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}