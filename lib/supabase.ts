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

// Note: Service role key removed for security
// All operations now go through authenticated user context with RLS policies

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
