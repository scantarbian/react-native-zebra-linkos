import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-zebra-linkos' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const ZebraLinkos = NativeModules.ZebraLinkos
  ? NativeModules.ZebraLinkos
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function multiply(a: number, b: number): Promise<number> {
  return ZebraLinkos.multiply(a, b);
}
