"use client"

export function Footer() {
  const scrollToSection = (id: string) => {
    const element = document.getElementById(id)
    if (element) {
      element.scrollIntoView({ behavior: "smooth" })
    }
  }

  return (
    <footer className="bg-[#1F1F1F] border-t border-white/10 py-12">
      <div className="container mx-auto px-4">
        <div className="grid md:grid-cols-3 gap-8 mb-8">
          <div>
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 bg-[#D3B574] rounded-full flex items-center justify-center">
                <span className="font-[family-name:var(--font-cinzel)] text-[#191919] font-bold">919</span>
              </div>
              <span className="font-[family-name:var(--font-cinzel)] text-[#F5F1E6] text-lg tracking-wide">
                CAFFE BAR
              </span>
            </div>
            <p className="text-[#A8A8A8] text-sm leading-relaxed">
              Elegantno utočište u srcu grada. Kafa po danu, ritam po noći.
            </p>
          </div>

          <div>
            <h4 className="text-[#F5F1E6] font-medium mb-4">Brzi linkovi</h4>
            <nav className="flex flex-col gap-2">
              {["Početna", "O nama", "Meni", "Događaji", "Galerija", "Kontakt"].map((item) => (
                <button
                  key={item}
                  onClick={() => scrollToSection(item.toLowerCase().replace(" ", "-"))}
                  className="text-[#A8A8A8] hover:text-[#D3B574] transition-colors text-left text-sm"
                >
                  {item}
                </button>
              ))}
            </nav>
          </div>

          <div>
            <h4 className="text-[#F5F1E6] font-medium mb-4">Radno vrijeme</h4>
            <div className="text-[#A8A8A8] text-sm space-y-2">
              <p>Ponedjeljak - Nedjelja</p>
              <p className="text-[#D3B574]">08:00 - 01:00</p>
            </div>
          </div>
        </div>

        <div className="pt-8 border-t border-[#D3B574]/30">
          <p className="text-center text-[#A8A8A8] text-sm">© 2025 Caffe Bar 919. Sva prava zadržana.</p>
        </div>
      </div>
    </footer>
  )
}
