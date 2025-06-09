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

const ZebraLinkos = ZebraLinkosModule
  ? ZebraLinkosModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

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

export async function scanUSB(): Promise<DiscoveredPrinterUSB[]> {
  return await ZebraLinkos.scanUSB();
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

export async function writeUSB(
  deviceId: string,
  zpl: string
): Promise<boolean> {
  return await ZebraLinkos.writeUSB(deviceId, zpl);
}

export type {
  DiscoveredPrinter,
  DiscoveredPrinterBluetooth,
  DiscoveredPrinterBluetoothLe,
  DiscoveredPrinterNetwork,
};
