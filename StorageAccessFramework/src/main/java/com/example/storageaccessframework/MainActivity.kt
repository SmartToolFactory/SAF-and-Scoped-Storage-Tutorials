package com.example.storageaccessframework

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.documentfile.provider.DocumentFile
import com.example.storageaccessframework.utils.FileUtils
import com.example.storageaccessframework.ui.theme.StorageAccessFrameworkTutorialsTheme
import com.example.storageaccessframework.utils.RequestFileReadWritePermission
import com.example.storageaccessframework.utils.getCurrentFolder
import java.io.File

internal const val KEY_CURRENT_FOLDER = "PATH"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StorageAccessFrameworkTutorialsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    StorageAccessFrameWorkExample()
                }
            }
        }
    }
}

@Composable
private fun StorageAccessFrameWorkExample() {

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
            try {
                Uri.parse(getCurrentFolder(context))
            } catch (e: NullPointerException) {
                Toast.makeText(
                    context, "Exception parsing Uri: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Uri.parse(Environment.getExternalStorageDirectory().absolutePath)
            }
        )
    }

    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->

        if (uri != null) {
            currentUri = uri
            val takeFlags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            // Take persistable read and write permissions for next time this Uri is to be used
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        }
    }

    var showDropdownMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                title = {
                    Text(
                        text = "Storage Access Framework"
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showDropdownMenu = true }
                    ) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")

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
                                onClick = {

                                }
                            )
                        }
                    }
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(it)
            ) {
                ExternalDirsSample(context)
                ExternalStorageDirectoriesSample(context)
            }
        }
    )
}

@Composable
private fun ExternalDirsSample(context: Context) {

    val stringBuilder = remember {
        StringBuilder()
    }

    stringBuilder.setLength(0)

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

        val path = file.path
            .split("/Android".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[0]

        stringBuilder.append(
            "path: $path, " +
                    "File: $file, " +
                    "isDirectory: ${file.isDirectory}\n" +
                    "isFile: ${file.isFile}, " +
                    "canRead: ${file.canRead()}, " +
                    "canWrite: ${file.canWrite()}\n\n"
        )
    }

    Text(
        text = "Storage getExternalFilesDirs()",
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
    Text(text = stringBuilder.toString(), fontSize = 12.sp)
}

@Composable
private fun ExternalStorageDirectoriesSample(context: Context) {

    val stringBuilder = remember {
        StringBuilder()
    }

    stringBuilder.setLength(0)

    val paths: Array<String> = FileUtils.getExternalStorageDirectories(context)

    for (path in paths) {
        stringBuilder.append("Path: $path\n")
    }

    // ********* EXTERNAL STORAGE CHECK *********
    val mediaStorageDir = File(Environment.getExternalStorageDirectory(), "")


    stringBuilder.append(
        "getExternalStorageDirectory: $mediaStorageDir, " +
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
        "DocumentFile " +
                "isDirectory: ${documentFile.isDirectory}, " +
                "isFile: ${documentFile.isFile}, " +
                "canRead: ${documentFile.canRead()}, " +
                "canWrite: ${documentFile.canWrite()}, " +
                "type: ${documentFile.type}, " +
                "uri: ${documentFile.uri}\n" +
                "name: ${documentFile.name}\n"
    )

    Text(
        text = "getExternalStorage",
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
    Text(text = stringBuilder.toString(), fontSize = 12.sp)
}
