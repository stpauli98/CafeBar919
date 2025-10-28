"use client"

import { useState, useEffect } from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Calendar, Clock, Music, Loader2, AlertCircle } from "lucide-react"
import type { Event } from "@/lib/supabase"
import { useLocomotiveScroll } from "@/components/locomotive-scroll-context"

export function Events() {
  const [events, setEvents] = useState<Event[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { scroll } = useLocomotiveScroll()

  useEffect(() => {
    async function fetchEvents() {
      try {
        setIsLoading(true)
        const response = await fetch("/api/events")

        if (!response.ok) {
          throw new Error("Failed to fetch events")
        }

        const data = await response.json()
        setEvents(data.events || [])
        setError(null)
      } catch (err) {
        console.error("Error fetching events:", err)
        setError("Nije moguće učitati događaje. Pokušajte ponovo kasnije.")
      } finally {
        setIsLoading(false)
      }
    }

    fetchEvents()
  }, [])

  // Update Locomotive Scroll after events are loaded
  useEffect(() => {
    if (!isLoading && scroll) {
      const timer = setTimeout(() => {
        scroll.update()
      }, 500)
      return () => clearTimeout(timer)
    }
  }, [isLoading, scroll])
  return (
    <section id="događaji" className="py-24 bg-[#191919]">
      <div className="container mx-auto px-4">
        <div className="text-center mb-16">
          <h2 className="font-[family-name:var(--font-cinzel)] text-4xl md:text-5xl text-[#F5F1E6] mb-6 tracking-wide">
            Događaji
          </h2>
          <div className="w-16 h-[1px] bg-[#D3B574] mx-auto mb-6" />
          <p className="text-[#A8A8A8] text-lg max-w-2xl mx-auto leading-relaxed">
            Akustične večeri, DJ setovi i posebne noći — pogledaj šta slijedi.
          </p>
        </div>

{isLoading ? (
          <div className="flex justify-center items-center py-20">
            <Loader2 size={48} className="text-[#D3B574] animate-spin" />
          </div>
        ) : error ? (
          <div className="flex flex-col items-center justify-center py-20">
            <AlertCircle size={48} className="text-red-500 mb-4" />
            <p className="text-[#A8A8A8] text-lg">{error}</p>
          </div>
        ) : events.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20">
            <Music size={48} className="text-[#D3B574] mb-4" />
            <p className="text-[#A8A8A8] text-lg">Trenutno nema zakazanih događaja.</p>
          </div>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {events.map((event) => (
              <Card
                key={event.id}
                className="bg-[#1F1F1F] border-white/10 p-6 hover:border-[#D3B574]/50 transition-all duration-300 hover:shadow-[0_0_20px_rgba(211,181,116,0.2)]"
              >
                <div className="mb-4">
                  <span className="inline-block px-3 py-1 bg-[#D3B574]/20 text-[#D3B574] text-xs font-medium rounded-full mb-4">
                    {event.type}
                  </span>
                  <h3 className="font-[family-name:var(--font-cinzel)] text-2xl text-[#F5F1E6] mb-3 tracking-wide">
                    {event.title}
                  </h3>
                  <p className="text-[#A8A8A8] mb-6">{event.description}</p>
                </div>

                <div className="space-y-2 mb-6">
                  <div className="flex items-center gap-2 text-[#F5F1E6]">
                    <Calendar size={16} className="text-[#D3B574]" />
                    <span className="text-sm">{event.date}</span>
                  </div>
                  <div className="flex items-center gap-2 text-[#F5F1E6]">
                    <Clock size={16} className="text-[#D3B574]" />
                    <span className="text-sm">{event.time}</span>
                  </div>
                </div>

                <Button
                  variant="outline"
                  className="w-full border-[#D3B574] text-[#D3B574] hover:bg-[#D3B574]/10 bg-transparent"
                >
                  <Music size={16} className="mr-2" />
                  Prijavi dolazak
                </Button>
              </Card>
            ))}
          </div>
        )}
      </div>
    </section>
  )
}
