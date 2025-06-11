import type {
  DiscoveredPrinter,
  DiscoveredPrinterBluetooth,
  DiscoveredPrinterBluetoothLe,
  DiscoveredPrinterNetwork,
  DiscoveredPrinterUSB,
} from './@types/index';

import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-zebra-linkos' doesn't seem to be linked. Make sure:\n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

interface ZebraLinkosNativeModule {
  writeTCP(ipAddress: string, zpl: string): Promise<boolean>;
  scanNetwork(): Promise<DiscoveredPrinterNetwork[]>;
  scanBluetooth(): Promise<DiscoveredPrinterBluetooth[]>;
  scanBluetoothLE(): Promise<DiscoveredPrinterBluetoothLe[]>;
  scanUSB(): Promise<DiscoveredPrinterUSB[]>;
  writeBLE(macAddress: string, zpl: string): Promise<boolean>;
  writeBTInsecure(macAddress: string, zpl: string): Promise<boolean>;
  writeUSB(deviceId: string, zpl: string): Promise<boolean>;
  checkUSBPermission(deviceId: string): Promise<boolean>;
}

// @ts-expect-error
const isTurboModuleEnabled = global.__turboModuleProxy != null;

const getZebraLinkosModule = (): ZebraLinkosNativeModule => {
  if (isTurboModuleEnabled) {
    return require('./NativeZebraLinkos').default;
  }

  if (NativeModules.ZebraLinkos) {
    return NativeModules.ZebraLinkos as ZebraLinkosNativeModule;
  }

  return new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    }
  ) as ZebraLinkosNativeModule;
};

export const ZebraLinkos = getZebraLinkosModule();

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

export type {
  DiscoveredPrinter,
  DiscoveredPrinterBluetooth,
  DiscoveredPrinterBluetoothLe,
  DiscoveredPrinterNetwork,
  DiscoveredPrinterUSB,
};
