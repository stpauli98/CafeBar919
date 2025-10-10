import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Calendar, Clock, Music } from "lucide-react"

const events = [
  {
    title: "Saturday Night",
    date: "Subota, 12. Oktobar",
    time: "22:00",
    description: "DJ set sa najboljim hitovima",
    type: "DJ Night",
  },
  {
    title: "Acoustic Night",
    date: "Petak, 18. Oktobar",
    time: "20:00",
    description: "Uživo akustična muzika",
    type: "Live Music",
  },
  {
    title: "Whiskey Night",
    date: "Četvrtak, 24. Oktobar",
    time: "21:00",
    description: "Specijalna večer sa whiskey degustacijom",
    type: "Special Event",
  },
]

export function Events() {
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

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {events.map((event) => (
            <Card
              key={event.title}
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
      </div>
    </section>
  )
}
