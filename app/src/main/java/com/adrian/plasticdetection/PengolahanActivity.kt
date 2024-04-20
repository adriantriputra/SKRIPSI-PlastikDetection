package com.adrian.plasticdetection

import android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.adrian.plasticdetection.data.DisposalStep
import com.adrian.plasticdetection.data.PlasticRecyclingInfo
import com.adrian.plasticdetection.data.RecyclingType
import com.adrian.plasticdetection.databinding.ActivityPengolahanBinding
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader

class PengolahanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPengolahanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengolahanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jsonString = readJsonFromAssets("daur_ulang.json")

        val gson = Gson()
        val plasticRecyclingInfo = gson.fromJson(jsonString, PlasticRecyclingInfo::class.java)

        val recyclingTypes = plasticRecyclingInfo.recyclingTypes
        val disposalSteps = plasticRecyclingInfo.disposalSteps

        setUI(recyclingTypes, disposalSteps)
        setUpListeners()
    }

    private fun setUI(recyclingTypes: List<RecyclingType>, disposalSteps: List<DisposalStep>) {
        val typeName = intent.getStringExtra(EXTRA_VALUE)

        val matchingType = recyclingTypes.find { it.type == typeName }

        matchingType?.let { type ->
            when (type.type) {
                "Jenis 1 PET" -> binding.resinCode.setImageResource(R.drawable.icon_resin_1)
                "Jenis 2 HDPE" -> binding.resinCode.setImageResource(R.drawable.icon_resin_2)
                "Jenis 4 LDPE" -> binding.resinCode.setImageResource(R.drawable.icon_resin_4)
                "Jenis 5 PP" -> binding.resinCode.setImageResource(R.drawable.icon_resin_5)
            }

            binding.plasticDescription.text = type.description
            binding.contentDaurUlang.text = type.howto
        }

        val disposalDescriptions = disposalSteps.joinToString("\n\n") { "${it.step}: ${it.description}" }
        binding.contentCaraMengolah.text = disposalDescriptions

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            binding.contentCaraMengolah.justificationMode = JUSTIFICATION_MODE_INTER_WORD
        }
//        when (intent.getStringExtra(EXTRA_VALUE)) {
//            "Jenis 1 PET" -> {
//                binding.resinCode.setImageResource(R.drawable.icon_resin_1)
//            }
//            "Jenis 2 HDPE" -> binding.resinCode.setImageResource(R.drawable.icon_resin_2)
//            "Jenis 4 LDPE" -> binding.resinCode.setImageResource(R.drawable.icon_resin_4)
//            "Jenis 5 PP" -> binding.resinCode.setImageResource(R.drawable.icon_resin_5)
//        }
    }

    private fun setUpListeners() {
        with(binding) {
            backArrow.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
            titleDaurUlang.setOnClickListener { toggleVisibility(contentDaurUlang, titleDaurUlang) }
            titleCaraMengolah.setOnClickListener {
                toggleVisibility(
                    contentCaraMengolah,
                    titleCaraMengolah
                )
            }
        }
    }

    private fun toggleVisibility(view: View, title: TextView) {
        val isVisible = when (view.visibility) {
            View.VISIBLE -> false
            else -> true
        }
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
        title.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            if (isVisible) R.drawable.icon_chevron_up else R.drawable.icon_chevron_down,
            0
        )
    }

    private fun readJsonFromAssets(fileName: String): String {
        val stringBuilder = StringBuilder()
        try {
            val inputStream = assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }

    companion object {
        const val EXTRA_VALUE = "VALUE"
    }
}
