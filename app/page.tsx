import { Header } from "@/components/header"
import { Hero } from "@/components/hero"
import { About } from "@/components/about"
import { Menu } from "@/components/menu"
import { Events } from "@/components/events"
import { Gallery } from "@/components/gallery"
import { Contact } from "@/components/contact"
import { Footer } from "@/components/footer"
import { SmoothScroll } from "@/components/smooth-scroll"

export default function Home() {
  return (
    <SmoothScroll>
      <main className="min-h-screen">
        <Header />
        <Hero />
        <About />
        <Menu />
        <Events />
        <Gallery />
        <Contact />
        <Footer />
      </main>
    </SmoothScroll>
  )
}
