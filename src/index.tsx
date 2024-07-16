import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-zebra-linkos' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const ZebraLinkOS = NativeModules.ZebraLinkOS
  ? NativeModules.ZebraLinkOS
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
  return await ZebraLinkOS.writeTCP(ipAddress, zpl);
}

export async function scanNetwork(): Promise<string[]> {
  return await ZebraLinkOS.scanNetwork();
}

export async function scanBluetooth(): Promise<string[]> {
  return await ZebraLinkOS.scanBluetooth();
}
