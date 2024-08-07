/**
 * Represents a discovered Zebra printer.
 * @property {string} address - MAC address, IP Address, or local name of printer.
 * @property {string} origin - The origin of the discovered printer. (net, bt, ble)
 */
export interface DiscoveredPrinter {
  address: string;
  origin: 'net' | 'bt' | 'ble';
}

export interface DiscoveredPrinterNetwork extends DiscoveredPrinter {}

/**
 * Represents a Bluetooth Low Energy (BLE) discovered Zebra printer.
 * @property {string} address - MAC address, IP Address, or local name of printer.
 * @property {string} friendlyName - The friendly name of the Bluetooth® device.
 */
export interface DiscoveredPrinterBluetoothLe extends DiscoveredPrinter {
  friendlyName: string;
}

/**
 * Represents a Bluetooth discovered Zebra printer.
 * @property {string} address - MAC address, IP Address, or local name of printer.
 * @property {string} friendlyName - The friendly name of the Bluetooth® device.
 */
export interface DiscoveredPrinterBluetooth extends DiscoveredPrinter {
  friendlyName: string;
}
