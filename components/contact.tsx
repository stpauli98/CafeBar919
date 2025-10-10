import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { MapPin, Phone, Clock, Instagram, Facebook, Navigation } from "lucide-react"

export function Contact() {
  return (
    <section id="kontakt" className="py-24 bg-[#191919]">
      <div className="container mx-auto px-4">
        <div className="text-center mb-16">
          <h2 className="font-[family-name:var(--font-cinzel)] text-4xl md:text-5xl text-[#F5F1E6] mb-6 tracking-wide">
            Kontakt
          </h2>
          <div className="w-16 h-[1px] bg-[#D3B574] mx-auto mb-6" />
          <p className="text-[#A8A8A8] text-lg max-w-2xl mx-auto leading-relaxed">
            Posjetite nas ili nas kontaktirajte za rezervacije.
          </p>
        </div>

        <div className="grid lg:grid-cols-2 gap-8 max-w-5xl mx-auto">
          <Card className="bg-[#1F1F1F] border-white/10 p-8">
            <h3 className="font-[family-name:var(--font-cinzel)] text-2xl text-[#F5F1E6] mb-6 tracking-wide">
              Informacije
            </h3>

            <div className="space-y-6">
              <div className="flex items-start gap-4">
                <MapPin className="text-[#D3B574] mt-1 flex-shrink-0" size={20} />
                <div>
                  <p className="text-[#F5F1E6] font-medium mb-1">Adresa</p>
                  <p className="text-[#A8A8A8]">Centar grada, Gradiška</p>
                </div>
              </div>

              <div className="flex items-start gap-4">
                <Phone className="text-[#D3B574] mt-1 flex-shrink-0" size={20} />
                <div>
                  <p className="text-[#F5F1E6] font-medium mb-1">Telefon</p>
                  <a href="tel:+38765123456" className="text-[#A8A8A8] hover:text-[#D3B574] transition-colors">
                    +387 65 123 456
                  </a>
                </div>
              </div>

              <div className="flex items-start gap-4">
                <Clock className="text-[#D3B574] mt-1 flex-shrink-0" size={20} />
                <div>
                  <p className="text-[#F5F1E6] font-medium mb-1">Radno vrijeme</p>
                  <p className="text-[#A8A8A8]">Pon - Ned: 08:00 - 01:00</p>
                </div>
              </div>

              <div className="pt-4 border-t border-white/10">
                <p className="text-[#F5F1E6] font-medium mb-3">Društvene mreže</p>
                <div className="flex gap-4">
                  <a
                    href="https://instagram.com/caffe__919__bar"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-[#A8A8A8] hover:text-[#D3B574] transition-colors"
                  >
                    <Instagram size={24} />
                  </a>
                  <a href="#" className="text-[#A8A8A8] hover:text-[#D3B574] transition-colors">
                    <Facebook size={24} />
                  </a>
                </div>
              </div>
            </div>
          </Card>

          <Card className="bg-[#1F1F1F] border-white/10 p-8">
            <h3 className="font-[family-name:var(--font-cinzel)] text-2xl text-[#F5F1E6] mb-6 tracking-wide">
              Rezervacija
            </h3>

            <p className="text-[#A8A8A8] mb-6 leading-relaxed">
              Rezervišite svoj sto i osigurajte mjesto u našem elegantnom ambijentu. Kontaktirajte nas telefonom ili
              putem društvenih mreža.
            </p>

            <div className="space-y-4">
              <Button className="w-full bg-[#D3B574] text-[#191919] hover:bg-[#D3B574]/90 font-medium py-6" asChild>
                <a href="tel:+38765123456">
                  <Phone size={18} className="mr-2" />
                  Pozovi za rezervaciju
                </a>
              </Button>

              <Button
                variant="outline"
                className="w-full border-[#D3B574] text-[#D3B574] hover:bg-[#D3B574]/10 py-6 bg-transparent"
                asChild
              >
                <a href="https://www.google.com/maps" target="_blank" rel="noopener noreferrer">
                  <Navigation size={18} className="mr-2" />
                  Navigiraj do nas
                </a>
              </Button>
            </div>
          </Card>
        </div>
      </div>
    </section>
  )
}
