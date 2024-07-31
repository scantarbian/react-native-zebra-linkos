import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
import type {
  DiscoveredPrinterBluetooth,
  DiscoveredPrinterBluetoothLe,
  DiscoveredPrinterNetwork,
} from './@types/index';
export interface Spec extends TurboModule {
  writeTCP(ipAddress: string, zpl: string): Promise<boolean>;
  writeBLE(macAddress: string, zpl: string): Promise<boolean>;
  writeBTInsecure(macAddress: string, zpl: string): Promise<boolean>;
  scanNetwork(): Promise<DiscoveredPrinterNetwork[]>;
  scanBluetooth(): Promise<DiscoveredPrinterBluetooth[]>;
  scanBluetoothLE(): Promise<DiscoveredPrinterBluetoothLe[]>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('ZebraLinkos');
