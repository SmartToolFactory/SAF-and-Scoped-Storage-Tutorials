package com.example.storageaccessframework.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.preference.PreferenceManager
import android.widget.Toast
import com.example.storageaccessframework.KEY_CURRENT_FOLDER
import java.io.File


internal fun saveCurrentFolderToPrefs(
    currentFolder: String,
    context: Context,
    onCurrentFolderSave: (String) -> Unit
) {

    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val edit: SharedPreferences.Editor = sharedPreferences.edit()
    edit.putString(
        KEY_CURRENT_FOLDER, currentFolder
    )
    edit.apply()

    Toast.makeText(
        context, "Current Folder=> $currentFolder", Toast.LENGTH_SHORT
    ).show()

    onCurrentFolderSave(currentFolder)
}

internal fun getCurrentFolder(context: Context): String {

    val mediaStorageDir = File(Environment.getExternalStorageDirectory(), "")
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // currentFolder should contain full URI: content://authority/path/id

    return sharedPreferences.getString(
        KEY_CURRENT_FOLDER, mediaStorageDir.absolutePath
    )!!
}