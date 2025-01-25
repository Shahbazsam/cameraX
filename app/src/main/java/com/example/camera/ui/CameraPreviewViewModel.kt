package com.example.camera.ui

import android.content.Context
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CameraPreviewViewModel : ViewModel() {

    private var surfaceOrientedMeteringPointFactory : SurfaceOrientedMeteringPointFactory ? = null
    private var cameraControl :CameraControl? = null

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest = _surfaceRequest.asStateFlow()

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { surfaceRequest ->
            _surfaceRequest.update { surfaceRequest }
            surfaceOrientedMeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                surfaceRequest.resolution.width.toFloat(),
                surfaceRequest.resolution.height.toFloat()
            )
        }
    }



   suspend fun bindToCamera(
        lifecycleOwner: LifecycleOwner,
        context : Context
    ) {
        val processCameraProvider = ProcessCameraProvider.awaitInstance(context)

       val camera = processCameraProvider.bindToLifecycle(
           lifecycleOwner , DEFAULT_FRONT_CAMERA , cameraPreviewUseCase
       )
       cameraControl = camera.cameraControl

       try {
           awaitCancellation()
       } finally {
           processCameraProvider.unbindAll()
           cameraControl = null
       }
    }

    fun tapToFocus (tapCoords : Offset) {
        val point = surfaceOrientedMeteringPointFactory?.createPoint(tapCoords.x , tapCoords.y)
        if (point != null) {
            val meteringAction = FocusMeteringAction.Builder(point).build()
            cameraControl?.startFocusAndMetering(meteringAction)
        }
    }


   }