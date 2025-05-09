# Android Device Info Spoofer

An Xposed module that allows you to spoof your device information. This app can change device properties like IMEI, brand, model, and more to help protect your privacy or for testing purposes.

## Features

- Spoof Android Build properties (brand, model, device, etc.)
- Spoof telephony information (IMEI, phone number, IMSI)
- Fetch random device profiles from an online service
- Terminal command support for changing device info via shell
- User-friendly UI for viewing and editing device properties

## Requirements

- Android 7.0 (API 24) or higher
- Xposed Framework or similar (LSPosed, EdXposed)
- Internet connection for fetching random device profiles

## Installation

1. Install the APK on your device
2. Enable the module in your Xposed manager (LSPosed, EdXposed, etc.)
3. Reboot your device
4. Launch the app to configure device information

## Usage

### Through the App

1. Launch the app
2. View your current device information in the "Current Device Information" section
3. Use the "Fetch Random Device Info" button to get a random device profile
4. Or manually edit any field in the "Spoofed Device Information" section
5. Changes will be applied after app restart or device reboot

### Through Terminal

1. In the app, tap "Extract Terminal Command Script" button
2. Grant storage permission if prompted
3. The script will be saved to your Documents/DeviceSpoofer folder
4. Open a terminal app with root access
5. Navigate to the script location
6. Make the script executable: `chmod +x spoof_device.sh`
7. Execute it: `./spoof_device.sh`

## How it Works

This app uses YukiHookAPI to hook into Android's system classes like android.os.Build and TelephonyManager to modify device information at runtime. When enabled, it will replace the real values with your spoofed values, making apps believe you're using a different device.

## Troubleshooting

If the spoofing doesn't seem to work:

- Make sure the module is properly enabled in your Xposed manager
- Reboot your device after enabling
- Check if the target app is in the module's scope
- Some apps use additional methods to detect device information that may not be covered by this module

## License

This project is licensed under the MIT License - see the LICENSE file for details.
