"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Calendar, Clock, Music, Loader2, AlertCircle, Plus, Edit, Trash2, X, LogOut } from "lucide-react"
import type { Event, EventInsert, EventUpdate } from "@/lib/supabase"
import { supabase } from "@/lib/supabase"

interface EventFormData {
  title: string
  date: string
  time: string
  description: string
  type: string
  image_url: string
  is_active: boolean
}

export default function AdminEventsPage() {
  const router = useRouter()
  const [events, setEvents] = useState<Event[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingEvent, setEditingEvent] = useState<Event | null>(null)
  const [formData, setFormData] = useState<EventFormData>({
    title: "",
    date: "",
    time: "",
    description: "",
    type: "",
    image_url: "",
    is_active: true,
  })
  const [isSaving, setIsSaving] = useState(false)

  useEffect(() => {
    // Check authentication on mount
    const checkAuth = async () => {
      const {
        data: { user },
      } = await supabase.auth.getUser()

      if (!user) {
        router.push("/admin/login")
        return
      }
    }

    checkAuth()
    fetchAllEvents()
  }, [router])

  async function fetchAllEvents() {
    try {
      setIsLoading(true)
      const response = await fetch("/api/events")

      if (!response.ok) {
        throw new Error("Failed to fetch events")
      }

      const data = await response.json()
      setEvents(data.events || [])
      setError(null)
    } catch {
      setError("Nije moguće učitati događaje. Pokušajte ponovo kasnije.")
    } finally {
      setIsLoading(false)
    }
  }

  function openCreateModal() {
    setEditingEvent(null)
    setFormData({
      title: "",
      date: "",
      time: "",
      description: "",
      type: "",
      image_url: "",
      is_active: true,
    })
    setIsModalOpen(true)
  }

  function openEditModal(event: Event) {
    setEditingEvent(event)
    setFormData({
      title: event.title,
      date: event.date,
      time: event.time,
      description: event.description,
      type: event.type,
      image_url: event.image_url || "",
      is_active: event.is_active,
    })
    setIsModalOpen(true)
  }

  function closeModal() {
    setIsModalOpen(false)
    setEditingEvent(null)
    setFormData({
      title: "",
      date: "",
      time: "",
      description: "",
      type: "",
      image_url: "",
      is_active: true,
    })
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setIsSaving(true)

    try {
      // Get auth session for token
      const {
        data: { session },
      } = await supabase.auth.getSession()

      if (!session) {
        alert("Sesija je istekla. Molimo prijavite se ponovo.")
        router.push("/admin/login")
        return
      }

      const url = editingEvent ? `/api/events/${editingEvent.id}` : "/api/events"
      const method = editingEvent ? "PATCH" : "POST"

      const body: EventInsert | EventUpdate = {
        title: formData.title,
        date: formData.date,
        time: formData.time,
        description: formData.description,
        type: formData.type,
        image_url: formData.image_url || null,
        is_active: formData.is_active,
      }

      const response = await fetch(url, {
        method,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${session.access_token}`,
        },
        body: JSON.stringify(body),
      })

      if (!response.ok) {
        throw new Error("Failed to save event")
      }

      await fetchAllEvents()
      closeModal()
    } catch {
      alert("Greška pri čuvanju događaja. Pokušajte ponovo.")
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete(id: string) {
    if (!confirm("Da li ste sigurni da želite da obrišete ovaj događaj?")) {
      return
    }

    try {
      // Get auth session for token
      const {
        data: { session },
      } = await supabase.auth.getSession()

      if (!session) {
        alert("Sesija je istekla. Molimo prijavite se ponovo.")
        router.push("/admin/login")
        return
      }

      const response = await fetch(`/api/events/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${session.access_token}`,
        },
      })

      if (!response.ok) {
        throw new Error("Failed to delete event")
      }

      await fetchAllEvents()
    } catch {
      alert("Greška pri brisanju događaja. Pokušajte ponovo.")
    }
  }

  async function handleLogout() {
    try {
      await supabase.auth.signOut()
      router.push("/admin/login")
      router.refresh()
    } catch {
      alert("Greška pri odjavljivanju.")
    }
  }

  return (
    <div className="min-h-screen bg-[#191919] py-12">
      <div className="container mx-auto px-4">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="font-[family-name:var(--font-cinzel)] text-4xl text-[#F5F1E6] mb-2 tracking-wide">
              Upravljanje događajima
            </h1>
            <p className="text-[#A8A8A8]">Kreirajte, uredite i upravljajte događajima</p>
          </div>
          <div className="flex gap-3">
            <Button
              onClick={openCreateModal}
              className="bg-[#D3B574] text-[#191919] hover:bg-[#D3B574]/90 font-medium"
            >
              <Plus size={20} className="mr-2" />
              Novi događaj
            </Button>
            <Button
              onClick={handleLogout}
              variant="outline"
              className="border-white/10 text-[#F5F1E6] hover:bg-white/5"
            >
              <LogOut size={20} className="mr-2" />
              Odjavi se
            </Button>
          </div>
        </div>

        {/* Content */}
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
            <p className="text-[#A8A8A8] text-lg mb-4">Nema kreiranih događaja.</p>
            <Button
              onClick={openCreateModal}
              variant="outline"
              className="border-[#D3B574] text-[#D3B574] hover:bg-[#D3B574]/10"
            >
              Kreiraj prvi događaj
            </Button>
          </div>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {events.map((event) => (
              <Card
                key={event.id}
                className={`bg-[#1F1F1F] border-white/10 p-6 ${
                  !event.is_active ? "opacity-50" : ""
                }`}
              >
                <div className="mb-4">
                  <div className="flex justify-between items-start mb-2">
                    <span className="inline-block px-3 py-1 bg-[#D3B574]/20 text-[#D3B574] text-xs font-medium rounded-full">
                      {event.type}
                    </span>
                    {!event.is_active && (
                      <span className="inline-block px-3 py-1 bg-red-500/20 text-red-500 text-xs font-medium rounded-full">
                        Neaktivan
                      </span>
                    )}
                  </div>
                  <h3 className="font-[family-name:var(--font-cinzel)] text-2xl text-[#F5F1E6] mb-3 tracking-wide">
                    {event.title}
                  </h3>
                  <p className="text-[#A8A8A8] mb-4">{event.description}</p>
                </div>

                <div className="space-y-2 mb-4">
                  <div className="flex items-center gap-2 text-[#F5F1E6]">
                    <Calendar size={16} className="text-[#D3B574]" />
                    <span className="text-sm">{event.date}</span>
                  </div>
                  <div className="flex items-center gap-2 text-[#F5F1E6]">
                    <Clock size={16} className="text-[#D3B574]" />
                    <span className="text-sm">{event.time}</span>
                  </div>
                </div>

                <div className="flex gap-2">
                  <Button
                    onClick={() => openEditModal(event)}
                    variant="outline"
                    className="flex-1 border-[#D3B574] text-[#D3B574] hover:bg-[#D3B574]/10"
                  >
                    <Edit size={16} className="mr-2" />
                    Uredi
                  </Button>
                  <Button
                    onClick={() => handleDelete(event.id)}
                    variant="outline"
                    className="flex-1 border-red-500 text-red-500 hover:bg-red-500/10"
                  >
                    <Trash2 size={16} className="mr-2" />
                    Obriši
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        )}

        {/* Modal */}
        {isModalOpen && (
          <div className="fixed inset-0 z-50 bg-[#191919]/95 flex items-center justify-center p-4">
            <div className="bg-[#1F1F1F] rounded-lg p-8 max-w-2xl w-full max-h-[90vh] overflow-y-auto border border-white/10">
              <div className="flex justify-between items-center mb-6">
                <h2 className="font-[family-name:var(--font-cinzel)] text-3xl text-[#F5F1E6] tracking-wide">
                  {editingEvent ? "Uredi događaj" : "Novi događaj"}
                </h2>
                <button onClick={closeModal} className="text-[#F5F1E6] hover:text-[#D3B574]">
                  <X size={24} />
                </button>
              </div>

              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-[#F5F1E6] mb-2">Naslov</label>
                  <Input
                    value={formData.title}
                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                    required
                    className="bg-[#191919] border-white/10 text-[#F5F1E6]"
                    placeholder="Saturday Night"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[#F5F1E6] mb-2">Datum</label>
                    <Input
                      value={formData.date}
                      onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                      required
                      className="bg-[#191919] border-white/10 text-[#F5F1E6]"
                      placeholder="Subota, 12. Oktobar"
                    />
                  </div>
                  <div>
                    <label className="block text-[#F5F1E6] mb-2">Vrijeme</label>
                    <Input
                      value={formData.time}
                      onChange={(e) => setFormData({ ...formData, time: e.target.value })}
                      required
                      className="bg-[#191919] border-white/10 text-[#F5F1E6]"
                      placeholder="22:00"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-[#F5F1E6] mb-2">Tip</label>
                  <Input
                    value={formData.type}
                    onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                    required
                    className="bg-[#191919] border-white/10 text-[#F5F1E6]"
                    placeholder="DJ Night"
                  />
                </div>

                <div>
                  <label className="block text-[#F5F1E6] mb-2">Opis</label>
                  <Textarea
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    required
                    className="bg-[#191919] border-white/10 text-[#F5F1E6]"
                    placeholder="DJ set sa najboljim hitovima"
                    rows={3}
                  />
                </div>

                <div className="flex gap-4 pt-6">
                  <Button
                    type="button"
                    onClick={closeModal}
                    variant="outline"
                    className="flex-1 border-white/10 text-[#F5F1E6] hover:bg-white/5"
                    disabled={isSaving}
                  >
                    Otkaži
                  </Button>
                  <Button
                    type="submit"
                    className="flex-1 bg-[#D3B574] text-[#191919] hover:bg-[#D3B574]/90"
                    disabled={isSaving}
                  >
                    {isSaving ? (
                      <>
                        <Loader2 size={16} className="mr-2 animate-spin" />
                        Čuvanje...
                      </>
                    ) : editingEvent ? (
                      "Sačuvaj izmjene"
                    ) : (
                      "Kreiraj događaj"
                    )}
                  </Button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
