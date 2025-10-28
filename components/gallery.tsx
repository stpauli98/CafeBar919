"use client"

import { useState, useEffect } from "react"
import Image from "next/image"
import { X, ChevronDown, Loader2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { useLocomotiveScroll } from "@/components/locomotive-scroll-context"

const galleryImages = [
  { src: "/images/image1.webp", alt: "Elegantni bar sa modernim ambijentom" },
  { src: "/images/image2.webp", alt: "Stilski detalji unutrašnjosti" },
  { src: "/images/image3.webp", alt: "Ugodna atmosfera Caffe Bar 919" },
  { src: "/images/interior-seating.webp", alt: "Unutrašnjost sa stolovima" },
  { src: "/images/loadingImage.webp", alt: "Ulaz u Caffe Bar 919" },
  { src: "/images/WhatsApp Image 2025-10-28 at 14.54.42 (1).webp", alt: "Detalj enterijera" },
  { src: "/images/WhatsApp Image 2025-10-28 at 14.54.42 (2).webp", alt: "Prostor za opuštanje" },
  { src: "/images/WhatsApp Image 2025-10-28 at 14.54.42.webp", alt: "Bar i šank" },
  { src: "/images/WhatsApp Image 2025-10-28 at 14.54.43 (1).webp", alt: "Ambient kaffe bara" },
  { src: "/images/WhatsApp Image 2025-10-28 at 14.54.43 (2).webp", alt: "Moderna atmosfera" },
  { src: "/images/WhatsApp Image 2025-10-28 at 14.54.43 (3).webp", alt: "Detalji dekora" },
  { src: "/images/WhatsApp Image 2025-10-28 at 14.54.45 (1).webp", alt: "Vanjski izgled" },
  { src: "/images/WhatsApp Image 2025-10-28 at 14.54.45 (2).webp", alt: "Noćna atmosfera" },
  { src: "/images/WhatsApp Image 2025-10-28 at 14.54.45.webp", alt: "Pogled kroz kapiju" },
]

const INITIAL_DISPLAY_COUNT = 4

export function Gallery() {
  const [selectedImage, setSelectedImage] = useState<number | null>(null)
  const [showAll, setShowAll] = useState(false)
  const [isImageLoading, setIsImageLoading] = useState(false)
  const { scroll } = useLocomotiveScroll()

  const displayedImages = showAll ? galleryImages : galleryImages.slice(0, INITIAL_DISPLAY_COUNT)
  const hasMoreImages = galleryImages.length > INITIAL_DISPLAY_COUNT

  // Update Locomotive Scroll when images are loaded
  useEffect(() => {
    if (showAll && scroll) {
      // Wait for images to load before updating scroll
      const timer = setTimeout(() => {
        scroll.update()
      }, 300)
      return () => clearTimeout(timer)
    }
  }, [showAll, scroll])

  // Disable Locomotive Scroll when modal is open
  useEffect(() => {
    if (scroll) {
      if (selectedImage !== null) {
        scroll.stop()
        // Reset loading state when opening modal
        setIsImageLoading(true)
      } else {
        scroll.start()
        scroll.update()
        setIsImageLoading(false)
      }
    }
  }, [selectedImage, scroll])

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
          {displayedImages.map((image, index) => (
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

        {hasMoreImages && !showAll && (
          <div className="flex justify-center mt-12">
            <Button
              onClick={() => setShowAll(true)}
              variant="outline"
              className="border-[#D3B574] text-[#D3B574] hover:bg-[#D3B574]/10 px-8 py-6 text-lg group"
            >
              Prikaži više
              <ChevronDown className="ml-2 transition-transform duration-300 group-hover:translate-y-1" size={20} />
            </Button>
          </div>
        )}
      </div>

      {selectedImage !== null && (
        <div
          className="fixed inset-0 z-50 bg-[#191919]/95 flex items-center justify-center p-4"
          onClick={() => setSelectedImage(null)}
        >
          <button
            onClick={() => setSelectedImage(null)}
            className="absolute top-4 right-4 text-[#F5F1E6] hover:text-[#D3B574] transition-colors z-10"
          >
            <X size={32} />
          </button>

          {/* Loading spinner */}
          {isImageLoading && (
            <div className="absolute inset-0 flex items-center justify-center">
              <Loader2 size={48} className="text-[#D3B574] animate-spin" />
            </div>
          )}

          <div className="relative w-full max-w-5xl h-[80vh]">
            <Image
              src={galleryImages[selectedImage].src || "/placeholder.svg"}
              alt={galleryImages[selectedImage].alt}
              fill
              sizes="100vw"
              quality={90}
              priority
              className="object-contain"
              onLoadingComplete={() => setIsImageLoading(false)}
              onError={() => setIsImageLoading(false)}
            />
          </div>
        </div>
      )}
    </section>
  )
}
