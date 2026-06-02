import { router, usePathname } from "expo-router";
import React, { useEffect, useRef, useState } from "react";
import {
  Animated,
  Easing,
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  View,
  useWindowDimensions,
} from "react-native";
import Feather from "react-native-vector-icons/Feather";

interface User {
  name: string;
  avatar: string;
}

interface SidebarProps {
  collapsed: boolean;
  setCollapsed: (value: boolean) => void;
}

type DashboardRoute =
  | "/"
  | "/(dashboard)/historico"
  | "/(dashboard)/notificacoes"
  | "/(dashboard)/pendentes"
  | "/(dashboard)/em-andamento"
  | "/(dashboard)/concluidas"
  | "/(dashboard)/relatorios";

export default function Sidebar({ collapsed, setCollapsed }: SidebarProps) {
  const [user, setUser] = useState<User | null>(null);

  const pathname = usePathname();
  const { width, height } = useWindowDimensions();
  const isMobile = width < 768;

  const SIDEBAR_WIDTH = 256;
  const COLLAPSED_WIDTH = 56;

  const translateX = useRef(
    new Animated.Value(isMobile && collapsed ? -width : 0)
  ).current;

  const widthAnim = useRef(
    new Animated.Value(!isMobile && collapsed ? COLLAPSED_WIDTH : SIDEBAR_WIDTH)
  ).current;

  const textOpacity = useRef(new Animated.Value(!collapsed ? 1 : 0)).current;

  useEffect(() => {
    if (isMobile) {
      Animated.timing(translateX, {
        toValue: collapsed ? -width : 0,
        duration: 350,
        easing: Easing.out(Easing.exp),
        useNativeDriver: true,
      }).start();
    } else {
      Animated.timing(widthAnim, {
        toValue: collapsed ? COLLAPSED_WIDTH : SIDEBAR_WIDTH,
        duration: 350,
        easing: Easing.out(Easing.exp),
        useNativeDriver: false,
      }).start();

      Animated.timing(textOpacity, {
        toValue: collapsed ? 0 : 1,
        duration: 250,
        easing: Easing.out(Easing.exp),
        useNativeDriver: true,
      }).start();
    }
  }, [collapsed, isMobile, width]);

  useEffect(() => {
    const timer = setTimeout(() => {
      setUser({
        name: "Jane Doe",
        avatar: "https://i.pravatar.cc/150?img=32",
      });
    }, 400);

    return () => clearTimeout(timer);
  }, []);

  const navItem = (path: DashboardRoute, label: string, icon: string) => {
    const active = pathname === path;

    return (
      <Pressable
        key={path}
        onPress={() => {
          router.push(path);
          if (isMobile) setCollapsed(true);
        }}
        style={[
          styles.navItem,
          active && styles.navItemActive,
          !isMobile && collapsed && styles.navItemCollapsed,
        ]}
      >
        <Feather
          name={icon}
          size={20}
          color={active ? "#fff" : "#4B5563"}
        />
        {!collapsed && (
          <Animated.Text
            style={[
              styles.navText,
              active && styles.navTextActive,
              { opacity: textOpacity },
            ]}
          >
            {label}
          </Animated.Text>
        )}
      </Pressable>
    );
  };

  return (
    <>
      {isMobile && !collapsed && (
        <Pressable style={styles.overlay} onPress={() => setCollapsed(true)} />
      )}

      {isMobile && (
        <Pressable
          onPress={() => setCollapsed(!collapsed)}
          style={styles.mobileHamburger}
        >
          <Feather name="menu" size={28} color="#111827" />
        </Pressable>
      )}

      <Animated.View
        style={[
          styles.sidebar,
          {
            width: isMobile ? width : widthAnim,
            height,
            transform: isMobile ? [{ translateX }] : undefined,
          },
        ]}
      >
        {!isMobile && (
          <Pressable
            onPress={() => setCollapsed(!collapsed)}
            style={styles.desktopToggle}
          >
            <Feather name="columns" size={20} color="#4B5563" />
          </Pressable>
        )}

        <ScrollView contentContainerStyle={styles.scroll}>
          {navItem("/", "Home", "home")}

          <View style={styles.divider} />

          {navItem("/(dashboard)/historico", "Histórico", "clock")}
          {navItem("/(dashboard)/notificacoes", "Notificações", "bell")}
          {navItem("/(dashboard)/relatorios", "Relatórios", "bar-chart-2")}

          <View style={styles.divider} />

          {!collapsed && (
            <Animated.Text
              style={[styles.sectionLabel, { opacity: textOpacity }]}
            >
              Solicitações
            </Animated.Text>
          )}

          {navItem("/(dashboard)/pendentes", "Pendentes", "clock")}
          {navItem("/(dashboard)/em-andamento", "Em andamento", "loader")}
          {navItem("/(dashboard)/concluidas", "Concluídas", "check-circle")}
        </ScrollView>

        {user && (
          <View
            style={[
              styles.footer,
              collapsed && !isMobile && styles.footerCollapsed,
            ]}
          >
            <Image source={{ uri: user.avatar }} style={styles.avatar} />
            {!collapsed && (
              <Animated.Text
                style={[styles.userName, { opacity: textOpacity }]}
              >
                {user.name}
              </Animated.Text>
            )}
          </View>
        )}
      </Animated.View>
    </>
  );
}

const styles = StyleSheet.create({
  sidebar: {
    position: "absolute",
    top: 0,
    left: 0,
    backgroundColor: "#fff",
    borderRightWidth: 1,
    borderRightColor: "#E5E7EB",
    zIndex: 40,
  },
  overlay: {
    position: "absolute",
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: "rgba(0,0,0,0.35)",
    zIndex: 30,
  },
  mobileHamburger: {
    position: "absolute",
    top: 16,
    left: 16,
    zIndex: 50,
  },
  desktopToggle: {
    position: "absolute",
    top: 16,
    right: 16,
    zIndex: 10,
  },
  scroll: {
    paddingTop: 80,
    paddingHorizontal: 8,
  },
  navItem: {
    flexDirection: "row",
    alignItems: "center",
    height: 48,
    paddingHorizontal: 16,
    borderRadius: 8,
    marginVertical: 4,
  },
  navItemCollapsed: {
    justifyContent: "center",
  },
  navItemActive: {
    backgroundColor: "#DC2626",
  },
  navText: {
    marginLeft: 12,
    fontSize: 14,
    color: "#4B5563",
  },
  navTextActive: {
    color: "#fff",
    fontWeight: "600",
  },
  divider: {
    height: 1,
    backgroundColor: "#F3F4F6",
    marginVertical: 16,
  },
  sectionLabel: {
    fontSize: 10,
    fontWeight: "600",
    color: "#9CA3AF",
    marginLeft: 16,
    marginBottom: 8,
  },
  footer: {
    flexDirection: "row",
    alignItems: "center",
    padding: 16,
    borderTopWidth: 1,
    borderTopColor: "#E5E7EB",
  },
  footerCollapsed: {
    justifyContent: "center",
  },
  avatar: {
    width: 32,
    height: 32,
    borderRadius: 16,
  },
  userName: {
    marginLeft: 12,
    fontSize: 14,
    fontWeight: "500",
  },
});