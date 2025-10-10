"use client"

import { useEffect, useRef } from "react"
import type LocomotiveScroll from "locomotive-scroll"

export function SmoothScroll({ children }: { children: React.ReactNode }) {
  const scrollRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    let locomotiveScroll: LocomotiveScroll | null = null

    const initLocomotiveScroll = async () => {
      const LocomotiveScrollModule = (await import("locomotive-scroll")).default

      if (scrollRef.current) {
        locomotiveScroll = new LocomotiveScrollModule({
          el: scrollRef.current,
          smooth: true,
          multiplier: 1,
          class: "is-reveal",
        })
      }
    }

    initLocomotiveScroll()

    return () => {
      if (locomotiveScroll) locomotiveScroll.destroy()
    }
  }, [])

  return (
    <div ref={scrollRef} data-scroll-container>
      {children}
    </div>
  )
}
