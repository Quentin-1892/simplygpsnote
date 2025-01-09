# SimplyGPSNote

SimplyGPSNote is an Android application designed to allow users to create, edit, and manage location-based notes with image attachments. This app provides an intuitive interface for adding descriptions, capturing photos, saving GPS locations, and managing favorite notes.

---

## Features

- **Create and Edit Notes**: Add descriptions, capture or upload images, and save your current location.
- **Favorites Management**: Mark notes as favorites for quick access.
- **Location Tracking**: Automatically capture and save the user's GPS coordinates.
- **View Notes**: Display all saved notes in a list and filter them by favorites.
- **Image Integration**: Attach images to notes by capturing them directly or choosing from the gallery.

---

## Installation

To install SimplyGPSNote, follow these steps:

1. Download the APK file provided.
2. Enable installation from unknown sources in your device settings.
3. Open the APK file to install the app on your Android device.

---

## User Guide

### Adding a New Note
1. Tap on the floating action button in the main screen.
2. Enter a description for your note.
3. (Optional) Capture a photo or select an image from your gallery.
4. The app will automatically retrieve your current GPS coordinates.
5. Tap "Save" to store the note.

### Managing Notes
- **Edit**: Open any note to update its description or image.
- **Delete**: Permanently remove a note using the delete button in the note detail view.
- **Mark as Favorite**: Mark or unmark notes as favorites to categorize them for quick access.

### Viewing Notes
- Navigate to the main screen to view all notes.
- Use the "Favoriten anzeigen" button to toggle between all notes and favorite notes.

---

## Technical Details

### Architecture
- **Database**: The app uses Room for local data storage.
- **UI Components**: Jetpack RecyclerView, ConstraintLayout, and ViewBinding.
- **Image Handling**: Android FileProvider is used for secure URI handling.
- **Permissions**:
  - Fine and Coarse Location for GPS tracking.
  - Camera and Media access for image capturing and selection.

### Codebase
- Written in **Kotlin**.
- Follows the MVVM architecture pattern.
- Dependencies are managed with Gradle.

---

## Troubleshooting

### Common Issues
1. **Unable to load saved images**: Ensure that the app has the necessary permissions for media access.
2. **GPS coordinates not saved**: Verify that location services are enabled on your device.
3. **App crashes**: If repeated crashes occur, reinstall the app or ensure the APK is correctly signed.

---

## Contributing

Contributions are welcome! If you'd like to contribute, please fork the repository and submit a pull request.

---

## License

This project is licensed under the MIT License.
