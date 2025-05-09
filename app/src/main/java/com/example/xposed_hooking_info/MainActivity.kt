package com.example.xposed_hooking_info

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.xposed_hooking_info.cli.ShellScriptHelper
import com.example.xposed_hooking_info.model.DeviceInfo
import com.example.xposed_hooking_info.ui.DiagnosticsScreen
import com.example.xposed_hooking_info.ui.MainViewModel
import com.example.xposed_hooking_info.ui.theme.Xposed_hooking_infoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var shellScriptHelper: ShellScriptHelper

    // Permission launcher for storage access
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, extract the script
            shellScriptHelper.extractSpoofingScript()
        }
    }

    // Permission launcher for phone state access
    private val phoneStatePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, refresh device info
            viewModel.refreshCurrentDeviceInfo()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        shellScriptHelper = ShellScriptHelper(this)

        // Check if we need to request phone state permission
        if (!viewModel.hasPhoneStatePermission()) {
            requestPhoneStatePermission()
        }

        enableEdgeToEdge()
        setContent {
            Xposed_hooking_infoTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        DeviceInfoSpoofingApp(
                            viewModel = viewModel,
                            onExtractShellScript = { extractShellScript() },
                            onNavigateToDiagnostics = { navController.navigate("diagnostics") },
                            onRequestPhonePermission = { requestPhoneStatePermission() }
                        )
                    }
                    composable("diagnostics") {
                        DiagnosticsScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                }
            }
        }
    }

    private fun extractShellScript() {
        // Check if we have the required permission
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, extract the script
                shellScriptHelper.extractSpoofingScript()
            }
            else -> {
                // Request the permission
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun requestPhoneStatePermission() {
        phoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoSpoofingApp(
    viewModel: MainViewModel,
    onExtractShellScript: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onRequestPhonePermission: () -> Unit
) {
    val currentDeviceInfo by viewModel.currentDeviceInfo.collectAsState()
    val savedDeviceInfo by viewModel.savedDeviceInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val hasPhonePermission = viewModel.hasPhoneStatePermission()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Info Spoofer") },
                actions = {
                    IconButton(onClick = onNavigateToDiagnostics) {
                        Text("⚙️")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Phone permission banner
                if (!hasPhonePermission) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grant phone state permission to see IMEI and more",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = onRequestPhonePermission
                            ) {
                                Text("Grant")
                            }
                        }
                    }
                }

                // Error message
                errorMessage?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(onClick = { viewModel.clearError() }) {
                                Text("X")
                            }
                        }
                    }
                }

                // Current Device Info Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Current Device Information",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DisplayDeviceInfoItem("Brand", currentDeviceInfo.brand)
                        DisplayDeviceInfoItem("Model", currentDeviceInfo.model)
                        DisplayDeviceInfoItem("Device", currentDeviceInfo.device)
                        DisplayDeviceInfoItem("Manufacturer", currentDeviceInfo.manufacturer)
                        DisplayDeviceInfoItem("Product", currentDeviceInfo.product)
                        DisplayDeviceInfoItem("Build ID", currentDeviceInfo.buildId)
                        DisplayDeviceInfoItem("Fingerprint", currentDeviceInfo.fingerprint)
                        if (hasPhonePermission) {
                            DisplayDeviceInfoItem("IMEI", currentDeviceInfo.imei)
                            DisplayDeviceInfoItem("Serial", currentDeviceInfo.hardwareSerial)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fake Device Info Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Spoofed Device Information",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        EditableDeviceInfoItem("Brand", savedDeviceInfo.brand) {
                            viewModel.updateDeviceInfoField("brand", it)
                        }
                        EditableDeviceInfoItem("Model", savedDeviceInfo.model) {
                            viewModel.updateDeviceInfoField("model", it)
                        }
                        EditableDeviceInfoItem("Device", savedDeviceInfo.device) {
                            viewModel.updateDeviceInfoField("device", it)
                        }
                        EditableDeviceInfoItem("Product", savedDeviceInfo.product) {
                            viewModel.updateDeviceInfoField("product", it)
                        }
                        EditableDeviceInfoItem("Manufacturer", savedDeviceInfo.manufacturer) {
                            viewModel.updateDeviceInfoField("manufacturer", it)
                        }
                        EditableDeviceInfoItem("IMEI", savedDeviceInfo.imei) {
                            viewModel.updateDeviceInfoField("imei", it)
                        }
                        EditableDeviceInfoItem("Hardware Serial", savedDeviceInfo.hardwareSerial) {
                            viewModel.updateDeviceInfoField("hardwareSerial", it)
                        }
                        EditableDeviceInfoItem("Phone Number", savedDeviceInfo.phoneNumber) {
                            viewModel.updateDeviceInfoField("phoneNumber", it)
                        }
                        EditableDeviceInfoItem("SIM IMSI", savedDeviceInfo.simImsi) {
                            viewModel.updateDeviceInfoField("simImsi", it)
                        }
                        EditableDeviceInfoItem("Build ID", savedDeviceInfo.buildId) {
                            viewModel.updateDeviceInfoField("buildId", it)
                        }
                        EditableDeviceInfoItem("Bootloader", savedDeviceInfo.bootloader) {
                            viewModel.updateDeviceInfoField("bootloader", it)
                        }
                        EditableDeviceInfoItem("Fingerprint", savedDeviceInfo.fingerprint) {
                            viewModel.updateDeviceInfoField("fingerprint", it)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.fetchRandomDeviceInfo() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    enabled = !isLoading
                ) {
                    Text("Fetch Random Device Info")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onExtractShellScript() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    enabled = !isLoading
                ) {
                    Text("Extract Terminal Command Script")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DisplayDeviceInfoItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value.ifEmpty { "Not available" },
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableDeviceInfoItem(label: String, value: String, onValueChange: (String) -> Unit) {
    var text by remember { mutableStateOf(value) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                onValueChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}