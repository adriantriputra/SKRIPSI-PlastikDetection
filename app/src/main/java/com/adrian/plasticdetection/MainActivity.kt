package com.adrian.plasticdetection

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.adrian.plasticdetection.databinding.ActivityMainBinding
import com.adrian.plasticdetection.helper.createCustomTempFile
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var currentPhotoPath: String
    private lateinit var hasilKlasifikasi: String

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.layoutKlasifikasi.visibility = View.GONE
        setUpListeners()
        setUI()
    }

    private fun setUI() {
        viewModel.classificationResult.observe(this) { result ->
            if (result != null) {
                val (resultLabel, confidence) = result
                hasilKlasifikasi = resultLabel

                binding.layoutKlasifikasi.visibility = View.VISIBLE
                binding.klasifikasiName.text = resultLabel
                binding.confidenceNum.text = (confidence * 100).toString()
            } else {
                binding.layoutKlasifikasi.visibility = View.GONE
            }
        }
        viewModel.maxPosResult.observe(this) {
            when (it) {
                2, 4, 6 -> {
                    binding.alert.text = getString(R.string.sampah_ini_tidak_dapat_didaur_ulang)
                    binding.alert.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.red
                        )
                    )
                    binding.buttonHowto.visibility = View.GONE
                }

                else -> {
                    binding.alert.text = getString(R.string.sampah_ini_dapat_didaur_ulang)
                    binding.alert.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.green
                        )
                    )
                    binding.buttonHowto.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setUpListeners() {
        binding.buttonKamera.setOnClickListener {
            startUpCamera()
        }

        binding.buttonGaleri.setOnClickListener {
            getImgFromGallery()
        }

        binding.buttonHowto.setOnClickListener {
            goToMengolahPage()
        }
    }

    private fun startUpCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@MainActivity,
                "com.adrian.plasticdetection",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun getImgFromGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun goToMengolahPage() {
        val intent = Intent(this@MainActivity, PengolahanActivity::class.java)
        intent.putExtra(PengolahanActivity.EXTRA_VALUE, hasilKlasifikasi)
        startActivity(intent)
    }

//    private fun classifyImage(image: Bitmap) {
//        val model = Model.newInstance(applicationContext)
//
//        // Creates inputs for reference.
//        val inputFeature0 =
//            TensorBuffer.createFixedSize(intArrayOf(1, imageSize, imageSize, 3), DataType.FLOAT32)
//        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
//        byteBuffer.order(ByteOrder.nativeOrder())
//
//        val intValues = IntArray(image.width * image.height)
//        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
//        var pixel = 0
//        for (i in 0 until imageSize) {
//            for (j in 0 until imageSize) {
//                val value = intValues[pixel++]
//                byteBuffer.putFloat((value shr 16 and 0xFF).toFloat() * (1f / 255))
//                byteBuffer.putFloat((value shr 8 and 0xFF).toFloat() * (1f / 255))
//                byteBuffer.putFloat((value and 0xFF).toFloat() * (1f / 255))
//            }
//        }
//
//        inputFeature0.loadBuffer(byteBuffer)
//
//        // Runs model inference and gets result.
//        val outputs = model.process(inputFeature0)
//        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//        val confidences = outputFeature0.floatArray
//
//        // Iterate thru outputFeature0 to get Maximum Confidence & Maximum index
//        var maxPos = 0
//        var maxConfidence = 0f
//        for (i in confidences.indices) {
//            if (confidences[i] > maxConfidence) {
//                Log.d("Scan", i.toString())
//                maxConfidence = confidences[i]
//                maxPos = i
//            }
//        }
//
//        val classes = arrayOf(
//            "Jenis 2 HDPE",
//            "Jenis 4 LDPE",
//            "Jenis 7 OTHER",
//            "Jenis 1 PET",
//            "Jenis 5 PP",
//            "Jenis 6 PS",
//            "Jenis 3 PVC"
//        )
//        hasilKlasifikasi = classes[maxPos]
//
//        binding.layoutKlasifikasi.visibility = View.VISIBLE
//        binding.klasifikasiName.text = hasilKlasifikasi
//        binding.confidenceNum.text = maxConfidence.toString()
//
//        when (maxPos) {
//            2, 4, 5 -> {
//                binding.alert.text = "SAMPAH INI TIDAK DAPAT DIDAUR ULANG"
//                binding.alert.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
//                binding.buttonHowto.visibility = View.GONE
//            }
//
//            else -> {
//                binding.alert.text = "SAMPAH INI DAPAT DIDAUR ULANG"
//                binding.alert.setTextColor(
//                    ContextCompat.getColor(
//                        applicationContext,
//                        R.color.green
//                    )
//                )
//                binding.buttonHowto.visibility = View.VISIBLE
//            }
//        }
//
//        // Releases model resources if no longer used.
//        model.close()
//    }

    private val launcherIntentCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val myFile = File(currentPhotoPath)

                myFile.let { file ->
                    val img = BitmapFactory.decodeFile(file.path)
                    binding.plasticImage.setImageBitmap(img)

                    val rescaledImg =
                        Bitmap.createScaledBitmap(img, imageSize, imageSize, false)
                    viewModel.classifyImage(applicationContext, rescaledImg)
                }
            }
        }

    private val launcherIntentGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImgUri = result.data?.data
                selectedImgUri?.let { uri ->
                    try {
                        val inputStream = contentResolver.openInputStream(uri)
                        val img = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()

                        binding.plasticImage.setImageBitmap(img)

                        val rescaledImg =
                            Bitmap.createScaledBitmap(img, imageSize, imageSize, false)
                        viewModel.classifyImage(applicationContext, rescaledImg)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    companion object {
        private const val imageSize = 299
    }
}