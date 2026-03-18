import React from "react";
import { Button, Text } from "react-native";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";

import { useAuth } from "../auth/AuthContext";
import { Screen } from "../components/Screen";
import type { RootStackParamList } from "../navigation/RootNavigator";

type Props = NativeStackScreenProps<RootStackParamList, "Home">;

export function HomeScreen({ navigation }: Props) {
  const { user, signOut } = useAuth();

  return (
    <Screen>
      <Text>{`Hello, ${user?.firstName ?? "user"}`}</Text>
      <Text>{user?.email}</Text>
      <Button title="Create card" onPress={() => navigation.navigate("CreateCard")} />
      <Button title="Transfer" onPress={() => navigation.navigate("Transfer")} />
      <Button title="Logout" onPress={() => void signOut()} />
    </Screen>
  );
}
