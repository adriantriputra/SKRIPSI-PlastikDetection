package com.adrian.plasticdetection

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adrian.plasticdetection.ml.PlasticDetectionModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainViewModel : ViewModel() {

    private val _classificationResult = MutableLiveData<Pair<String, Float>>()
    val classificationResult: LiveData<Pair<String, Float>> = _classificationResult

    private val _maxPosResult = MutableLiveData<Int>()
    val maxPosResult: LiveData<Int> = _maxPosResult

    fun classifyImage(context: Context, image: Bitmap){
        val model = PlasticDetectionModel.newInstance(context)

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1,
            imageSize,
            imageSize, 3), DataType.FLOAT32)
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(image.width * image.height)
        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
        var pixel = 0
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val value = intValues[pixel++]
                byteBuffer.putFloat((value shr 16 and 0xFF).toFloat() * (1f / 255))
                byteBuffer.putFloat((value shr 8 and 0xFF).toFloat() * (1f / 255))
                byteBuffer.putFloat((value and 0xFF).toFloat() * (1f / 255))
            }
        }

        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val confidences = outputFeature0.floatArray

        // Iterate thru outputFeature0 to get Maximum Confidence & Maximum index
        var maxPos = 0
        var maxConfidence = 0f
        for (i in confidences.indices){
            if (confidences[i] > maxConfidence){
                Log.d("Scan", i.toString())
                maxConfidence = confidences[i]
                maxPos = i
            }
        }

        val classes = arrayOf("Jenis 1 PET", "Jenis 2 HDPE", "Jenis 3 PVC",
            "Jenis 4 LDPE", "Jenis 5 PP", "Jenis 6 PS", "Jenis 7 OTHER")

        val resultLabel = classes[maxPos]
        val confidence = maxConfidence

        _classificationResult.postValue(Pair(resultLabel, confidence))
        _maxPosResult.postValue(maxPos)

        // Releases model resources if no longer used.
        model.close()
    }

    companion object {
        private const val imageSize = 299
    }
}