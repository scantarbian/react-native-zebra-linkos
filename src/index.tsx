const ZebraLinkos = require('./NativeZebraLinkos').default;

export function multiply(a: number, b: number): number {
  return ZebraLinkos.multiply(a, b);
}

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
