import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  writeTCP(ipAddress: string, zpl: string): Promise<boolean>;
  scanNetwork(): Promise<string[]>;
  scanBluetooth(): Promise<string[]>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('ZebraLinkos');
