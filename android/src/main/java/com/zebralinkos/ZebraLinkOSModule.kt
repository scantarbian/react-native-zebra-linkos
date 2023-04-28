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
import com.zebra.sdk.printer.discovery.DiscoveredPrinterNetwork
import com.zebra.sdk.printer.discovery.DiscoveryException
import com.zebra.sdk.printer.discovery.DiscoveryHandler
import com.zebra.sdk.printer.discovery.NetworkDiscoverer
// Zebra Bluetooth Discovery
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth
import org.json.JSONArray
import org.json.JSONObject

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
      val printers = arrayListOf<String>()

      override fun foundPrinter(printer: DiscoveredPrinter) {
        try {
          this.printers.add((printer as DiscoveredPrinterNetwork).address)
          Log.d(NAME, "Found printer: ${printer.address}")
        } catch (e: Exception) {
          Log.e(NAME, "Error adding printer to list: ${e.localizedMessage}")
          promise.reject("E_NETWORK_SCAN", e.localizedMessage, e)
          e.localizedMessage?.let { Log.e(NAME, it) }
          e.printStackTrace()
        }
      }

      override fun discoveryFinished() {
        Log.d(NAME, "Discovery finished")
        promise.resolve(Arguments.makeNativeArray(this.printers))
      }

      override fun discoveryError(message: String?) {
        Log.e(NAME, "Network discovery error: $message")
        promise.reject("E_NETWORK_SCAN", message)
      }
    }

    try {
      Log.d(NAME, "Going to scan network")
      NetworkDiscoverer.findPrinters(discoveryHandler)
    } catch (e: DiscoveryException) {
      Log.e(NAME, "Error scanning network: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_NETWORK_SCAN", e.localizedMessage, e)
    }
  }

  @ReactMethod
  fun scanBluetooth(promise: Promise) {
    val discoveryHandler = object : DiscoveryHandler{
        val foundPrinterList = ArrayList<Map<String, String>>()

        override fun foundPrinter(printer: DiscoveredPrinter) {
          val foundPrinter = HashMap<String, String>()
          Log.d(NAME, "Found printer: ${printer.address}")
          foundPrinter["address"] = printer.address
          foundPrinter["name"] = (printer as DiscoveredPrinterBluetooth).friendlyName
          foundPrinterList.add(foundPrinter)
        }

        override fun discoveryFinished() {
          Log.d(NAME, "Bluetooth discovery finished")
          val jsonObj = ArrayList<JSONObject>()

          for (printer in foundPrinterList) {
            jsonObj.add(JSONObject(printer))
          }

          val foundPrinterJSON = JSONArray(jsonObj)

          promise.resolve(foundPrinterJSON.toString())
        }

        override fun discoveryError(message: String?) {
          Log.e(NAME, "Bluetooth discovery error: $message")
          promise.reject("E_BLUETOOTH_SCAN", message)
        }
      }

      try {
        Log.d(NAME, "Going to scan bluetooth")
        BluetoothDiscoverer.findPrinters(this.reactApplicationContext, discoveryHandler)
      } catch (e: DiscoveryException) {
        Log.e(NAME, "Error scanning bluetooth: ${e.localizedMessage}")
        e.localizedMessage?.let { Log.e(NAME, it) }
        e.printStackTrace()
        promise.reject("E_BLUETOOTH_SCAN", e.localizedMessage, e)
    }
  }

  companion object {
    const val NAME = "ZebraLinkOS"
  }
}
