"use client"

import { useState, useEffect } from "react"
import Image from "next/image"

export function LoadingScreen() {
  const [isVisible, setIsVisible] = useState(true)
  const [isAnimating, setIsAnimating] = useState(false)
  const [showContent, setShowContent] = useState(false)

  useEffect(() => {
    // Fade in content after initial mount
    const contentTimer = setTimeout(() => {
      setShowContent(true)
    }, 100)

    // Start fade out animation after 3 seconds
    const fadeTimer = setTimeout(() => {
      setIsAnimating(true)
      // Remove from DOM after fade completes (1000ms for smoother fade)
      setTimeout(() => {
        setIsVisible(false)
      }, 1000)
    }, 3000)

    return () => {
      clearTimeout(contentTimer)
      clearTimeout(fadeTimer)
    }
  }, [])

  if (!isVisible) return null

  return (
    <div
      className={`fixed inset-0 z-[9999] bg-[#191919] transition-opacity duration-1000 ease-out ${
        isAnimating ? "opacity-0" : "opacity-100"
      }`}
    >
      {/* Background Image with Overlay */}
      <div className="absolute inset-0">
        <div className="relative w-full h-full">
          <Image
            src="/images/loadingImage.webp"
            alt="Caffe Bar 919 Entrance"
            fill
            className="object-cover"
            priority
            quality={95}
          />
          {/* Dark gradient overlay */}
          <div className="absolute inset-0 bg-gradient-to-b from-[#191919]/70 via-[#191919]/60 to-[#191919]/90" />
        </div>
      </div>

      {/* Content */}
      <div className="relative z-10 h-full flex flex-col items-center justify-center px-4">
        <div
          className={`text-center transition-all duration-700 ${
            showContent ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
          }`}
        >
          {/* Main Welcome Text */}
          <h1 className="font-[family-name:var(--font-cinzel)] text-4xl sm:text-5xl md:text-6xl lg:text-7xl text-[#F5F1E6] mb-8 tracking-wider leading-tight">
            Dobrodošli
          </h1>

          {/* Gold Separator Line */}
          <div className="w-24 h-[1px] bg-[#D3B574] mx-auto mb-8" />

          {/* Logo/Brand */}
          <h2 className="font-[family-name:var(--font-cinzel)] text-3xl sm:text-4xl md:text-5xl text-[#D3B574] mb-4 tracking-wide">
            —919—
          </h2>
          <p className="font-[family-name:var(--font-cinzel)] text-lg sm:text-xl md:text-2xl text-[#F5F1E6] tracking-wider">
            CAFFE BAR
          </p>
        </div>
      </div>
    </div>
  )
}