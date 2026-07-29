import type { Metadata } from "next";
import { LoginScreen } from "../components/LoginScreen";

export const metadata: Metadata = {
  title: "Sign in",
  description: "Administrator sign-in for the Prunus Mume operations console.",
};

export default function LoginPage() {
  return <LoginScreen />;
}
