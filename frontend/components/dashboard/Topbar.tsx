import React from "react";
import {
  SafeAreaView,
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
} from "react-native";
import { useRouter } from "expo-router";
import { useUser } from "@/packages/context/UserContext";

interface TopbarProps {
  sidebarWidth?: number;
}

export default function Topbar({ sidebarWidth = 256 }: TopbarProps) {
  const { logout, user } = useUser();
  const router = useRouter();

  const handleLogout = () => {
    logout();
    router.replace("/login");
  };

  const handleAdmin = () => {
    router.push("/admin");
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={[styles.container, { paddingLeft: 16, paddingRight: 16 }]}>
        <View style={styles.logoContainer}>
          <Text style={styles.userText}>
            {user?.name} ({user?.role})
          </Text>

          {user?.role?.toLowerCase() === "admin" && (
            <TouchableOpacity
              onPress={handleAdmin}
              style={styles.adminButton}
            >
              <Text style={styles.buttonText}>Admin</Text>
            </TouchableOpacity>
          )}

          <TouchableOpacity
            onPress={handleLogout}
            style={styles.logoutButton}
          >
            <Text style={styles.buttonText}>Logout</Text>
          </TouchableOpacity>
        </View>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    backgroundColor: "#fff",
  },
  container: {
    height: 56,
    flexDirection: "row",
    alignItems: "center",
    borderBottomWidth: 1,
    borderBottomColor: "#E5E7EB",
    width: "100%",
    zIndex: 30,
  },
  logoContainer: {
    marginLeft: "auto",
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  userText: {
    fontSize: 14,
    color: "#374151",
  },
  adminButton: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: "#2563EB",
    borderRadius: 4,
  },
  logoutButton: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: "#DC2626",
    borderRadius: 4,
  },
  buttonText: {
    color: "#fff",
    fontSize: 14,
    fontWeight: "500",
  },
});