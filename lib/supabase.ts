import { createClient } from "@supabase/supabase-js"

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL!
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!

if (!supabaseUrl || !supabaseAnonKey) {
  throw new Error("Missing Supabase environment variables")
}

// Client-side Supabase client (for browser)
export const supabase = createClient(supabaseUrl, supabaseAnonKey, {
  auth: {
    // Use cookies instead of localStorage for SSR compatibility
    storage: typeof window !== "undefined" ? window.localStorage : undefined,
    autoRefreshToken: true,
    persistSession: true,
    detectSessionInUrl: true,
  },
})

// Server-side Supabase client for admin operations (database queries)
// Lazy initialization to avoid browser errors
let _supabaseAdmin: ReturnType<typeof createClient> | null = null

export function getSupabaseAdmin() {
  if (typeof window !== "undefined") {
    throw new Error("supabaseAdmin can only be used on the server side")
  }

  if (!_supabaseAdmin) {
    _supabaseAdmin = createClient(supabaseUrl, process.env.SUPABASE_SERVICE_ROLE_KEY!, {
      auth: {
        autoRefreshToken: false,
        persistSession: false,
      },
    })
  }

  return _supabaseAdmin
}

// Deprecated: Use getSupabaseAdmin() instead
// Keeping for backward compatibility with existing code
export const supabaseAdmin =
  typeof window === "undefined"
    ? createClient(supabaseUrl, process.env.SUPABASE_SERVICE_ROLE_KEY || "", {
        auth: {
          autoRefreshToken: false,
          persistSession: false,
        },
      })
    : null

// Types for events table
export interface Event {
  id: string
  title: string
  date: string
  time: string
  description: string
  type: string
  image_url: string | null
  is_active: boolean
  created_at: string
  updated_at: string
}

export interface EventInsert {
  title: string
  date: string
  time: string
  description: string
  type: string
  image_url?: string | null
  is_active?: boolean
}

export interface EventUpdate {
  title?: string
  date?: string
  time?: string
  description?: string
  type?: string
  image_url?: string | null
  is_active?: boolean
}
