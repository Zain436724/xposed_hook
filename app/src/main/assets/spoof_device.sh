#!/system/bin/sh
#
# Device Info Spoofer CLI
#
# This script allows you to trigger device spoofing from the command line
# It will fetch a random device profile from the online service and apply it
#

PACKAGE="com.example.xposed_hooking_info"
ACTION="com.example.xposed_hooking_info.SPOOF_DEVICE"

echo "Device Info Spoofer"
echo "==================="
echo "Fetching and applying random device profile..."

# Send broadcast to the app to trigger device spoofing
am broadcast -n "$PACKAGE/.cli.CommandLineReceiver" -a "$ACTION"

echo "Command sent. Device information should be spoofed now."
echo "Launch the Device Info Spoofer app to see the current spoofed values."
