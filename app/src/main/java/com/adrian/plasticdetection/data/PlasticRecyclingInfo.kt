package com.adrian.plasticdetection.data

import com.google.gson.annotations.SerializedName

data class PlasticRecyclingInfo(
    @SerializedName("daur_ulang") val recyclingTypes: List<RecyclingType>,
    @SerializedName("cara_pembuangan") val disposalSteps: List<DisposalStep>
)

data class RecyclingType(
    @SerializedName("jenis") val type: String,
    @SerializedName("deskripsi") val description: String,
    @SerializedName("cara") val howto: String,
)

data class DisposalStep(
    @SerializedName("langkah") val step: String,
    @SerializedName("deskripsi") val description: String
)

