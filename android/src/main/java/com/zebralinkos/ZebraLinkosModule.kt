package com.zebralinkos

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule
import com.zebra.sdk.btleComm.BluetoothLeConnection
import com.zebra.sdk.btleComm.BluetoothLeDiscoverer
import com.zebra.sdk.btleComm.DiscoveredPrinterBluetoothLe
import com.zebra.sdk.comm.BluetoothConnectionInsecure
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.comm.TcpConnection
import com.zebra.sdk.comm.UsbConnection
import com.zebra.sdk.printer.PrinterStatus
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth
import com.zebra.sdk.printer.discovery.DiscoveredPrinterNetwork
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb
import com.zebra.sdk.printer.discovery.DiscoveryException
import com.zebra.sdk.printer.discovery.DiscoveryHandler
import com.zebra.sdk.printer.discovery.NetworkDiscoverer
import com.zebra.sdk.printer.discovery.UsbDiscoverer

@ReactModule(name = ZebraLinkosModule.NAME)
class ZebraLinkosModule internal constructor(reactContext: ReactApplicationContext) :
    ZebraLinkosSpec(reactContext) {

  private val usbManager =
      this.reactApplicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
  private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
  //  Handles USB permission requests
  private var usbReceiverRegistered = false
  private var pendingDevice: UsbDevice? = null
  private var pendingZpl: String? = null
  private var pendingPromise: Promise? = null
  private var pendingPermissionPromise: Promise? = null

  private val usbReceiver =
      object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
          if (intent.action == ACTION_USB_PERMISSION) {
            val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            val mode = intent.getStringExtra("Mode")

            Log.d(
                NAME,
                "USB permission received for device: ${device?.productName}, granted: $granted")

            if (granted) {
              if (pendingPermissionPromise != null) {
                pendingPermissionPromise?.resolve(true)
              } else if (device != null) {
                if (mode == "Status") {
                  checkUsbStatusNow(device.deviceId.toString(), device, pendingPromise)
                } else if (mode == "Print") {
                  writeToUsbNow(device, pendingZpl, pendingPromise)
                } else {
                  Log.e(NAME, "Unknown mode: $mode")
                  pendingPromise?.reject("E_USB_MODE", "Unknown USB mode: $mode")
                }
              }
            } else {
              pendingPermissionPromise?.reject(
                  "E_USB_PERMISSION_DENIED", "USB permission denied by user")
              pendingPromise?.reject("E_USB_PERMISSION_DENIED", "USB permission denied by user")
            }

            // Clean up
            pendingDevice = null
            pendingPromise = null
            pendingPermissionPromise = null

            try {
              context.unregisterReceiver(this)
            } catch (_: Exception) {}
            usbReceiverRegistered = false
          }
        }
      }

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  override fun writeTCP(ipAddress: String, zpl: String, promise: Promise) {
    val printerConnection = TcpConnection(ipAddress, TcpConnection.DEFAULT_ZPL_TCP_PORT)

    try {
      Log.d(NAME, "Going to write via TCP with IP Address: $ipAddress")
      printerConnection.open()

      Log.d(NAME, "Connection is open: ${printerConnection.isConnected}, sending data")
      printerConnection.write(zpl.toByteArray())

      promise.resolve(true)
    } catch (e: ConnectionException) {
      Log.e(NAME, "Error writing to TCP connection: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_TCP_CONNECTION", e.localizedMessage, e)
    } finally {
      printerConnection.close()
    }
  }

  @ReactMethod
  override fun writeBLE(macAddress: String, zpl: String, promise: Promise) {
    val printerConnection = BluetoothLeConnection(macAddress, this.reactApplicationContext)

    try {
      printerConnection.open()

      printerConnection.write(zpl.toByteArray())

      Thread.sleep(500)
      promise.resolve(true)
    } catch (e: ConnectionException) {
      Log.e(NAME, "Error writing to BLE connection: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_BLE_CONNECTION", e.localizedMessage, e)
    } finally {
      printerConnection.close()
    }
  }

  @ReactMethod
  override fun writeBTInsecure(macAddress: String, zpl: String, promise: Promise) {
    val printerConnection = BluetoothConnectionInsecure(macAddress)

    try {
      printerConnection.open()

      printerConnection.write(zpl.toByteArray())

      Thread.sleep(500)
      promise.resolve(true)
    } catch (e: ConnectionException) {
      Log.e(NAME, "Error writing to BT insecure connection: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_BT_INSECURE_CONNECTION", e.localizedMessage, e)
    } finally {
      printerConnection.close()
    }
  }

  @ReactMethod
  override fun writeUSB(deviceId: String, zpl: String, promise: Promise) {
    val device = getUSBDeviceById(deviceId)

    if (device == null) {
      Log.e(NAME, "USB device with ID $deviceId not found")
      promise.reject("E_USB_DEVICE_NOT_FOUND", "USB device with ID $deviceId not found")
      return
    }

    //      Request permission for the USB device
    if (usbManager.hasPermission(device)) {
      writeToUsbNow(device, zpl, promise)
    } else {
      pendingDevice = device
      pendingZpl = zpl
      pendingPromise = promise

      if (!usbReceiverRegistered) {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        ContextCompat.registerReceiver(
            reactApplicationContext, usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        usbReceiverRegistered = true
      }

      val explicitIntent =
          Intent(ACTION_USB_PERMISSION).apply {
            setPackage(reactApplicationContext.packageName)
            putExtra(UsbManager.EXTRA_DEVICE, device)
            putExtra("Mode", "Print")
          }

      val permissionIntent =
          PendingIntent.getBroadcast(
              reactApplicationContext, 0, explicitIntent, PendingIntent.FLAG_MUTABLE)

      usbManager.requestPermission(device, permissionIntent)
    }
  }

  private fun getUSBDeviceById(deviceId: String): UsbDevice? {
    val deviceList = usbManager.deviceList

    if (!deviceList.containsKey(deviceId)) {
      Log.e(NAME, "USB device with ID $deviceId not found")
      return null
    }

    val device =
        deviceList[deviceId]
            ?: run {
              Log.e(NAME, "USB device with ID $deviceId is null")
              return null
            }

    return device
  }

  private fun writeToUsbNow(
      device: UsbDevice,
      zpl: String? = pendingZpl,
      promise: Promise? = pendingPromise
  ) {
    val conn = UsbConnection(usbManager, device)
    try {
      conn.open()
      conn.write(zpl!!.toByteArray())
      Thread.sleep(500)
      promise?.resolve(true)
    } catch (e: ConnectionException) {
      Log.e(NAME, "Error writing to USB connection: ${e.localizedMessage}")
      promise?.reject("E_USB_CONNECTION", e.localizedMessage, e)
    } finally {
      try {
        conn.close()
      } catch (e: ConnectionException) {
        Log.e(NAME, "Error closing USB connection: ${e.localizedMessage}")
      }
      // Clean up pending
      pendingDevice = null
      pendingZpl = null
      pendingPromise = null
    }
  }

  private fun checkUsbStatusNow(deviceId: String, device: UsbDevice,       promise: Promise? = pendingPromise) {
    val conn = UsbConnection(usbManager, device)
    try {
      conn.open()

      val printer = ZebraPrinterFactory.getInstance(conn)

      val printerStatus = printer.currentStatus

      val printerStatusMap = convertStatusToWritableMap(deviceId, printerStatus)

      Log.d(NAME, "Printer status: $printerStatusMap")

      promise?.resolve(printerStatusMap)
    } catch (e: ConnectionException) {
      Log.e(NAME, "Error checking USB printer status: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise?.reject("E_USB_STATUS", e.localizedMessage, e)
    } catch (e: ZebraPrinterLanguageUnknownException) {
      Log.e(NAME, "Error getting printer language: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise?.reject("E_USB_STATUS", e.localizedMessage, e)
    } finally {
      try {
        conn.close()
      } catch (e: ConnectionException) {
        Log.e(NAME, "Error closing USB connection: ${e.localizedMessage}")
      }

      // Clean up pending
      pendingDevice = null
      pendingZpl = null
      pendingPromise = null
    }
  }

  @ReactMethod
  override fun checkUSBPermission(deviceId: String, promise: Promise) {
    val device = getUSBDeviceById(deviceId)

    if (device == null) {
      Log.e(NAME, "USB device with ID $deviceId not found")
      promise.reject("E_USB_DEVICE_NOT_FOUND", "USB device with ID $deviceId not found")
      return
    }

    if (usbManager.hasPermission(device)) {
      Log.d(NAME, "USB permission already granted for device: $deviceId")
      promise.resolve(true)
    } else {
      Log.d(NAME, "USB permission not yet granted for device: $deviceId")
      pendingDevice = device
      pendingPermissionPromise = promise

      if (!usbReceiverRegistered) {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        ContextCompat.registerReceiver(
            reactApplicationContext, usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        usbReceiverRegistered = true
      }

      val explicitIntent =
          Intent(ACTION_USB_PERMISSION).apply {
            setPackage(reactApplicationContext.packageName)
            putExtra(UsbManager.EXTRA_DEVICE, device)
          }

      val permissionIntent =
          PendingIntent.getBroadcast(
              reactApplicationContext, 0, explicitIntent, PendingIntent.FLAG_MUTABLE)

      usbManager.requestPermission(device, permissionIntent)
    }
  }

  @ReactMethod
  override fun scanNetwork(promise: Promise) {
    fun convertToWritableMap(printer: DiscoveredPrinterNetwork): WritableMap {
      val map = Arguments.createMap()
      map.putString("address", printer.address)
      map.putString("origin", "net")
      return map
    }

    val discoveryHandler =
        object : DiscoveryHandler {
          val printers = Arguments.createArray()

          override fun foundPrinter(printer: DiscoveredPrinter) {
            try {
              Log.d(NAME, "Found printer: $printer")
              val printerNetwork = printer as DiscoveredPrinterNetwork
              this.printers.pushMap(convertToWritableMap(printerNetwork))
              Log.d(NAME, "Found printer: ${printerNetwork.address}")
            } catch (e: Exception) {
              Log.e(NAME, "Error adding printer to list: ${e.localizedMessage}")
              promise.reject("E_NET_SCAN", e.localizedMessage, e)
              e.localizedMessage?.let { Log.e(NAME, it) }
              e.printStackTrace()
            }
          }

          override fun discoveryFinished() {
            Log.d(NAME, "Discovery finished")
            promise.resolve(this.printers)
          }

          override fun discoveryError(message: String?) {
            Log.e(NAME, "Network discovery error: $message")
            promise.reject("E_NET_SCAN", message)
          }
        }

    try {
      Log.d(NAME, "Going to scan network")
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
      map.putString("origin", "bt")
      return map
    }

    val discoveryHandler =
        object : DiscoveryHandler {
          val printers = Arguments.createArray()

          override fun foundPrinter(printer: DiscoveredPrinter) {
            try {
              val printerBluetooth = printer as DiscoveredPrinterBluetooth
              this.printers.pushMap(convertToWritableMap(printerBluetooth))
              Log.d(NAME, "Found printer: ${printerBluetooth.friendlyName}")
            } catch (e: Exception) {
              Log.e(NAME, "Error adding printer to list: ${e.localizedMessage}")
              promise.reject("E_BT_SCAN", e.localizedMessage, e)
              e.localizedMessage?.let { Log.e(NAME, it) }
              e.printStackTrace()
            }
          }

          override fun discoveryFinished() {
            Log.d(NAME, "Discovery finished")
            promise.resolve(this.printers)
          }

          override fun discoveryError(message: String?) {
            Log.e(NAME, "Bluetooth discovery error: $message")
            promise.reject("E_BT_SCAN", message)
          }
        }

    try {
      Log.d(NAME, "Going to scan bluetooth")
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
      map.putString("origin", "ble")
      return map
    }

    val discoveryHandler =
        object : DiscoveryHandler {
          val printers = Arguments.createArray()

          override fun foundPrinter(printer: DiscoveredPrinter) {
            try {
              val printerBluetoothLe = printer as DiscoveredPrinterBluetoothLe
              this.printers.pushMap(convertToWritableMap(printerBluetoothLe))
              Log.d(NAME, "Found printer: ${printerBluetoothLe.friendlyName}")
            } catch (e: Exception) {
              Log.e(NAME, "Error adding printer to list: ${e.localizedMessage}")
              promise.reject("E_BLE_SCAN", e.localizedMessage, e)
              e.localizedMessage?.let { Log.e(NAME, it) }
              e.printStackTrace()
            }
          }

          override fun discoveryFinished() {
            Log.d(NAME, "Discovery finished")
            promise.resolve(this.printers)
          }

          override fun discoveryError(message: String?) {
            Log.e(NAME, "Bluetooth discovery error: $message")
            promise.reject("E_BLE_SCAN", message)
          }
        }

    try {
      Log.d(NAME, "Going to scan bluetooth LE")
      BluetoothLeDiscoverer.findPrinters(this.reactApplicationContext, discoveryHandler)
    } catch (e: ConnectionException) {
      Log.e(NAME, "Error scanning bluetooth LE: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_BLE_SCAN", e.localizedMessage, e)
    }
  }

  @ReactMethod
  override fun scanUSB(promise: Promise) {
    fun convertToWritableMap(printer: DiscoveredPrinterUsb): WritableMap {
      val map = Arguments.createMap()
      map.putString("friendlyName", printer.device.productName)
      map.putString("manufacturer", printer.device.manufacturerName)
      map.putString("address", printer.address)
      map.putString("origin", "usb")
      return map
    }

    val discoveryHandler =
        object : DiscoveryHandler {
          val printers = Arguments.createArray()

          override fun foundPrinter(printer: DiscoveredPrinter) {
            try {
              val printerUSB = printer as DiscoveredPrinterUsb
              this.printers.pushMap(convertToWritableMap(printerUSB))
              Log.d(NAME, "Found USB printer: ${printerUSB.address}")
            } catch (e: Exception) {
              Log.e(NAME, "Error adding USB printer to list: ${e.localizedMessage}")
              promise.reject("E_USB_SCAN", e.localizedMessage, e)
              e.localizedMessage?.let { Log.e(NAME, it) }
              e.printStackTrace()
            }
          }

          override fun discoveryFinished() {
            Log.d(NAME, "USB discovery finished")
            promise.resolve(this.printers)
          }

          override fun discoveryError(message: String?) {
            Log.e(NAME, "USB discovery error: $message")
            promise.reject("E_USB_SCAN", message)
          }
        }

    try {
      Log.d(NAME, "Going to scan USB")
      UsbDiscoverer.findPrinters(this.reactApplicationContext, discoveryHandler)
    } catch (e: ConnectionException) {
      Log.e(NAME, "Error scanning USB: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_USB_SCAN", e.localizedMessage, e)
    }
  }

  private fun convertStatusToWritableMap(
      printerAddress: String,
      status: PrinterStatus
  ): WritableMap {
    val map = Arguments.createMap()
    map.putString("address", printerAddress)
    map.putBoolean("isHeadCold", status.isHeadCold)
    map.putBoolean("isHeadTooHot", status.isHeadTooHot)
    map.putBoolean("isPaperOut", status.isPaperOut)
    map.putBoolean("isPartialFormatInProgress", status.isPartialFormatInProgress)
    map.putBoolean("isPaused", status.isPaused)
    map.putBoolean("isReadyToPrint", status.isReadyToPrint)
    map.putBoolean("isReceiveBufferFull", status.isReceiveBufferFull)
    map.putBoolean("isRibbonOut", status.isRibbonOut)
    map.putInt("labelLengthInDots", status.labelLengthInDots)
    map.putInt("labelsRemainingInBatch", status.labelsRemainingInBatch)
    map.putInt("numberOfFormatsInReceiveBuffer", status.numberOfFormatsInReceiveBuffer)
    map.putString("printMode", status.printMode.toString())

    return map
  }

  @ReactMethod
  override fun checkTCPPrinterStatus(ipAddress: String, promise: Promise) {
    val printerConnection = TcpConnection(ipAddress, TcpConnection.DEFAULT_ZPL_TCP_PORT)

    try {
      printerConnection.open()

      val printer = ZebraPrinterFactory.getInstance(printerConnection)

      val printerStatus = printer.currentStatus

      val printerStatusMap = convertStatusToWritableMap(ipAddress, printerStatus)

      Log.d(NAME, "Printer status: $printerStatusMap")

      promise.resolve(printerStatusMap)
    } catch (e: ConnectionException) {
      Log.e(NAME, "Error checking TCP printer status: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_TCP_STATUS", e.localizedMessage, e)
    } catch (e: ZebraPrinterLanguageUnknownException) {
      Log.e(NAME, "Error getting printer language: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_TCP_STATUS", e.localizedMessage, e)
    } finally {
      printerConnection.close()
    }
  }

  @ReactMethod
  override fun checkBLEPrinterStatus(macAddress: String, promise: Promise) {
    val printerConnection = BluetoothLeConnection(macAddress, this.reactApplicationContext)

    try {
      printerConnection.open()

      val printer = ZebraPrinterFactory.getInstance(printerConnection)

      val printerStatus = printer.currentStatus

      val printerStatusMap = convertStatusToWritableMap(macAddress, printerStatus)

      Log.d(NAME, "Printer status: $printerStatusMap")

      promise.resolve(printerStatusMap)
    } catch (e: ConnectionException) {
      Log.e(NAME, "Error checking BLE printer status: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_BLE_STATUS", e.localizedMessage, e)
    } catch (e: ZebraPrinterLanguageUnknownException) {
      Log.e(NAME, "Error getting printer language: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_BLE_STATUS", e.localizedMessage, e)
    } finally {
      printerConnection.close()
    }
  }

  @ReactMethod
  override fun checkBTInsecurePrinterStatus(macAddress: String, promise: Promise) {
    val printerConnection = BluetoothConnectionInsecure(macAddress)

    try {
      printerConnection.open()

      val printer = ZebraPrinterFactory.getInstance(printerConnection)

      val printerStatus = printer.currentStatus

      val printerStatusMap = convertStatusToWritableMap(macAddress, printerStatus)

      Log.d(NAME, "Printer status: $printerStatusMap")

      promise.resolve(printerStatusMap)
    } catch (e: ConnectionException) {
      Log.e(NAME, "Error checking BT insecure printer status: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_BT_INSECURE_STATUS", e.localizedMessage, e)
    } catch (e: ZebraPrinterLanguageUnknownException) {
      Log.e(NAME, "Error getting printer language: ${e.localizedMessage}")
      e.localizedMessage?.let { Log.e(NAME, it) }
      e.printStackTrace()
      promise.reject("E_BT_INSECURE_STATUS", e.localizedMessage, e)
    } finally {
      printerConnection.close()
    }
  }

  @ReactMethod
  override fun checkUSBPrinterStatus(deviceId: String, promise: Promise) {
    val device = getUSBDeviceById(deviceId)

    if (device == null) {
      Log.e(NAME, "USB device with ID $deviceId not found")
      promise.reject("E_USB_DEVICE_NOT_FOUND", "USB device with ID $deviceId not found")
      return
    }

    //      Request permission for the USB device
    if (usbManager.hasPermission(device)) {
      checkUsbStatusNow(deviceId, device, promise)
    } else {
      pendingDevice = device
      pendingPromise = promise

      if (!usbReceiverRegistered) {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        ContextCompat.registerReceiver(
            reactApplicationContext, usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        usbReceiverRegistered = true
      }

      val explicitIntent =
          Intent(ACTION_USB_PERMISSION).apply {
            setPackage(reactApplicationContext.packageName)
            putExtra(UsbManager.EXTRA_DEVICE, device)
            putExtra("Mode", "Status")
          }

      val permissionIntent =
          PendingIntent.getBroadcast(
              reactApplicationContext, 0, explicitIntent, PendingIntent.FLAG_MUTABLE)

      usbManager.requestPermission(device, permissionIntent)
    }
  }

  companion object {
    const val NAME = "ZebraLinkos"
  }
}
