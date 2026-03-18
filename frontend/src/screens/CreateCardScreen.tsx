import React, { useState } from "react";
import { Alert, Button, Text } from "react-native";

import { createCard } from "../api/banking";
import { useAuth } from "../auth/AuthContext";
import { Screen } from "../components/Screen";

export function CreateCardScreen() {
  const { user } = useAuth();
  const [lastCard, setLastCard] = useState<string | null>(null);

  async function onCreateCard() {
    if (!user) {
      return;
    }

    try {
      const response = await createCard(user.accessToken);
      setLastCard(response.cardNumber);
    } catch {
      Alert.alert("Create card failed", "Request was rejected by backend.");
    }
  }

  return (
    <Screen>
      <Text>Create a new virtual card for current account.</Text>
      <Button title="Create card" onPress={onCreateCard} />
      {lastCard ? <Text>{`Last created card: ${lastCard}`}</Text> : null}
    </Screen>
  );
}
