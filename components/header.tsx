"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Menu, X } from "lucide-react"

export function Header() {
  const [isScrolled, setIsScrolled] = useState(false)
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20)
    }
    window.addEventListener("scroll", handleScroll)
    return () => window.removeEventListener("scroll", handleScroll)
  }, [])

  const scrollToSection = (id: string) => {
    const element = document.getElementById(id)
    if (element) {
      element.scrollIntoView({ behavior: "smooth" })
      setIsMobileMenuOpen(false)
    }
  }

  return (
    <header
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        isScrolled ? "bg-[#191919]/95 backdrop-blur-md border-b border-white/10" : "bg-transparent"
      }`}
    >
      <div className="container mx-auto px-4 py-4 flex items-center justify-between">
        <button onClick={() => scrollToSection("hero")} className="flex items-center gap-3 group">
          <div className="w-12 h-12 bg-[#D3B574] rounded-full flex items-center justify-center">
            <span className="font-[family-name:var(--font-cinzel)] text-[#191919] font-bold text-lg">919</span>
          </div>
          <span className="font-[family-name:var(--font-cinzel)] text-[#F5F1E6] text-xl tracking-wide hidden sm:block">
            CAFFE BAR
          </span>
        </button>

        <nav className="hidden lg:flex items-center gap-8">
          {["Početna", "O nama", "Meni", "Događaji", "Galerija", "Kontakt"].map((item) => (
            <button
              key={item}
              onClick={() => scrollToSection(item.toLowerCase().replace(" ", "-"))}
              className="text-[#F5F1E6] hover:text-[#D3B574] transition-colors text-sm tracking-wide"
            >
              {item}
            </button>
          ))}
        </nav>

        <div className="flex items-center gap-4">
          <Button
            onClick={() => scrollToSection("kontakt")}
            className="bg-[#D3B574] text-[#191919] hover:bg-[#D3B574]/90 font-medium hidden sm:flex"
          >
            Rezerviši sto
          </Button>

          <button onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)} className="lg:hidden text-[#F5F1E6]">
            {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>

      {isMobileMenuOpen && (
        <div className="lg:hidden bg-[#1F1F1F] border-t border-white/10">
          <nav className="container mx-auto px-4 py-6 flex flex-col gap-4">
            {["Početna", "O nama", "Meni", "Događaji", "Galerija", "Kontakt"].map((item) => (
              <button
                key={item}
                onClick={() => scrollToSection(item.toLowerCase().replace(" ", "-"))}
                className="text-[#F5F1E6] hover:text-[#D3B574] transition-colors text-left py-2"
              >
                {item}
              </button>
            ))}
            <Button
              onClick={() => scrollToSection("kontakt")}
              className="bg-[#D3B574] text-[#191919] hover:bg-[#D3B574]/90 font-medium w-full"
            >
              Rezerviši sto
            </Button>
          </nav>
        </div>
      )}
    </header>
  )
}
