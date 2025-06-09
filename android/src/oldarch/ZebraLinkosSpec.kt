package com.zebralinkos

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule

abstract class ZebraLinkosSpec internal constructor(context: ReactApplicationContext) :
    ReactContextBaseJavaModule(context) {

  abstract fun writeTCP(ipAddress: String, zpl: String, promise: Promise)

  abstract fun writeBLE(macAddress: String, zpl: String, promise: Promise)

  abstract fun writeBTInsecure(macAddress: String, zpl: String, promise: Promise)

  abstract fun scanNetwork(promise: Promise)

  abstract fun scanBluetooth(promise: Promise)

  abstract fun scanBluetoothLE(promise: Promise)
}
