package com.zebralinkos

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.zebra.sdk.btleComm.BluetoothLeDiscoverer
import com.zebra.sdk.btleComm.DiscoveredPrinterBluetoothLe
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.comm.TcpConnection
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth
import com.zebra.sdk.printer.discovery.DiscoveredPrinterNetwork
import com.zebra.sdk.printer.discovery.DiscoveryException
import com.zebra.sdk.printer.discovery.DiscoveryHandler
import com.zebra.sdk.printer.discovery.NetworkDiscoverer

@ReactModule(name = ZebraLinkosModule.NAME)
class ZebraLinkosModule(reactContext: ReactApplicationContext) :
        NativeZebraLinkosSpec(reactContext) {

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  override fun writeTCP(ipAddress: String, zpl: String, promise: Promise) {
    val printerConnection: Connection = TcpConnection(ipAddress, TcpConnection.DEFAULT_ZPL_TCP_PORT)

    try {
      Log.i(NAME, "Going to write via TCP with IP Address: $ipAddress")
      printerConnection.open()

      Log.i(NAME, "Connection is open: ${printerConnection.isConnected}, sending data")
      printerConnection.write(zpl.toByteArray())

      promise.resolve(true)
    } catch (e: ConnectionException) {
      Log.i(NAME, "Error writing to TCP connection: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_TCP_CONNECTION", e.localizedMessage, e)
    } finally {
      printerConnection.close()
    }
  }

  @ReactMethod
  override fun scanNetwork(promise: Promise) {
    fun convertToWritableMap(printer: DiscoveredPrinterNetwork): WritableMap {
      val map = Arguments.createMap()
      map.putString("address", printer.address)
      return map
    }

    val discoveryHandler =
            object : DiscoveryHandler {
              val printers = Arguments.createArray()

              override fun foundPrinter(printer: DiscoveredPrinter) {
                try {
                  val printerNetwork = printer as DiscoveredPrinterNetwork
                  this.printers.pushMap(convertToWritableMap(printerNetwork))
                  Log.i(NAME, "Found printer: ${printerNetwork.address}")
                } catch (e: Exception) {
                  Log.e(NAME, "Error adding printer to list: ${e.localizedMessage}")
                  promise.reject("E_NET_SCAN", e.localizedMessage, e)
                  e.localizedMessage?.let { Log.e(NAME, it) }
                  e.printStackTrace()
                }
              }

              override fun discoveryFinished() {
                Log.i(NAME, "Discovery finished")
                promise.resolve(this.printers)
              }

              override fun discoveryError(message: String?) {
                Log.e(NAME, "Network discovery error: $message")
                promise.reject("E_NET_SCAN", message)
              }
            }

    try {
      Log.i(NAME, "Going to scan network")
      NetworkDiscoverer.findPrinters(discoveryHandler)
    } catch (e: DiscoveryException) {
      Log.e(NAME, "Error scanning network: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_NET_SCAN", e.localizedMessage, e)
    }
  }

  @ReactMethod
  override fun scanBluetooth(promise: Promise) {
    fun convertToWritableMap(printer: DiscoveredPrinterBluetooth): WritableMap {
      val map = Arguments.createMap()
      map.putString("address", printer.address)
      map.putString("friendlyName", printer.friendlyName)
      return map
    }

    val discoveryHandler =
            object : DiscoveryHandler {
              val printers = Arguments.createArray()

              override fun foundPrinter(printer: DiscoveredPrinter) {
                try {
                  val printerBluetooth = printer as DiscoveredPrinterBluetooth
                  this.printers.pushMap(convertToWritableMap(printerBluetooth))
                  Log.i(NAME, "Found printer: ${printerBluetooth.friendlyName}")
                } catch (e: Exception) {
                  Log.e(NAME, "Error adding printer to list: ${e.localizedMessage}")
                  promise.reject("E_BT_SCAN", e.localizedMessage, e)
                  e.localizedMessage?.let { Log.e(NAME, it) }
                  e.printStackTrace()
                }
              }

              override fun discoveryFinished() {
                Log.i(NAME, "Discovery finished")
                promise.resolve(this.printers)
              }

              override fun discoveryError(message: String?) {
                Log.e(NAME, "Bluetooth discovery error: $message")
                promise.reject("E_BT_SCAN", message)
              }
            }

    try {
      Log.i(NAME, "Going to scan bluetooth")
      BluetoothDiscoverer.findPrinters(this.reactApplicationContext, discoveryHandler)
    } catch (e: ConnectionException) {
      Log.e(NAME, "Error scanning bluetooth: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_BT_SCAN", e.localizedMessage, e)
    }
  }

  @ReactMethod
  override fun scanBluetoothLE(promise: Promise) {
    fun convertToWritableMap(printer: DiscoveredPrinterBluetoothLe): WritableMap {
      val map = Arguments.createMap()
      map.putString("address", printer.address)
      map.putString("friendlyName", printer.friendlyName)
      return map
    }

    val discoveryHandler =
            object : DiscoveryHandler {
              val printers = Arguments.createArray()

              override fun foundPrinter(printer: DiscoveredPrinter) {
                try {
                  val printerBluetoothLe = printer as DiscoveredPrinterBluetoothLe
                  this.printers.pushMap(convertToWritableMap(printerBluetoothLe))
                  Log.i(NAME, "Found printer: ${printerBluetoothLe.friendlyName}")
                } catch (e: Exception) {
                  Log.e(NAME, "Error adding printer to list: ${e.localizedMessage}")
                  promise.reject("E_BTLE_SCAN", e.localizedMessage, e)
                  e.localizedMessage?.let { Log.e(NAME, it) }
                  e.printStackTrace()
                }
              }

              override fun discoveryFinished() {
                Log.i(NAME, "Discovery finished")
                promise.resolve(this.printers)
              }

              override fun discoveryError(message: String?) {
                Log.e(NAME, "Bluetooth discovery error: $message")
                promise.reject("E_BTLE_SCAN", message)
              }
            }

    try {
      Log.i(NAME, "Going to scan bluetooth LE")
      BluetoothLeDiscoverer.findPrinters(this.reactApplicationContext, discoveryHandler)
    } catch (e: ConnectionException) {
      Log.e(NAME, "Error scanning bluetooth LE: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_BTLE_SCAN", e.localizedMessage, e)
    }
  }

  companion object {
    const val NAME = "ZebraLinkos"
  }
}
