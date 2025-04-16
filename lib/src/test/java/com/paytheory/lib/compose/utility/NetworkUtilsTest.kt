package com.paytheory.lib.compose.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NetworkUtilsTest {
    
    private lateinit var mockContext: Context
    private lateinit var mockConnectivityManager: ConnectivityManager
    private lateinit var mockNetwork: Network
    private lateinit var mockNetworkCapabilities: NetworkCapabilities
    
    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockConnectivityManager = mockk(relaxed = true)
        mockNetwork = mockk(relaxed = true)
        mockNetworkCapabilities = mockk(relaxed = true)
        
        // Set up context to return connectivity manager
        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        
        // Default active network
        every { mockConnectivityManager.activeNetwork } returns mockNetwork
    }
    
    @Test
    fun `isNetworkAvailable returns true with wifi`() {
        // Given network capabilities with WiFi
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns false
        
        // When checking network availability
        val result = NetworkUtils.isNetworkAvailable(mockContext)
        
        // Then result should be true
        assertTrue(result)
    }
    
    @Test
    fun `isNetworkAvailable returns true with cellular`() {
        // Given network capabilities with cellular
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns false
        
        // When checking network availability
        val result = NetworkUtils.isNetworkAvailable(mockContext)
        
        // Then result should be true
        assertTrue(result)
    }
    
    @Test
    fun `isNetworkAvailable returns true with ethernet`() {
        // Given network capabilities with ethernet
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns true
        
        // When checking network availability
        val result = NetworkUtils.isNetworkAvailable(mockContext)
        
        // Then result should be true
        assertTrue(result)
    }
    
    @Test
    fun `isNetworkAvailable returns false with no network`() {
        // Given no active network
        every { mockConnectivityManager.activeNetwork } returns null
        
        // When checking network availability
        val result = NetworkUtils.isNetworkAvailable(mockContext)
        
        // Then result should be false
        assertFalse(result)
    }
    
    @Test
    fun `isNetworkAvailable returns false with no capabilities`() {
        // Given no network capabilities
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns null
        
        // When checking network availability
        val result = NetworkUtils.isNetworkAvailable(mockContext)
        
        // Then result should be false
        assertFalse(result)
    }
    
    @Test
    fun `isNetworkAvailable returns false with no transport capabilities`() {
        // Given network capabilities with no transport
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns false
        
        // When checking network availability
        val result = NetworkUtils.isNetworkAvailable(mockContext)
        
        // Then result should be false
        assertFalse(result)
    }
} 