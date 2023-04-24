import React from 'react';
import { useState, useEffect } from 'react';

import {
  StyleSheet,
  View,
  Text,
  TextInput,
  Button,
  ToastAndroid,
  PermissionsAndroid,
} from 'react-native';
import { multiply, writeTCP, scanNetwork } from 'react-native-zebra-linkos';

export default function App() {
  const [ip, setIp] = useState('');
  const [result, setResult] = useState<number | undefined>();
  const [devices, setDevices] = useState<string[]>([]);
  const [isScanning, setIsScanning] = useState(false);

  const ZPL_TEST_STRING =
    '^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ';

  useEffect(() => {
    multiply(3, 7).then(setResult);
  }, []);

  const requestPermissions = async () => {
    try {
      const granted = await PermissionsAndroid.requestMultiple([
        'android.permission.NEARBY_WIFI_DEVICES',
        'android.permission.ACCESS_FINE_LOCATION',
      ]);

      if (
        granted['android.permission.ACCESS_FINE_LOCATION'] ===
          PermissionsAndroid.RESULTS.GRANTED &&
        granted['android.permission.NEARBY_WIFI_DEVICES'] ===
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

  const scan = async () => {
    console.log('Scanning network...');
    ToastAndroid.show(`Starting network scan`, ToastAndroid.SHORT);
    setIsScanning(true);
    try {
      const permissions = await requestPermissions();

      if (!permissions) {
        throw new Error('Permissions not granted');
      }

      const result = await scanNetwork();
      setDevices(result);
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
          style={{
            flexDirection: 'row',
          }}
        >
          <Text key={id}>{device}</Text>
          <Button title="Send" onPress={() => sendTestTCP(device)} />
        </View>
      );
    });
  };

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <TextInput
        placeholder="IP Address"
        inputMode="numeric"
        onChangeText={setIp}
        value={ip}
        style={styles.input}
      />
      <Button title="Test TCP" onPress={() => sendTestTCP()} />
      <Button title="Scan Network" onPress={scan} />
      <View
        style={{
          flexDirection: 'column',
        }}
      >
        {displayDevices()}
      </View>
    </View>
  );
}

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
