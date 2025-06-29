import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
import type {
  DiscoveredPrinterBluetooth,
  DiscoveredPrinterBluetoothLe,
  DiscoveredPrinterNetwork,
  DiscoveredPrinterUSB,
  PrinterStatus,
} from './@types/index';
export interface Spec extends TurboModule {
  writeTCP(ipAddress: string, zpl: string): Promise<boolean>;
  writeBLE(macAddress: string, zpl: string): Promise<boolean>;
  writeBTInsecure(macAddress: string, zpl: string): Promise<boolean>;
  writeUSB(deviceId: string, zpl: string): Promise<boolean>;
  checkUSBPermission(deviceId: string): Promise<boolean>;
  scanNetwork(): Promise<DiscoveredPrinterNetwork[]>;
  scanBluetooth(): Promise<DiscoveredPrinterBluetooth[]>;
  scanBluetoothLE(): Promise<DiscoveredPrinterBluetoothLe[]>;
  scanUSB(): Promise<DiscoveredPrinterUSB[]>;
  checkTCPPrinterStatus(ipAddress: string): Promise<PrinterStatus>;
  checkBLEPrinterStatus(macAddress: string): Promise<PrinterStatus>;
  checkBTPrinterStatus(macAddress: string): Promise<PrinterStatus>;
  checkUSBPrinterStatus(deviceId: string): Promise<PrinterStatus>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('ZebraLinkos');
