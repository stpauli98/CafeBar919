import Image from "next/image"
import { Quote } from "lucide-react"

export function About() {
  return (
    <section id="o-nama" className="py-24 bg-[#191919]">
      <div className="container mx-auto px-4">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          <div className="relative h-[500px] rounded-lg overflow-hidden">
            <Image
              src="/images/interior-seating.png"
              alt="Interior Caffe Bar 919"
              fill
              sizes="(max-width: 768px) 100vw, 50vw"
              quality={85}
              priority
              className="object-cover"
            />
          </div>

          <div>
            <h2 className="font-[family-name:var(--font-cinzel)] text-4xl md:text-5xl text-[#F5F1E6] mb-6 tracking-wide">
              O nama
            </h2>
            <div className="w-16 h-[1px] bg-[#D3B574] mb-8" />

            <p className="text-[#F5F1E6] text-lg leading-relaxed mb-6">
              U samom srcu grada, 919 spaja šarm klasičnog enterijera i moderan osjećaj udobnosti. Dođi zbog kafe,
              ostani zbog atmosfere.
            </p>

            <p className="text-[#A8A8A8] text-base leading-relaxed mb-8">
              Naš prostor odražava pažljivo odabrane detalje — od ciglenih zidova i tamnozelene stolarije do zlatnih
              akcenata koji stvaraju toplu, elegantnu atmosferu. Svaki kutak ima svoju priču, a svaki posjet postaje
              poseban trenutak.
            </p>

            <div className="bg-[#1F1F1F] border border-white/10 rounded-lg p-6 relative">
              <Quote className="text-[#D3B574] mb-4" size={32} />
              <p className="text-[#F5F1E6] text-xl font-[family-name:var(--font-cinzel)] italic leading-relaxed">
                Udobnost klasike, energija grada.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
