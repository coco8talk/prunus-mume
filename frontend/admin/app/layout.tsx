import type { Metadata } from "next";
import { headers } from "next/headers";
import { AuthProvider } from "./components/AuthProvider";
import "./globals.css";

export async function generateMetadata(): Promise<Metadata> {
  const requestHeaders = await headers();
  const host =
    requestHeaders.get("x-forwarded-host") ??
    requestHeaders.get("host") ??
    "localhost:3000";
  const protocol =
    requestHeaders.get("x-forwarded-proto") ??
    (host.startsWith("localhost") ? "http" : "https");
  const socialImage = `${protocol}://${host}/og-questions.png`;

  return {
    title: {
      default: "Prunus Mume Admin",
      template: "%s · Prunus Mume Admin",
    },
    description:
      "Internal operations console for the Prunus Mume learning platform.",
    openGraph: {
      title: "Prunus Mume Admin Console",
      description: "Questions, in focus.",
      type: "website",
      images: [{ url: socialImage, width: 1536, height: 1024 }],
    },
    twitter: {
      card: "summary_large_image",
      title: "Prunus Mume Admin Console",
      description: "Questions, in focus.",
      images: [socialImage],
    },
  };
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
