//package com.paytheory.android.sdk.sockets
//
//import IdempotencyApiService
//import PaymentApiService
//import com.paytheory.android.sdk.Constants
//import com.paytheory.android.sdk.api.ApiWorker
//import com.paytheory.android.sdk.api.ChallengeApiService
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import retrofit2.Retrofit
//import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
//
//
///**
// * Object that contains functions to create api calls
// */
//object WebsocketService {
//
//    fun connectCall() = Retrofit.Builder()
//        .baseUrl(Constants.SOCKET_BASE_PATH)
//        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//        .addConverterFactory(ApiWorker.gsonConverter)
//        .client(ApiWorker.client)
//        .build()
//        .create(ConnectService::class.java)
//
//    fun challengeCall() = Retrofit.Builder()
//        .baseUrl(Constants.SOCKET_BASE_PATH)
//        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//        .addConverterFactory(ApiWorker.gsonConverter)
//        .client(ApiWorker.client)
//        .build()
//        .create(ChallengeApiService::class.java)
//
//    fun idempotencyCall() = Retrofit.Builder()
//        .baseUrl(Constants.SOCKET_BASE_PATH)
//        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//        .addConverterFactory(ApiWorker.gsonConverter)
//        .client(ApiWorker.client)
//        .build()
//        .create(IdempotencyApiService::class.java)
//
//    fun paymentCall() = Retrofit.Builder()
//        .baseUrl(Constants.SOCKET_BASE_PATH)
//        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//        .addConverterFactory(ApiWorker.gsonConverter)
//        .client(ApiWorker.client)
//        .build()
//        .create(PaymentApiService::class.java)
//
//    fun disconnectCall() = Retrofit.Builder()
//        .baseUrl(Constants.SOCKET_BASE_PATH)
//        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//        .addConverterFactory(ApiWorker.gsonConverter)
//        .client(ApiWorker.client)
//        .build()
//        .create(DisconnectService::class.java)
//}