import { NextRequest, NextResponse } from "next/server"
import { supabase, getSupabaseAdmin, type EventInsert } from "@/lib/supabase"

// GET /api/events - List all active events
export async function GET() {
  try {
    const { data, error } = await supabase
      .from("events")
      .select("*")
      .eq("is_active", true)
      .order("date", { ascending: true })

    if (error) {
      console.error("Error fetching events:", error)
      return NextResponse.json({ error: error.message }, { status: 500 })
    }

    return NextResponse.json({ events: data })
  } catch (error) {
    console.error("Unexpected error:", error)
    return NextResponse.json({ error: "Internal server error" }, { status: 500 })
  }
}

// POST /api/events - Create new event (requires admin)
export async function POST(request: NextRequest) {
  try {
    const body = (await request.json()) as EventInsert

    // Validate required fields
    if (!body.title || !body.date || !body.time || !body.description || !body.type) {
      return NextResponse.json({ error: "Missing required fields" }, { status: 400 })
    }

    // Use admin client for insert operations
    const { data, error } = await getSupabaseAdmin()
      .from("events")
      .insert([body] as never)
      .select()
      .single()

    if (error) {
      console.error("Error creating event:", error)
      return NextResponse.json({ error: error.message }, { status: 500 })
    }

    return NextResponse.json({ event: data }, { status: 201 })
  } catch (error) {
    console.error("Unexpected error:", error)
    return NextResponse.json({ error: "Internal server error" }, { status: 500 })
  }
}
