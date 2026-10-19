package com.glowbird.gbeacon.logic.network

import com.glowbird.gbeacon.logic.model.AdParams
import com.glowbird.gbeacon.logic.model.AdResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AdService {

    @POST("lanch/ad/ad")
    suspend fun getAdData(@Body adParams: AdParams): AdResponse

}