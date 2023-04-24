import React from 'react';
import { useState, useEffect } from 'react';

import { StyleSheet, View, Text, TextInput, Button } from 'react-native';
import { multiply, writeTCP, scanNetwork } from 'react-native-zebra-linkos';

export default function App() {
  const [ip, setIp] = useState('');
  const [result, setResult] = useState<number | undefined>();

  const ZPL_TEST_STRING =
    '^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ';

  useEffect(() => {
    multiply(3, 7).then(setResult);
  }, []);

  const sendTestTCP = async () => {
    console.log('Sending test TCP...');

    try {
      const response = await writeTCP(ip, ZPL_TEST_STRING);
      console.log(response);
    } catch (error) {
      console.error(error);
    }
  };

  const scan = async () => {
    console.log('Scanning network...');
    try {
      const response = await scanNetwork();
      console.log(response);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <TextInput
        placeholder="IP Address"
        onChangeText={setIp}
        value={ip}
        style={styles.input}
      />
      <Button title="Test TCP" onPress={sendTestTCP} />
      <Button title="Scan Network" onPress={scan} />
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
