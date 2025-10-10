import { Card } from "@/components/ui/card"
import { Coffee, Wine, Martini, Droplet } from "lucide-react"

const menuItems = [
  {
    category: "Espresso",
    icon: Coffee,
    items: [
      { name: "Espresso", price: "2.00 KM" },
      { name: "Cappuccino", price: "3.00 KM" },
      { name: "Latte", price: "3.50 KM" },
      { name: "Flat White", price: "3.50 KM" },
    ],
  },
  {
    category: "Piva",
    icon: Droplet,
    items: [
      { name: "Erdinger", price: "5.00 KM" },
      { name: "Domaće pivo", price: "3.00 KM" },
      { name: "Craft pivo", price: "6.00 KM" },
    ],
  },
  {
    category: "Whiskey",
    icon: Wine,
    items: [
      { name: "Jameson", price: "6.00 KM" },
      { name: "Jack Daniel's", price: "7.00 KM" },
      { name: "Chivas Regal", price: "8.00 KM" },
    ],
  },
  {
    category: "Kokteli",
    icon: Martini,
    items: [
      { name: "Mojito", price: "8.00 KM" },
      { name: "Gin Tonic", price: "7.00 KM" },
      { name: "Old Fashioned", price: "9.00 KM" },
    ],
  },
]

export function Menu() {
  return (
    <section id="meni" className="py-24 bg-[#1F1F1F]">
      <div className="container mx-auto px-4">
        <div className="text-center mb-16">
          <h2 className="font-[family-name:var(--font-cinzel)] text-4xl md:text-5xl text-[#F5F1E6] mb-6 tracking-wide">
            Meni
          </h2>
          <div className="w-16 h-[1px] bg-[#D3B574] mx-auto mb-6" />
          <p className="text-[#A8A8A8] text-lg max-w-2xl mx-auto leading-relaxed">
            Biramo zrna i boce s karakterom. Od savršeno izbalansiranog espressa do pažljivo odabranog whiskeja.
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
          {menuItems.map((category) => {
            const Icon = category.icon
            return (
              <Card
                key={category.category}
                className="bg-[#191919] border-white/10 p-6 hover:border-[#D3B574]/50 transition-all duration-300 hover:shadow-[0_0_20px_rgba(211,181,116,0.2)]"
              >
                <div className="flex items-center gap-3 mb-6">
                  <Icon className="text-[#D3B574]" size={24} />
                  <h3 className="font-[family-name:var(--font-cinzel)] text-xl text-[#F5F1E6] tracking-wide">
                    {category.category}
                  </h3>
                </div>
                <div className="space-y-4">
                  {category.items.map((item) => (
                    <div key={item.name} className="flex justify-between items-baseline">
                      <span className="text-[#F5F1E6]">{item.name}</span>
                      <span className="text-[#A8A8A8] text-sm">{item.price}</span>
                    </div>
                  ))}
                </div>
              </Card>
            )
          })}
        </div>
      </div>
    </section>
  )
}
