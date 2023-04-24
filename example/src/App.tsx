import React from 'react';
import { useState, useEffect } from 'react';

import { StyleSheet, View, Text, TextInput, Button } from 'react-native';
import { multiply, writeTCP, scanNetwork } from 'react-native-zebra-linkos';

export default function App() {
  const [ip, setIp] = useState('');
  const [result, setResult] = useState<number | undefined>();
  const [devices, setDevices] = useState<string[]>([]);

  const ZPL_TEST_STRING =
    '^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ';

  useEffect(() => {
    multiply(3, 7).then(setResult);
  }, []);

  const sendTestTCP = async (targetIp = ip) => {
    console.log('Sending test TCP...');

    try {
      const response = await writeTCP(targetIp, ZPL_TEST_STRING);
      console.log(response);
    } catch (error) {
      console.error(error);
    }
  };

  const scan = async () => {
    console.log('Scanning network...');
    try {
      const result = await scanNetwork();
      setDevices(result);
      console.log('scanResult', result);
    } catch (error) {
      console.error(error);
    }
  };

  const displayDevices = () => {
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
