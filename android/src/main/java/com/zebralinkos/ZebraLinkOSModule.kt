package com.zebralinkos

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.Arguments
import android.util.Log
// Zebra Connection Modules
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.comm.TcpConnection
// Zebra Network Discovery
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.sdk.printer.discovery.DiscoveryException
import com.zebra.sdk.printer.discovery.DiscoveryHandler
import com.zebra.sdk.printer.discovery.NetworkDiscoverer

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

  @ReactMethod
  fun scanNetwork(promise: Promise) {
    val discoveryHandler = object : DiscoveryHandler {
      val printers = mutableListOf<DiscoveredPrinter>()

      override fun foundPrinter(printer: DiscoveredPrinter) {
        printers.add(printer)
      }

      override fun discoveryFinished() {
        Log.d(NAME, "Discovery finished")
        printers.forEach { printer -> println(printer) }
      }

      override fun discoveryError(message: String?) {
        Log.e(NAME, "Discovery error: $message")
      }
    }

    try {
      Log.d(NAME, "Going to scan network")
      NetworkDiscoverer.findPrinters(discoveryHandler)
      promise.resolve(Arguments.makeNativeArray(discoveryHandler.printers))
    } catch (e: DiscoveryException) {
      Log.d(NAME, "Error scanning network: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_NETWORK_SCAN", e.localizedMessage, e)
    }
  }

  companion object {
    const val NAME = "ZebraLinkOS"
  }
}
