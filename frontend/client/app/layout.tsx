import type { Metadata } from "next";
import { headers } from "next/headers";
import { Header } from "./components/Header";
import "./globals.css";

export async function generateMetadata(): Promise<Metadata> {
  const requestHeaders = await headers();
  const host = requestHeaders.get("x-forwarded-host") ?? requestHeaders.get("host");
  const protocol = requestHeaders.get("x-forwarded-proto") ?? "https";
  const metadataBase = host ? new URL(`${protocol}://${host}`) : undefined;
  const title = "梅问｜把知识练成直觉";
  const description = "面向持续学习者的精选题库、练习进度与签到计划。";

  return {
    metadataBase,
    title,
    description,
    openGraph: {
      title,
      description,
      type: "website",
      locale: "zh_CN",
      images: [{ url: "/og.png", width: 1200, height: 630, alt: "梅问学习题库" }],
    },
    twitter: {
      card: "summary_large_image",
      title,
      description,
      images: ["/og.png"],
    },
  };
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <body>
        <Header />
        {children}
      </body>
    </html>
  );
}
