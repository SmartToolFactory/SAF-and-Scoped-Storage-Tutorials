package com.example.storageaccessframework.utils

import android.Manifest
import androidx.compose.runtime.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

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

