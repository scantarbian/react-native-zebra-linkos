const ZebraLinkos = require('./NativeZebraLinkos').default;

export function multiply(a: number, b: number): number {
  return ZebraLinkos.multiply(a, b);
}
