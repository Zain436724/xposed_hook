package com.example.xposed_hooking_info.network

import com.example.xposed_hooking_info.model.DeviceInfo
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class FakeDeviceApiService {
    private val baseUrl = "https://www.myfakeinfo.com/mobile/get-android-device-information.php"
    private val client: OkHttpClient

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    suspend fun getFakeDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("Failed to fetch fake device info: ${response.code}")
        }

        val responseBody = response.body?.string() ?: ""
        parseHtmlResponse(responseBody)
    }

    private fun parseHtmlResponse(html: String): DeviceInfo {
        // This is a simple HTML parsing approach using regex
        // In a production app, consider using a proper HTML parser library
        val brandPattern = Pattern.compile("Brand\\s*:\\s*<span[^>]*>([^<]+)</span>")
        val modelPattern = Pattern.compile("Model\\s*:\\s*<span[^>]*>([^<]+)</span>")
        val devicePattern = Pattern.compile("Device\\s*:\\s*<span[^>]*>([^<]+)</span>")
        val idPattern = Pattern.compile("ID\\s*:\\s*<span[^>]*>([^<]+)</span>")
        val manufacturePattern = Pattern.compile("Manufacturer\\s*:\\s*<span[^>]*>([^<]+)</span>")
        val productPattern = Pattern.compile("Product\\s*:\\s*<span[^>]*>([^<]+)</span>")
        val fingerprintPattern = Pattern.compile("Fingerprint\\s*:\\s*<span[^>]*>([^<]+)</span>")
        val imeiPattern = Pattern.compile("IMEI\\s*:\\s*<span[^>]*>([^<]+)</span>")

        val brandMatcher = brandPattern.matcher(html)
        val modelMatcher = modelPattern.matcher(html)
        val deviceMatcher = devicePattern.matcher(html)
        val idMatcher = idPattern.matcher(html)
        val manufacturerMatcher = manufacturePattern.matcher(html)
        val productMatcher = productPattern.matcher(html)
        val fingerprintMatcher = fingerprintPattern.matcher(html)
        val imeiMatcher = imeiPattern.matcher(html)

        // Generate random values for properties not provided by the website
        val randomPrefix = (100000..999999).random().toString()

        return DeviceInfo(
            brand = if (brandMatcher.find()) brandMatcher.group(1) else "Generic",
            model = if (modelMatcher.find()) modelMatcher.group(1) else "Generic Model",
            device = if (deviceMatcher.find()) deviceMatcher.group(1) else "generic_device",
            buildId = if (idMatcher.find()) idMatcher.group(1) else "ID$randomPrefix",
            manufacturer = if (manufacturerMatcher.find()) manufacturerMatcher.group(1) else "Generic",
            product = if (productMatcher.find()) productMatcher.group(1) else "generic_product",
            fingerprint = if (fingerprintMatcher.find()) fingerprintMatcher.group(1) else "generic/device/id:android/release/keys",
            imei = if (imeiMatcher.find()) imeiMatcher.group(1) else "86${(1000000000..9999999999).random()}",
            hardwareSerial = "HW${(1000000..9999999).random()}",
            phoneNumber = "+1${(2000000000..9999999999).random()}",
            simImsi = "31026${(1000000000..9999999999).random()}",
            bootloader = "BL${randomPrefix}",
            board = "board_${randomPrefix.substring(0, 3)}",
            displayId = "ID.${randomPrefix}"
        )
    }
}