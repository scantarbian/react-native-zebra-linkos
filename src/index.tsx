import type {
  DiscoveredPrinter,
  DiscoveredPrinterBluetooth,
  DiscoveredPrinterBluetoothLe,
  DiscoveredPrinterNetwork,
  DiscoveredPrinterUSB,
} from './@types/index';
import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-zebra-linkos' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// @ts-expect-error
const isTurboModuleEnabled = global.__turboModuleProxy != null;

const ZebraLinkosModule = isTurboModuleEnabled
  ? require('./NativeZebraLinkos').default
  : NativeModules.ZebraLinkos;

export function writeTCP(ipAddress: string, zpl: string): Promise<boolean> {
  return ZebraLinkos.writeTCP(ipAddress, zpl);
}

export function scanNetwork(): Promise<DiscoveredPrinterNetwork[]> {
  return ZebraLinkos.scanNetwork();
}

export function scanBluetooth(): Promise<DiscoveredPrinterBluetooth[]> {
  return ZebraLinkos.scanBluetooth();
}

export function scanBluetoothLE(): Promise<DiscoveredPrinterBluetoothLe[]> {
  return ZebraLinkos.scanBluetoothLE();
}

export function scanUSB(): Promise<DiscoveredPrinterUSB[]> {
  return ZebraLinkos.scanUSB();
}

export function writeBLE(macAddress: string, zpl: string): Promise<boolean> {
  return ZebraLinkos.writeBLE(macAddress, zpl);
}

export function writeBTInsecure(
  macAddress: string,
  zpl: string
): Promise<boolean> {
  return ZebraLinkos.writeBTInsecure(macAddress, zpl);
}

export function writeUSB(deviceId: string, zpl: string): Promise<boolean> {
  return ZebraLinkos.writeUSB(deviceId, zpl);
}

export function checkUSBPermission(deviceId: string): Promise<boolean> {
  return ZebraLinkos.checkUSBPermission(deviceId);
}

export interface ZebraLinkosModule {
  writeTCP: typeof writeTCP;
  scanNetwork: typeof scanNetwork;
  scanBluetooth: typeof scanBluetooth;
  scanBluetoothLE: typeof scanBluetoothLE;
  scanUSB: typeof scanUSB;
  writeBLE: typeof writeBLE;
  writeBTInsecure: typeof writeBTInsecure;
  writeUSB: typeof writeUSB;
  checkUSBPermission: typeof checkUSBPermission;
}

export type {
  DiscoveredPrinter,
  DiscoveredPrinterBluetooth,
  DiscoveredPrinterBluetoothLe,
  DiscoveredPrinterNetwork,
  DiscoveredPrinterUSB,
};

export const ZebraLinkos: ZebraLinkosModule = ZebraLinkosModule
  ? ZebraLinkosModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
