package id.alian.barcodescanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import id.alian.barcodescanner.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            setupControls()
        }

        val aniSlide: Animation =
            AnimationUtils.loadAnimation(this@MainActivity, R.anim.scanner_animation)

        with(binding) {
            barcodeLine.startAnimation(aniSlide)
        }
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    private fun setupControls() {
        barcodeDetector =
            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(300, 300)
            .setAutoFocusEnabled(true)
            .build()

        with(binding) {
            cameraSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                @SuppressLint("MissingPermission")
                override fun surfaceCreated(holder: SurfaceHolder) {
                    try {
                        cameraSource.start(holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                @SuppressLint("MissingPermission")
                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    heigth: Int,
                ) {
                    try {
                        cameraSource.start(holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                @SuppressLint("MissingPermission")
                override fun surfaceDestroyed(p0: SurfaceHolder) {
                    cameraSource.stop()
                }
            })

            barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
                override fun release() {
                    Toast.makeText(applicationContext,
                        "Scanner has been closed",
                        Toast.LENGTH_SHORT).show()
                }

                override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                    val barcodes = detections.detectedItems
                    if (barcodes.size() == 1) {
                        scannedValue = barcodes.valueAt(0).rawValue
                        runOnUiThread {
                            cameraSource.stop()
                            barcodeLine.clearAnimation()
                            Toast.makeText(applicationContext,
                                scannedValue,
                                Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "value-else", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.stop()
    }
}