"use client"

import { useState } from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Loader2, AlertCircle, Lock } from "lucide-react"
import { supabase } from "@/lib/supabase"

export default function AdminLoginPage() {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setIsLoading(true)

    try {
      const { data, error: signInError } = await supabase.auth.signInWithPassword({
        email,
        password,
      })

      if (signInError) {
        setError("Neispravna email adresa ili lozinka")
        setIsLoading(false)
        return
      }

      if (data.session) {
        // Wait a bit for cookies to be set before redirecting
        await new Promise((resolve) => setTimeout(resolve, 500))

        // Use window.location for full page reload to ensure middleware gets new cookies
        window.location.href = "/admin/events"
      } else {
        setError("Sesija nije kreirana. Pokušajte ponovo.")
        setIsLoading(false)
      }
    } catch {
      setError("Greška pri prijavljivanju. Pokušajte ponovo.")
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#191919] flex items-center justify-center px-4">
      <Card className="w-full max-w-md bg-[#1F1F1F] border-white/10 p-8">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-[#D3B574]/20 mb-4">
            <Lock size={32} className="text-[#D3B574]" />
          </div>
          <h1 className="font-[family-name:var(--font-cinzel)] text-3xl text-[#F5F1E6] mb-2 tracking-wide">
            Admin Pristup
          </h1>
          <div className="w-16 h-[1px] bg-[#D3B574] mx-auto mb-4" />
          <p className="text-[#A8A8A8]">Prijavite se za upravljanje događajima</p>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 p-4 bg-red-500/10 border border-red-500/20 rounded-lg flex items-start gap-3">
            <AlertCircle size={20} className="text-red-500 flex-shrink-0 mt-0.5" />
            <p className="text-red-500 text-sm">{error}</p>
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="email" className="block text-[#F5F1E6] mb-2 text-sm">
              Email adresa
            </label>
            <Input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
              className="bg-[#191919] border-white/10 text-[#F5F1E6] focus:border-[#D3B574]"
              placeholder="admin@caffebar919.com"
              disabled={isLoading}
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-[#F5F1E6] mb-2 text-sm">
              Lozinka
            </label>
            <Input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
              className="bg-[#191919] border-white/10 text-[#F5F1E6] focus:border-[#D3B574]"
              placeholder="Unesite lozinku"
              disabled={isLoading}
            />
          </div>

          <Button
            type="submit"
            className="w-full bg-[#D3B574] text-[#191919] hover:bg-[#D3B574]/90 font-medium py-6"
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <Loader2 size={20} className="mr-2 animate-spin" />
                Prijavljivanje...
              </>
            ) : (
              "Prijavi se"
            )}
          </Button>
        </form>

        {/* Back to Home */}
        <div className="mt-6 text-center">
          <button
            onClick={() => (window.location.href = "/")}
            className="text-[#A8A8A8] hover:text-[#D3B574] text-sm transition-colors"
          >
            ← Nazad na početnu
          </button>
        </div>
      </Card>
    </div>
  )
}
