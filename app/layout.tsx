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
  title: "Caffe Bar 919 — elegantno utočište u srcu Gradiške",
  description:
    "Kafa po danu, ritam po noći. Caffe Bar 919 — topao, elegantan ambijent, posebna ponuda pića i događaji uživo. Adresa, radno vrijeme, rezervacije.",
  generator: "v0.app",
  openGraph: {
    title: "Caffe Bar 919 — elegantno utočište u srcu Gradiške",
    description: "Kafa po danu, ritam po noći. Mjesto gdje se elegancija i dobra atmosfera sreću.",
    images: ["/images/entrance-exterior.png"],
  },
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
