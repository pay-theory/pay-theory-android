package com.paytheory.android.sdk.api

import com.paytheory.android.sdk.Constants
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

/**
 * Object that contains functions to create api calls
 */
object ApiService {

    fun ptTokenApiCall(): PTTokenApiService = Retrofit.Builder()
        .baseUrl(Constants.API_BASE_PATH)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ApiWorker.gsonConverter)
        .client(ApiWorker.client)
        .build()
        .create(PTTokenApiService::class.java)
}