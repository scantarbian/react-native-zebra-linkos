package com.zebralinkos

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule

abstract class ZebraLinkosSpec internal constructor(context: ReactApplicationContext) :
    ReactContextBaseJavaModule(context) {

      enum class ConnectionType {
        TCP,
        BLE,
        BT_INSECURE,
        USB
      }

  abstract fun writeTCP(ipAddress: String, zpl: String, promise: Promise)

  abstract fun writeBLE(macAddress: String, zpl: String, promise: Promise)

  abstract fun writeBTInsecure(macAddress: String, zpl: String, promise: Promise)

  abstract fun writeUSB(deviceId: String, zpl: String, promise: Promise)

  abstract fun scanNetwork(promise: Promise)

  abstract fun scanBluetooth(promise: Promise)

  abstract fun scanBluetoothLE(promise: Promise)

  abstract fun scanUSB(promise: Promise)

  abstract fun checkUSBPermission(deviceId: String, promise: Promise)

  abstract fun checkTCPPrinterStatus(ipAddress: String, promise: Promise)

  abstract fun checkBLEPrinterStatus(macAddress: String, promise: Promise)

  abstract fun checkBTPrinterStatus(macAddress: String, promise: Promise)

  abstract fun checkUSBPrinterStatus(deviceId: String, promise: Promise)
}
