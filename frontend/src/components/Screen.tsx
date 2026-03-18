import React from "react";
import { SafeAreaView, StyleSheet, View, type ViewProps } from "react-native";

export function Screen(props: ViewProps) {
  return (
    <SafeAreaView style={styles.safeArea}>
      <View {...props} style={[styles.content, props.style]} />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: "#ffffff"
  },
  content: {
    flex: 1,
    padding: 16,
    gap: 12
  }
});
