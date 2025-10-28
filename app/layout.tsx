import type React from "react"
import type { Metadata } from "next"
import { GeistSans } from "geist/font/sans"
import { GeistMono } from "geist/font/mono"
import { Cinzel } from "next/font/google"
import { Analytics } from "@vercel/analytics/next"
import "./globals.css"
import { Suspense } from "react"

const cinzel = Cinzel({
  subsets: ["latin"],
  variable: "--font-cinzel",
  display: "swap",
})

export const metadata: Metadata = {
  metadataBase: new URL("https://www.919bar.com"),
  title: "Caffe Bar 919 — elegantno utočište u srcu Gradiške",
  description:
    "Kafa po danu, ritam po noći. Caffe Bar 919 — topao, elegantan ambijent, posebna ponuda pića i događaji uživo. Adresa, radno vrijeme, rezervacije.",
  keywords: ["caffe bar", "gradiska", "kafa", "bar", "919", "caffe bar 919"],
  authors: [{ name: "Next Pixel", url: "https://www.nextpixel.dev" }],
  creator: "Next Pixel",
  openGraph: {
    type: "website",
    locale: "sr_RS",
    url: "https://www.919bar.com",
    siteName: "Caffe Bar 919",
    title: "Caffe Bar 919 — elegantno utočište u srcu Gradiške",
    description: "Kafa po danu, ritam po noći. Mjesto gdje se elegancija i dobra atmosfera sreću.",
    images: [
      {
        url: "/images/loadingImage.webp",
        width: 1200,
        height: 630,
        alt: "Caffe Bar 919 — ulaz u lokal",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "Caffe Bar 919 — elegantno utočište u srcu Gradiške",
    description: "Kafa po danu, ritam po noći. Mjesto gdje se elegancija i dobra atmosfera sreću.",
    images: ["/images/loadingImage.webp"],
  },
  robots: {
    index: true,
    follow: true,
  },
}

export const viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 5,
  themeColor: "#191919",
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="sr">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
      </head>
      <body className={`font-sans ${GeistSans.variable} ${GeistMono.variable} ${cinzel.variable}`}>
        <Suspense fallback={null}>{children}</Suspense>
        <Analytics />
      </body>
    </html>
  )
}
