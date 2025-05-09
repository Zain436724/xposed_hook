package com.example.xposed_hooking_info.cli

import android.content.Context
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Helper class to extract shell scripts to external storage
 * so users can run them from terminal
 */
class ShellScriptHelper(private val context: Context) {

    /**
     * Extracts the spoofing shell script to external storage
     * Returns the path to the extracted script if successful
     */
    fun extractSpoofingScript(): String? {
        try {
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val scriptDir = File(documentsDir, "DeviceSpoofer")

            if (!scriptDir.exists()) {
                scriptDir.mkdirs()
            }

            val scriptFile = File(scriptDir, "spoof_device.sh")

            // Write the shell script from assets to external storage
            val inputStream = context.assets.open("spoof_device.sh")
            val outputStream = FileOutputStream(scriptFile)

            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }

            inputStream.close()
            outputStream.close()

            // Make the script executable
            scriptFile.setExecutable(true, false)

            // Show a toast to the user
            Toast.makeText(
                context,
                "Shell script extracted to:\n${scriptFile.absolutePath}",
                Toast.LENGTH_LONG
            ).show()

            return scriptFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Error extracting script: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            return null
        }
    }
}