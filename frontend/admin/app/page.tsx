import type { Metadata } from "next";
import { UserManagement } from "./components/UserManagement";

export const metadata: Metadata = {
  title: "Users",
  description: "Search and manage Prunus Mume user accounts.",
};

export default function Home() {
  return <UserManagement />;
}
