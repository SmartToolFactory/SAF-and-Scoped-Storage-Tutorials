package com.example.storageaccessframework

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.documentfile.provider.DocumentFile
import com.example.storageaccessframework.fileutils.FileUtils
import com.example.storageaccessframework.ui.theme.StorageAccessFrameworkTutorialsTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.io.File


class MainActivity : ComponentActivity() {

    // Last saved folder
    private var currentFolder = ""

    private val KEY_CURRENT_FOLDER = "PATH"
    private val REQUEST_FOLDER_ACCESS = 1001

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)

        val mediaStorageDir = File(Environment.getExternalStorageDirectory(), "")

        // currentFolder should contain full URI: content://authority/path/id
        currentFolder = sharedPreferences.getString(
            KEY_CURRENT_FOLDER, mediaStorageDir.absolutePath
        )!!

        setContent {
            StorageAccessFrameworkTutorialsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    StorageAccessFrameWorkExample(currentFolder)
                }
            }
        }
    }

    /**
     * Fires an intent to spin up the folder chooser
     */
    fun selectDirectory() {
        // ACTION_OPEN_DOCUMENT_TREE is the intent to choose a folder
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(
            intent, REQUEST_FOLDER_ACCESS
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {

            // selectDirectory() invoked
            if (requestCode == REQUEST_FOLDER_ACCESS) {

                data?.let { intent: Intent ->

                    intent.data?.let { uri: Uri ->

//                         grantUriPermission(
//                             packageName, uri,
//                         Intent.FLAG_GRANT_READ_URI_PERMISSION or
//                         Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                        val takeFlags =
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                        // Check for the freshest data.
                        contentResolver.takePersistableUriPermission(uri, takeFlags)

                        currentFolder = uri.toString()

                        saveCurrentFolderToPrefs()
                    }
                }
            }
        }
    }

    private fun saveCurrentFolderToPrefs() {
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putString(
            KEY_CURRENT_FOLDER, currentFolder
        )
        edit.apply()

        Toast.makeText(
            this@MainActivity, "Current Folder=> $currentFolder", Toast.LENGTH_SHORT
        ).show()
    }
}


@Composable
private fun StorageAccessFrameWorkExample(currentFolder: String) {

    val context = LocalContext.current

    var permissionGranted by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    if (permissionGranted) {
        SAFExample()
    } else {
        RequestFileReadWritePermission(onDenied = {
            Toast.makeText(context, "Read File Permission Denied", Toast.LENGTH_SHORT).show()
        }, onShouldShowRationale = {
            Toast.makeText(
                context,
                "This permission is required for reading files or directories",
                Toast.LENGTH_SHORT
            ).show()
        }, onGranted = {
            permissionGranted = true
        })
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SAFExample() {
    val context = LocalContext.current

    var currentUri by remember {
        mutableStateOf(
            Uri.parse(Environment.getExternalStorageDirectory().absolutePath)
        )
    }

    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->


        if (uri != null) {
            currentUri = uri

//            context.grantUriPermission(
//                context.packageName,
//                uri,
//                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//            )

            val takeFlags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            // Check for the freshest data.
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)

        }
    }

    var showDropdownMenu by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = showDropdownMenu,
        onDismissRequest = { showDropdownMenu = !showDropdownMenu }
    ) {
        DropdownMenuItem(
            text = { Text(text = "Select Directory") },
            onClick = {
                directoryPicker.launch(currentUri)
            }
        )
        DropdownMenuItem(
            text = { Text(text = "Reset Directory") },
            onClick = { /*TODO*/ }
        )
    }


    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(text = "Storage Access Framework")
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                }
            }
        )
        ExternalDirsSample(context)
        ExternalStorageDirectoriesSample(context)
    }
}

@Composable
private fun ExternalDirsSample(context: Context) {

    val stringBuilder = remember {
        StringBuilder()
    }

    // Displays External Directory and SD Cards
    /**
     * ********* EXTERNAL STORAGE CHECK *********
     *
     * Returns absolute paths to application-specific directories on all shared/external
     * storage devices where the application can place persistent files it owns.
     * These files are internal to the application,
     * and not typically visible to the user as media.
     */

    // Displays External Directory and SD Cards
    val dirs: Array<File> = context.getExternalFilesDirs(null)

    for (file: File in dirs) {

        val path =
            file.path.split("/Android".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

        stringBuilder.append(
            "path: $path, File: $file, " + "isDirectory: ${file.isDirectory}\n" + "isFile: ${file.isFile}, " + "canRead: ${file.canRead()}, " + "canWrite: ${file.canWrite()}\n\n"
        )
    }

    Text(text = "Storage getExternalFilesDirs()", color = MaterialTheme.colorScheme.primary)
    Text(text = stringBuilder.toString(), fontSize = 12.sp)
}

@Composable
private fun ExternalStorageDirectoriesSample(context: Context) {

    val stringBuilder = remember {
        StringBuilder()
    }

    val paths: Array<String> = FileUtils.getExternalStorageDirectories(context)

    for (path in paths) {
        stringBuilder.append("Path: $path")
    }

    // ********* EXTERNAL STORAGE CHECK *********
    val mediaStorageDir: File = File(Environment.getExternalStorageDirectory(), "")


    stringBuilder.append(
        "mediaStorageDir: $mediaStorageDir, " +
                "isDirectory: ${mediaStorageDir.isDirectory}\n" +
                "isFile: ${mediaStorageDir.isFile}, " +
                "canRead: ${mediaStorageDir.canRead()}, " +
                "canWrite: ${mediaStorageDir.canWrite()}\n\n"
    )

    /**
     * Don't create File from path if it's Content Uri content://authority/path/id </br>
     * Returns /content:com.android.externalstorage.documents/tree/primary for main
     * memory using SAF
     */
    // Returns Uri with file:///storage/emulated/0 for main device memory 🔥 BAD
    val documentFile = DocumentFile.fromFile(Environment.getExternalStorageDirectory())

    stringBuilder.append(
        "DocumentFile: $documentFile\n" +
                "isDirectory: ${documentFile.isDirectory}, " +
                "isFile: ${documentFile.isFile}, " +
                "canRead: ${documentFile.canRead()}, " +
                "canWrite: ${documentFile.canWrite()}, " +
                "type: ${documentFile.type}, " +
                "uri: ${documentFile.uri}\n" +
                "name: ${documentFile.name}\n"
    )

    Text(text = "getExternalStorage", color = MaterialTheme.colorScheme.primary)
    Text(text = stringBuilder.toString(), fontSize = 12.sp)
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun RequestFileReadWritePermission(
    onDenied: (() -> Unit)? = null,
    onShouldShowRationale: (() -> Unit)? = null,
    onGranted: () -> Unit
) {

    val readPermissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    var requested by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = requested) {
        readPermissionState.launchPermissionRequest()
    }


    when (readPermissionState.status) {

        // If the permission is granted, then start
        PermissionStatus.Granted -> {
            onGranted()
        }

        is PermissionStatus.Denied -> {
            if (readPermissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                onShouldShowRationale?.invoke()
                requested = true
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                if (requested) {
                    onDenied?.invoke()
                }
            }
        }
    }
}


