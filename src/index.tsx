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

export function multiply(a: number, b: number): Promise<number> {
  return ZebraLinkOS.multiply(a, b);
}

export async function writeTCP(
  ipAddress: string,
  zpl: string
): Promise<boolean> {
  return await ZebraLinkOS.writeTCP(ipAddress, zpl);
}
