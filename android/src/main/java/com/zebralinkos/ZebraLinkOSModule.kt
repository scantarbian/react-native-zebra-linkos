package com.zebralinkos

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import android.util.Log
// Zebra Connection Modules
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.comm.TcpConnection

class ZebraLinkOSModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b)
  }

  @ReactMethod
  fun writeTCP(ipAddress: String, zpl: String, promise: Promise) {
    val printerConnection: Connection = TcpConnection(ipAddress, TcpConnection.DEFAULT_ZPL_TCP_PORT)

    try {
      Log.d(NAME, "Going to write via TCP with IP Address: $ipAddress")
      printerConnection.open()

      Log.d(NAME, "Connection is open: ${printerConnection.isConnected}, sending data")
      printerConnection.write(zpl.toByteArray())

      promise.resolve(true)
    } catch (e: ConnectionException) {
      Log.d(NAME, "Error writing to TCP connection: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_TCP_CONNECTION", e.localizedMessage, e)
    } finally {
      printerConnection.close()
    }
  }

  companion object {
    const val NAME = "ZebraLinkOS"
  }
}
