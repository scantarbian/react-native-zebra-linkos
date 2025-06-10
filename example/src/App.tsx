import { useState } from 'react';

import {
  StyleSheet,
  View,
  Text,
  Button,
  ToastAndroid,
  PermissionsAndroid,
} from 'react-native';
import {
  writeTCP,
  writeBLE,
  writeBTInsecure,
  writeUSB,
  scanNetwork,
  scanBluetooth,
  scanBluetoothLE,
  scanUSB,
} from 'react-native-zebra-linkos';
import type {
  DiscoveredPrinter,
  DiscoveredPrinterBluetooth,
  DiscoveredPrinterBluetoothLe,
} from 'react-native-zebra-linkos';

const App = () => {
  const [devices, setDevices] = useState<DiscoveredPrinter[]>([]);
  const [isScanning, setIsScanning] = useState(false);
  const [error, setError] = useState<string | undefined>();

  const ZPL_TEST_STRING =
    '^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ';

  const requestPermissions = async () => {
    try {
      const locationGranted = await PermissionsAndroid.requestMultiple([
        'android.permission.ACCESS_FINE_LOCATION',
        'android.permission.ACCESS_COARSE_LOCATION',
      ]);

      if (
        locationGranted['android.permission.ACCESS_FINE_LOCATION'] ===
          (PermissionsAndroid.RESULTS.GRANTED ||
            PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN) &&
        locationGranted['android.permission.ACCESS_COARSE_LOCATION'] ===
          (PermissionsAndroid.RESULTS.GRANTED ||
            PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN)
      ) {
        return true;
      } else {
        return false;
      }
    } catch (err) {
      console.error(err);
      return false;
    }
  };

  const requestPermissionsBluetooth = async () => {
    try {
      const granted = await PermissionsAndroid.requestMultiple([
        'android.permission.BLUETOOTH_SCAN',
        'android.permission.BLUETOOTH_CONNECT',
      ]);

      if (
        granted['android.permission.BLUETOOTH_SCAN'] ===
          PermissionsAndroid.RESULTS.GRANTED &&
        granted['android.permission.BLUETOOTH_CONNECT'] ===
          PermissionsAndroid.RESULTS.GRANTED
      ) {
        return true;
      } else {
        return false;
      }
    } catch (err) {
      console.error(err);
      return false;
    }
  };

  const sendTestTCP = async (targetIp: string) => {
    console.log('Sending test TCP...');
    ToastAndroid.show(`Sending test TCP to ${targetIp}`, ToastAndroid.SHORT);

    try {
      const response = await writeTCP(targetIp, ZPL_TEST_STRING);
      ToastAndroid.show(`TCP sent to ${targetIp}`, ToastAndroid.SHORT);
      console.log(response);
    } catch (err) {
      console.error(err);
    }
  };

  const sendTestBLE = async (targetMac: string) => {
    console.log('Sending test BLE...');
    ToastAndroid.show(`Sending test BLE to ${targetMac}`, ToastAndroid.SHORT);

    try {
      const response = await writeBLE(targetMac, ZPL_TEST_STRING);
      ToastAndroid.show(`BLE sent to ${targetMac}`, ToastAndroid.SHORT);
      console.log(response);
    } catch (err) {
      console.error(err);
    }
  };

  const sendTestBT = async (targetMac: string) => {
    console.log('Sending test BT...');
    ToastAndroid.show(`Sending test BT to ${targetMac}`, ToastAndroid.SHORT);

    try {
      const response = await writeBTInsecure(targetMac, ZPL_TEST_STRING);
      ToastAndroid.show(`BT sent to ${targetMac}`, ToastAndroid.SHORT);
      console.log(response);
    } catch (err) {
      console.error(err);
    }
  };

  const sendTestUSB = async (targetId: string) => {
    console.log('Sending test USB...');
    ToastAndroid.show(`Sending test USB to ${targetId}`, ToastAndroid.SHORT);
    try {
      const response = await writeUSB(targetId, ZPL_TEST_STRING);
      ToastAndroid.show(`USB sent to ${targetId}`, ToastAndroid.SHORT);
      console.log(response);
    } catch (err) {
      console.error(err);
    }
  };

  const scanTCP = async () => {
    setError(undefined);
    console.log('Scanning network...');
    ToastAndroid.show(`Starting network scan`, ToastAndroid.SHORT);

    try {
      const permissions = await requestPermissions();

      if (!permissions) {
        throw new Error('Permissions not granted');
      }

      setIsScanning(true);
      const result = await scanNetwork();
      console.log('scanResult', result);
      setDevices(result);
      ToastAndroid.show(`Found ${result.length} devices`, ToastAndroid.SHORT);
    } catch (err) {
      console.error('Network Scan Error', err);
      ToastAndroid.show(`Network Scan Error`, ToastAndroid.SHORT);
    } finally {
      setIsScanning(false);
    }
  };

  const scanBT = async () => {
    console.log('Scanning bluetooth...');
    ToastAndroid.show(`Starting BT scan`, ToastAndroid.SHORT);
    setIsScanning(true);
    try {
      const permissions = await requestPermissionsBluetooth();

      if (!permissions) {
        throw new Error('Permissions not granted');
      }

      const result = await scanBluetooth();
      console.log('scanResult', result);
      setDevices(result);
      ToastAndroid.show(`Found ${result.length} devices`, ToastAndroid.SHORT);
    } catch (err) {
      console.error(err);
    } finally {
      setIsScanning(false);
    }
  };

  const scanBTLE = async () => {
    console.log('Scanning bluetooth low energy...');
    ToastAndroid.show(`Starting BTLE scan`, ToastAndroid.SHORT);
    setIsScanning(true);
    try {
      const permissions = await requestPermissionsBluetooth();

      if (!permissions) {
        throw new Error('Permissions not granted');
      }

      const result = await scanBluetoothLE();
      console.log('scanResult', result);
      setDevices(result);
      ToastAndroid.show(`Found ${result.length} devices`, ToastAndroid.SHORT);
    } catch (err) {
      console.error(err);
    } finally {
      setIsScanning(false);
    }
  };

  const scanSerial = async () => {
    console.log('Scanning USB...');
    ToastAndroid.show(`Starting USB scan`, ToastAndroid.SHORT);
    setIsScanning(true);
    try {
      const result = await scanUSB();
      console.log('scanResult', result);
      setDevices(result);
      ToastAndroid.show(`Found ${result.length} devices`, ToastAndroid.SHORT);
    } catch (err) {
      console.error(err);
    } finally {
      setIsScanning(false);
    }
  };

  const displayDevices = () => {
    if (isScanning) {
      return (
        <Text
          style={{
            padding: 4,
            backgroundColor: 'red',
            color: 'white',
          }}
        >
          Scanning...
        </Text>
      );
    }

    if (!devices || devices.length === 0) {
      return (
        <Text
          style={{
            padding: 4,
            backgroundColor: 'red',
            color: 'white',
          }}
        >
          No devices found
        </Text>
      );
    }

    return devices.map((device, id) => {
      const name =
        (device as DiscoveredPrinterBluetooth | DiscoveredPrinterBluetoothLe)
          .friendlyName || device.address;

      const macAddress =
        (device as DiscoveredPrinterBluetooth | DiscoveredPrinterBluetoothLe)
          .address || '-';

      return (
        <View
          key={`device-${id}`}
          style={{
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
            borderColor: 'white',
            borderWidth: 1,
            padding: 8,
          }}
        >
          <View
            style={{
              flexDirection: 'row',
              columnGap: 8,
            }}
          >
            <Text
              key={`name-${id}-text`}
              style={{
                color: 'white',
              }}
            >
              {name}
            </Text>
            <Text
              key={`mac-${id}-text`}
              style={{
                color: 'white',
              }}
            >
              {macAddress}
            </Text>
          </View>
          <Button
            key={`device-${id}-button`}
            title="Send"
            onPress={() => {
              switch (device.origin) {
                case 'net': {
                  return sendTestTCP(device.address);
                }
                case 'bt': {
                  return sendTestBT(device.address);
                }
                case 'ble': {
                  return sendTestBLE(device.address);
                }
                case 'usb': {
                  return sendTestUSB(device.address);
                }
                default:
                  throw new Error('Undefined origin');
              }
            }}
          />
        </View>
      );
    });
  };

  return (
    <View style={styles.container}>
      {error !== undefined && (
        <Text
          style={{
            padding: 4,
            backgroundColor: 'red',
            color: 'white',
          }}
        >
          Error: {error}
        </Text>
      )}
      <Button title="Scan Network" onPress={scanTCP} />
      <Button title="Scan Bluetooth" onPress={scanBT} />
      <Button title="Scan Bluetooth LE" onPress={scanBTLE} />
      <Button title="Scan USB" onPress={scanSerial} />
      <View
        style={{
          flexDirection: 'column',
          width: '60%',
        }}
      >
        {displayDevices()}
      </View>
    </View>
  );
};

export default App;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    rowGap: 20,
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
  input: {
    height: 40,
    margin: 12,
    borderWidth: 1,
    padding: 10,
    borderColor: 'white',
  },
});
