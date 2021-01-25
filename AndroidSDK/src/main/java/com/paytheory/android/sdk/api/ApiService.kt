package com.paytheory.android.sdk.api

import IdempotencyApiService
import PaymentApiService
import com.paytheory.android.sdk.Constants
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

object ApiService {

    fun challengeApiCall() = Retrofit.Builder()
        .baseUrl(Constants.API_PUBLIC_BASE_PATH)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ApiWorker.gsonConverter)
        .client(ApiWorker.client)
        .build()
        .create(ChallengeApiService::class.java)

    fun idempotencyApiCall() = Retrofit.Builder()
        .baseUrl(Constants.API_BASE_PATH)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ApiWorker.gsonConverter)
        .client(ApiWorker.client)
        .build()
        .create(IdempotencyApiService::class.java)

    fun paymentApiCall() = Retrofit.Builder()
        .baseUrl(Constants.API_BASE_PATH)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ApiWorker.gsonConverter)
        .client(ApiWorker.client)
        .build()
        .create(PaymentApiService::class.java)
}