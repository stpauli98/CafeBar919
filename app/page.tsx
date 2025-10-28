import { Header } from "@/components/header"
import { Hero } from "@/components/hero"
import { About } from "@/components/about"
import { Menu } from "@/components/menu"
import { Events } from "@/components/events"
import { Gallery } from "@/components/gallery"
import { Contact } from "@/components/contact"
import { Footer } from "@/components/footer"
import { SmoothScroll } from "@/components/smooth-scroll"
import { LoadingScreen } from "@/components/loading-screen"

export default function Home() {
  return (
    <>
      <LoadingScreen />
      <SmoothScroll>
        <main className="min-h-screen">
          <Header />
          <Hero />
          <Events />
          <Gallery />
          <Menu />
          <About />
          <Contact />
          <Footer />
        </main>
      </SmoothScroll>
    </>
  )
}
