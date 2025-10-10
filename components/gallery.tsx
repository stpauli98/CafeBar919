"use client"

import { useState } from "react"
import Image from "next/image"
import { X } from "lucide-react"

const galleryImages = [
  { src: "/images/entrance-exterior.png", alt: "Ulaz u Caffe Bar 919" },
  { src: "/images/interior-seating.png", alt: "Unutrašnjost sa stolovima" },
  { src: "/images/hero-bar.png", alt: "Bar sa pićima" },
  { src: "/images/interior-floor.png", alt: "Detalj enterijera" },
  { src: "/images/entrance-gate.png", alt: "Pogled kroz kapiju" },
  { src: "/images/exterior-street.png", alt: "Vanjski izgled" },
]

export function Gallery() {
  const [selectedImage, setSelectedImage] = useState<number | null>(null)

  return (
    <section id="galerija" className="py-24 bg-[#1F1F1F]">
      <div className="container mx-auto px-4">
        <div className="text-center mb-16">
          <h2 className="font-[family-name:var(--font-cinzel)] text-4xl md:text-5xl text-[#F5F1E6] mb-6 tracking-wide">
            Galerija
          </h2>
          <div className="w-16 h-[1px] bg-[#D3B574] mx-auto mb-6" />
          <p className="text-[#A8A8A8] text-lg max-w-2xl mx-auto leading-relaxed">
            Zavirite u naš prostor i otkrijte atmosferu koja vas čeka.
          </p>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          {galleryImages.map((image, index) => (
            <button
              key={index}
              onClick={() => setSelectedImage(index)}
              className="relative h-64 rounded-lg overflow-hidden group cursor-pointer"
            >
              <Image
                src={image.src || "/placeholder.svg"}
                alt={image.alt}
                fill
                sizes="(max-width: 768px) 50vw, (max-width: 1200px) 33vw, 25vw"
                loading="lazy"
                quality={85}
                className="object-cover transition-transform duration-300 group-hover:scale-110"
              />
              <div className="absolute inset-0 bg-[#191919]/0 group-hover:bg-[#191919]/40 transition-colors duration-300" />
            </button>
          ))}
        </div>
      </div>

      {selectedImage !== null && (
        <div
          className="fixed inset-0 z-50 bg-[#191919]/95 flex items-center justify-center p-4"
          onClick={() => setSelectedImage(null)}
        >
          <button
            onClick={() => setSelectedImage(null)}
            className="absolute top-4 right-4 text-[#F5F1E6] hover:text-[#D3B574] transition-colors"
          >
            <X size={32} />
          </button>
          <div className="relative w-full max-w-5xl h-[80vh]">
            <Image
              src={galleryImages[selectedImage].src || "/placeholder.svg"}
              alt={galleryImages[selectedImage].alt}
              fill
              sizes="100vw"
              quality={90}
              priority
              className="object-contain"
            />
          </div>
        </div>
      )}
    </section>
  )
}
