import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  writeTCP(ipAddress: string, zpl: string): Promise<boolean>;
  scanNetwork(): Promise<string[]>;
  scanBluetooth(): Promise<string[]>;
  scanBluetoothLE(): Promise<string[]>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('ZebraLinkos');
