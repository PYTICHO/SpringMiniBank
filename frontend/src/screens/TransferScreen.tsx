import React, { useState } from "react";
import { Alert, Button, Text, TextInput } from "react-native";

import { makeTransaction } from "../api/banking";
import { useAuth } from "../auth/AuthContext";
import { Screen } from "../components/Screen";

export function TransferScreen() {
  const { user } = useAuth();
  const [amount, setAmount] = useState("");
  const [to, setTo] = useState("");
  const [description, setDescription] = useState("");

  async function onSubmit() {
    if (!user) {
      return;
    }

    try {
      await makeTransaction(user.accessToken, {
        amount: Number(amount),
        currency: "RUB",
        type: "CARD_TO_CARD",
        description,
        to
      });

      Alert.alert("Success", "Transaction created.");
    } catch {
      Alert.alert("Transfer failed", "Check data and backend response.");
    }
  }

  return (
    <Screen>
      <Text>Amount</Text>
      <TextInput value={amount} onChangeText={setAmount} keyboardType="numeric" style={{ borderWidth: 1, padding: 12 }} />
      <Text>To card number</Text>
      <TextInput value={to} onChangeText={setTo} style={{ borderWidth: 1, padding: 12 }} />
      <Text>Description</Text>
      <TextInput value={description} onChangeText={setDescription} style={{ borderWidth: 1, padding: 12 }} />
      <Button title="Send transfer" onPress={onSubmit} />
    </Screen>
  );
}
