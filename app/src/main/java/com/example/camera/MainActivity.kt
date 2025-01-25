package com.example.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camera.ui.CameraPreviewViewModel
import com.example.camera.ui.theme.CameraTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CameraTheme {
                val viewModel = remember { CameraPreviewViewModel() }
                CameraPreviewScreen(viewModel)
            }
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    viewModel: CameraPreviewViewModel,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (cameraPermissionState.status.isGranted) {
        CameraPreviewContent(viewModel)
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentSize()
                .widthIn(max = 480.dp),
        ) {
            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                "Whoops! Looks like we need your camera to work our magic!" +
                        "Don't worry, we just wanna see your pretty face (and maybe some cats).  " +
                        "Grant us permission and let's get this party started!"
            } else {
                "Hi there! We need your camera to work our magic! ✨\n" +
                        "Grant us permission and let's get this party started! \uD83C\uDF89"
            }
            Text(
                text = textToShow,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {cameraPermissionState.launchPermissionRequest()}
            ) {
                Text(
                    text = "Grant Permission"
                )
            }
        }
    }
}


@Composable
private fun CameraPreviewContent(
    viewModel: CameraPreviewViewModel,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    modifier: Modifier = Modifier
    ) {
    val context = LocalContext.current
    val surfaceRequest = viewModel.surfaceRequest.collectAsStateWithLifecycle()

    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToCamera(lifecycleOwner , context)
    }

    surfaceRequest.value?.let { request ->
        val coordinateTransformer = remember { MutableCoordinateTransformer() }
        CameraXViewfinder(
            surfaceRequest = request,
            coordinateTransformer = coordinateTransformer,
            modifier = modifier.pointerInput(Unit) {
                detectTapGestures { tapCoords ->
                    with(coordinateTransformer) {
                        viewModel.tapToFocus(tapCoords.transform())
                    }
                }
            }
        )
    }
}
