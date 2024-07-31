const ZebraLinkos = require('./NativeZebraLinkos').default;
import type {
  DiscoveredPrinter,
  DiscoveredPrinterBluetooth,
  DiscoveredPrinterBluetoothLe,
  DiscoveredPrinterNetwork,
} from './@types/index';

export async function writeTCP(
  ipAddress: string,
  zpl: string
): Promise<boolean> {
  return await ZebraLinkos.writeTCP(ipAddress, zpl);
}

export async function scanNetwork(): Promise<DiscoveredPrinterNetwork[]> {
  return await ZebraLinkos.scanNetwork();
}

export async function scanBluetooth(): Promise<DiscoveredPrinterBluetooth[]> {
  return await ZebraLinkos.scanBluetooth();
}

export async function scanBluetoothLE(): Promise<
  DiscoveredPrinterBluetoothLe[]
> {
  return await ZebraLinkos.scanBluetoothLE();
}

export async function writeBLE(
  macAddress: string,
  zpl: string
): Promise<boolean> {
  return await ZebraLinkos.writeBLE(macAddress, zpl);
}

export async function writeBTInsecure(
  macAddress: string,
  zpl: string
): Promise<boolean> {
  return await ZebraLinkos.writeBTInsecure(macAddress, zpl);
}

export type {
  DiscoveredPrinter,
  DiscoveredPrinterBluetooth,
  DiscoveredPrinterBluetoothLe,
  DiscoveredPrinterNetwork,
};
