package com.paytheory.android.sdk.api

import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory

/**
 * Class that contains functions to create api calls
 * @param _basePath base path for pt token call
 */
class ApiService(_basePath: String) {

    val basePath = _basePath

    /**
     * Function that creates the pt token api call
     */
    fun ptTokenApiCall(): PTTokenApiService = Retrofit.Builder()
        .baseUrl(basePath)
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(ApiWorker.gsonConverter)
        .client(ApiWorker.client)
        .build()
        .create(PTTokenApiService::class.java)
}