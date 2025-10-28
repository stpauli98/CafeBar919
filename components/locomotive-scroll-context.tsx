"use client"

import { createContext, useContext, useEffect, useRef, useState, useCallback } from "react"
import type LocomotiveScroll from "locomotive-scroll"

interface LocomotiveScrollEvent {
  scroll: {
    x: number
    y: number
  }
  limit: {
    x: number
    y: number
  }
  currentElements: Record<string, unknown>
}

interface LocomotiveScrollContextType {
  scroll: LocomotiveScroll | null
  isReady: boolean
  onScroll: (callback: (args: LocomotiveScrollEvent) => void) => () => void
}

const LocomotiveScrollContext = createContext<LocomotiveScrollContextType>({
  scroll: null,
  isReady: false,
  onScroll: () => () => {},
})

export function useLocomotiveScroll() {
  return useContext(LocomotiveScrollContext)
}

export function LocomotiveScrollProvider({ children }: { children: React.ReactNode }) {
  const scrollRef = useRef<HTMLDivElement>(null)
  const locomotiveScrollRef = useRef<LocomotiveScroll | null>(null)
  const [isReady, setIsReady] = useState(false)
  const scrollCallbacksRef = useRef<Set<(args: LocomotiveScrollEvent) => void>>(new Set())

  // Callback registration system
  const onScroll = useCallback((callback: (args: LocomotiveScrollEvent) => void) => {
    scrollCallbacksRef.current.add(callback)

    return () => {
      scrollCallbacksRef.current.delete(callback)
    }
  }, [])

  useEffect(() => {
    let timeoutId: NodeJS.Timeout
    const updateIntervals: NodeJS.Timeout[] = []

    const initLocomotiveScroll = async () => {
      const LocomotiveScrollModule = (await import("locomotive-scroll")).default

      if (scrollRef.current) {
        locomotiveScrollRef.current = new LocomotiveScrollModule({
          el: scrollRef.current,
          smooth: true,
          multiplier: 1,
          class: "is-reveal",
        })

        // Set up scroll event handler
        locomotiveScrollRef.current.on("scroll", (args: LocomotiveScrollEvent) => {
          scrollCallbacksRef.current.forEach((callback) => callback(args))
        })

        // Mark as ready after initialization
        timeoutId = setTimeout(() => {
          setIsReady(true)
        }, 100)

        // Update scroll multiple times during page load to handle dynamic content
        // This ensures the scroll height is correct as images and API data load
        const updateTimes = [300, 600, 1000, 1500, 2000, 3000]
        updateTimes.forEach((time) => {
          const timer = setTimeout(() => {
            if (locomotiveScrollRef.current) {
              locomotiveScrollRef.current.update()
            }
          }, time)
          updateIntervals.push(timer)
        })
      }
    }

    initLocomotiveScroll()

    // Handle window resize
    const handleResize = () => {
      if (locomotiveScrollRef.current) {
        locomotiveScrollRef.current.update()
      }
    }

    window.addEventListener("resize", handleResize)

    return () => {
      clearTimeout(timeoutId)
      updateIntervals.forEach((timer) => clearTimeout(timer))
      window.removeEventListener("resize", handleResize)
      // Clear callbacks from a copy to avoid stale reference
      const callbacks = scrollCallbacksRef.current
      callbacks.clear()
      if (locomotiveScrollRef.current) {
        locomotiveScrollRef.current.destroy()
        locomotiveScrollRef.current = null
      }
    }
  }, [])

  return (
    <LocomotiveScrollContext.Provider value={{ scroll: locomotiveScrollRef.current, isReady, onScroll }}>
      <div ref={scrollRef} data-scroll-container>
        {children}
      </div>
    </LocomotiveScrollContext.Provider>
  )
}
