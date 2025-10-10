"use client"

import { Button } from "@/components/ui/button"
import { ArrowDown } from "lucide-react"
import Image from "next/image"

export function Hero() {
  const scrollToSection = (id: string) => {
    const element = document.getElementById(id)
    if (element) {
      element.scrollIntoView({ behavior: "smooth" })
    }
  }

  return (
    <section id="početna" className="relative h-screen flex items-center justify-center overflow-hidden">
      <div className="absolute inset-0 z-0">
        <Image src="/images/hero-bar.png" alt="Caffe Bar 919 interior" fill className="object-cover" priority />
        <div className="absolute inset-0 bg-gradient-to-b from-[#191919]/70 via-[#191919]/50 to-[#191919]" />
      </div>

      <div className="relative z-10 container mx-auto px-4 text-center">
        <h1 className="font-[family-name:var(--font-cinzel)] text-5xl md:text-7xl lg:text-8xl text-[#F5F1E6] mb-6 tracking-wide leading-tight">
          —919—
        </h1>
        <div className="w-24 h-[1px] bg-[#D3B574] mx-auto mb-6" />
        <h2 className="font-[family-name:var(--font-cinzel)] text-2xl md:text-3xl text-[#D3B574] mb-8 tracking-wider">
          CAFFE BAR
        </h2>
        <p className="text-[#F5F1E6] text-lg md:text-xl max-w-2xl mx-auto mb-12 leading-relaxed">
          Mjesto gdje se elegancija i dobra atmosfera sreću.
          <br />
          Kafa po danu, ritam po noći.
        </p>

        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
          <Button
            onClick={() => scrollToSection("kontakt")}
            className="bg-[#D3B574] text-[#191919] hover:bg-[#D3B574]/90 font-medium px-8 py-6 text-lg"
          >
            Rezerviši sto
          </Button>
          <Button
            onClick={() => scrollToSection("meni")}
            variant="outline"
            className="border-[#D3B574] text-[#D3B574] hover:bg-[#D3B574]/10 px-8 py-6 text-lg"
          >
            Pogledaj meni
          </Button>
        </div>
      </div>

      <button
        onClick={() => scrollToSection("o-nama")}
        className="absolute bottom-8 left-1/2 -translate-x-1/2 z-10 text-[#D3B574] animate-bounce"
      >
        <ArrowDown size={32} />
      </button>
    </section>
  )
}
