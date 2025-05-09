package com.example.xposed_hooking_info.hook

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import com.example.xposed_hooking_info.data.DevicePreferencesRepository
import com.example.xposed_hooking_info.model.DeviceInfo
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.type.java.IntType

class DeviceInfoHook : YukiBaseHooker() {

    override fun onHook() {
        // Get saved device info
        val context = appContext ?: return
        val repository = DevicePreferencesRepository(context)
        val fakeDeviceInfo: DeviceInfo = repository.getSavedDeviceInfoSync(context)

        loggerD(msg = "Loading fake device info: ${fakeDeviceInfo.brand} ${fakeDeviceInfo.model}")

        // Hook Build class fields
        hookBuildFields(fakeDeviceInfo)

        // Hook TelephonyManager methods
        hookTelephonyManager(fakeDeviceInfo)
    }

    private fun hookBuildFields(fakeInfo: DeviceInfo) {
        // Hook common Build fields
        if (fakeInfo.brand.isNotEmpty()) {
            Build::class.java.field { name = "BRAND" }.get().set(fakeInfo.brand)
        }

        if (fakeInfo.model.isNotEmpty()) {
            Build::class.java.field { name = "MODEL" }.get().set(fakeInfo.model)
        }

        if (fakeInfo.device.isNotEmpty()) {
            Build::class.java.field { name = "DEVICE" }.get().set(fakeInfo.device)
        }

        if (fakeInfo.product.isNotEmpty()) {
            Build::class.java.field { name = "PRODUCT" }.get().set(fakeInfo.product)
        }

        if (fakeInfo.manufacturer.isNotEmpty()) {
            Build::class.java.field { name = "MANUFACTURER" }.get().set(fakeInfo.manufacturer)
        }

        if (fakeInfo.buildId.isNotEmpty()) {
            Build::class.java.field { name = "ID" }.get().set(fakeInfo.buildId)
        }

        if (fakeInfo.fingerprint.isNotEmpty()) {
            Build::class.java.field { name = "FINGERPRINT" }.get().set(fakeInfo.fingerprint)
        }

        if (fakeInfo.bootloader.isNotEmpty()) {
            Build::class.java.field { name = "BOOTLOADER" }.get().set(fakeInfo.bootloader)
        }

        if (fakeInfo.board.isNotEmpty()) {
            Build::class.java.field { name = "BOARD" }.get().set(fakeInfo.board)
        }

        if (fakeInfo.displayId.isNotEmpty()) {
            Build::class.java.field { name = "DISPLAY" }.get().set(fakeInfo.displayId)
        }

        // Hook getSerial() method
        if (fakeInfo.hardwareSerial.isNotEmpty()) {
            Build::class.java.method {
                name = "getSerial"
            }.hook {
                after {
                    result = fakeInfo.hardwareSerial
                }
            }
        }
    }

    private fun hookTelephonyManager(fakeInfo: DeviceInfo) {
        // Hook TelephonyManager methods
        if (fakeInfo.imei.isNotEmpty()) {
            TelephonyManager::class.java.method {
                name = "getImei"
            }.hook {
                after {
                    result = fakeInfo.imei
                }
            }

            TelephonyManager::class.java.method {
                name = "getImei"
                param(Int::class.java)
            }.hook {
                after {
                    result = fakeInfo.imei
                }
            }

            // For older Android versions
            TelephonyManager::class.java.method {
                name = "getDeviceId"
            }.hook {
                after {
                    result = fakeInfo.imei
                }
            }

            TelephonyManager::class.java.method {
                name = "getDeviceId"
                param(Int::class.java)
            }.hook {
                after {
                    result = fakeInfo.imei
                }
            }
        }

        if (fakeInfo.phoneNumber.isNotEmpty()) {
            TelephonyManager::class.java.method {
                name = "getLine1Number"
            }.hook {
                after {
                    result = fakeInfo.phoneNumber
                }
            }
        }

        if (fakeInfo.simImsi.isNotEmpty()) {
            TelephonyManager::class.java.method {
                name = "getSubscriberId"
            }.hook {
                after {
                    result = fakeInfo.simImsi
                }
            }
        }
    }
}