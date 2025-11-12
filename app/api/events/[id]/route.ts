import { NextRequest, NextResponse } from "next/server"
import { type EventUpdate } from "@/lib/supabase"
import { createClient } from "@supabase/supabase-js"

// Helper function to verify authentication
async function verifyAuth(request: NextRequest) {
  const authHeader = request.headers.get("authorization")
  if (!authHeader) {
    return { error: "Unauthorized - No token provided", status: 401 }
  }

  const token = authHeader.replace("Bearer ", "")

  const supabaseClient = createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
    {
      global: {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    }
  )

  const {
    data: { user },
    error: authError,
  } = await supabaseClient.auth.getUser()

  if (authError || !user) {
    return { error: "Unauthorized - Invalid token", status: 401 }
  }

  return { supabaseClient, user }
}

// PATCH /api/events/[id] - Update event (requires admin)
export async function PATCH(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    // Verify authentication
    const authResult = await verifyAuth(request)
    if ("error" in authResult) {
      return NextResponse.json({ error: authResult.error }, { status: authResult.status })
    }

    const { supabaseClient } = authResult
    const { id } = await params
    const body = (await request.json()) as EventUpdate

    if (!id) {
      return NextResponse.json({ error: "Event ID is required" }, { status: 400 })
    }

    // Use authenticated client for update (RLS will apply)
    const { data, error } = await supabaseClient.from("events").update(body as never).eq("id", id).select().single()

    if (error) {
      console.error("Error updating event:", error)
      return NextResponse.json({ error: error.message }, { status: 500 })
    }

    if (!data) {
      return NextResponse.json({ error: "Event not found" }, { status: 404 })
    }

    return NextResponse.json({ event: data })
  } catch (error) {
    console.error("Unexpected error:", error)
    return NextResponse.json({ error: "Internal server error" }, { status: 500 })
  }
}

// DELETE /api/events/[id] - Delete event (hard delete - permanently remove from database)
export async function DELETE(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    // Verify authentication
    const authResult = await verifyAuth(request)
    if ("error" in authResult) {
      return NextResponse.json({ error: authResult.error }, { status: authResult.status })
    }

    const { supabaseClient } = authResult
    const { id } = await params

    if (!id) {
      return NextResponse.json({ error: "Event ID is required" }, { status: 400 })
    }

    // Hard delete - permanently remove from database (RLS will apply)
    const { error } = await supabaseClient.from("events").delete().eq("id", id)

    if (error) {
      console.error("Error deleting event:", error)
      return NextResponse.json({ error: error.message }, { status: 500 })
    }

    return NextResponse.json({ message: "Event deleted successfully" })
  } catch (error) {
    console.error("Unexpected error:", error)
    return NextResponse.json({ error: "Internal server error" }, { status: 500 })
  }
}

// GET /api/events/[id] - Get single event (public, no auth required)
export async function GET(_request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params

    if (!id) {
      return NextResponse.json({ error: "Event ID is required" }, { status: 400 })
    }

    // Use public client for GET (no auth required for reading)
    const supabaseClient = createClient(
      process.env.NEXT_PUBLIC_SUPABASE_URL!,
      process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
    )

    const { data, error } = await supabaseClient.from("events").select("*").eq("id", id).single()

    if (error) {
      console.error("Error fetching event:", error)
      return NextResponse.json({ error: error.message }, { status: 500 })
    }

    if (!data) {
      return NextResponse.json({ error: "Event not found" }, { status: 404 })
    }

    return NextResponse.json({ event: data })
  } catch (error) {
    console.error("Unexpected error:", error)
    return NextResponse.json({ error: "Internal server error" }, { status: 500 })
  }
}
