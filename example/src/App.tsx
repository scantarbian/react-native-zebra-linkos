import React from 'react';
import { useState } from 'react';

import {
  StyleSheet,
  View,
  Text,
  TextInput,
  Button,
  ToastAndroid,
  PermissionsAndroid,
} from 'react-native';
import {
  writeTCP,
  scanNetwork,
  scanBluetooth,
} from 'react-native-zebra-linkos';
import * as Sentry from '@sentry/react-native';

Sentry.init({
  dsn: 'https://6799a339036c47199903fc49b124d592@o4505087032623104.ingest.sentry.io/4505087045664768',
});

const App = () => {
  const [ip, setIp] = useState('');
  const [devices, setDevices] = useState<string[]>([]);
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

  const sendTestTCP = async (targetIp = ip) => {
    console.log('Sending test TCP...');
    ToastAndroid.show(`Sending test TCP to ${targetIp}`, ToastAndroid.SHORT);

    try {
      const response = await writeTCP(targetIp, ZPL_TEST_STRING);
      ToastAndroid.show(`TCP sent to ${targetIp}`, ToastAndroid.SHORT);
      console.log(response);
    } catch (error) {
      console.error(error);
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
      setDevices(result);
      ToastAndroid.show(`Found ${result.length} devices`, ToastAndroid.SHORT);
      console.log('scanResult', result);
    } catch (error) {
      console.error('Network Scan Error', error);
      ToastAndroid.show(`Network Scan Error`, ToastAndroid.SHORT);
    } finally {
      setIsScanning(false);
    }
  };

  const scanBT = async () => {
    console.log('Scanning bluetooth...');
    ToastAndroid.show(`Starting bluetooth scan`, ToastAndroid.SHORT);
    // setIsScanning(true);
    try {
      const permissions = await requestPermissionsBluetooth();

      if (!permissions) {
        throw new Error('Permissions not granted');
      }

      const result = await scanBluetooth();
      // setDevices(result);
      ToastAndroid.show(`Found ${result.length} devices`, ToastAndroid.SHORT);
      console.log('scanResult', result);
    } catch (error) {
      console.error(error);
    } finally {
      setIsScanning(false);
    }
  };

  const displayDevices = () => {
    if (isScanning) {
      return <Text>Scanning...</Text>;
    }

    if (devices.length === 0) {
      return <Text>No devices found</Text>;
    }

    return devices.map((device, id) => {
      return (
        <View
          key={`device-${id}`}
          style={{
            flexDirection: 'row',
          }}
        >
          <Text key={`device-${id}-text`}>{device}</Text>
          <Button
            key={`device-${id}-button`}
            title="Send"
            onPress={() => sendTestTCP(device)}
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
      <TextInput
        placeholder="IP Address"
        inputMode="numeric"
        onChangeText={setIp}
        value={ip}
        style={styles.input}
      />
      <Button title="Test TCP" onPress={() => sendTestTCP()} />
      <Button title="Scan Network" onPress={scanTCP} />
      <View
        style={{
          flexDirection: 'column',
        }}
      >
        {displayDevices()}
      </View>
      <Button title="Scan Bluetooth" onPress={scanBT} />
    </View>
  );
};

export default Sentry.wrap(App);

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
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
  },
});
