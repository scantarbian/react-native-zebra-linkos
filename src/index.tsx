const ZebraLinkos = require('./NativeZebraLinkos').default;

export async function writeTCP(
  ipAddress: string,
  zpl: string
): Promise<boolean> {
  return await ZebraLinkos.writeTCP(ipAddress, zpl);
}

export async function scanNetwork(): Promise<string[]> {
  return await ZebraLinkos.scanNetwork();
}

export async function scanBluetooth(): Promise<string[]> {
  return await ZebraLinkos.scanBluetooth();
}

export async function scanBluetoothLE(): Promise<string[]> {
  return await ZebraLinkos.scanBluetoothLE();
}
