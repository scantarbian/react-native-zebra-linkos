/**
 * Represents a discovered Zebra printer.
 * @property {string} address - MAC address, IP Address, or local name of printer.
 * @property {string} origin - The origin of the discovered printer. (net, bt, ble)
 */
export interface DiscoveredPrinter {
  address: string;
  origin: 'net' | 'bt' | 'ble' | 'usb';
}

export interface DiscoveredPrinterNetwork extends DiscoveredPrinter {
  origin: 'net';
}

/**
 * Represents a Bluetooth Low Energy (BLE) discovered Zebra printer.
 * @property {string} address - MAC address, IP Address, or local name of printer.
 * @property {string} friendlyName - The friendly name of the Bluetooth® device.
 */
export interface DiscoveredPrinterBluetoothLe extends DiscoveredPrinter {
  friendlyName: string;
  origin: 'ble';
}

/**
 * Represents a Bluetooth discovered Zebra printer.
 * @property {string} address - MAC address, IP Address, or local name of printer.
 * @property {string} friendlyName - The friendly name of the Bluetooth® device.
 */
export interface DiscoveredPrinterBluetooth extends DiscoveredPrinter {
  friendlyName: string;
  origin: 'bt';
}

export interface DiscoveredPrinterUSB extends DiscoveredPrinter {
  friendlyName: string;
  manufacturer: string;
  origin: 'usb';
}
