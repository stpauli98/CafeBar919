import { NextRequest, NextResponse } from "next/server"
import { getSupabaseAdmin, type EventUpdate } from "@/lib/supabase"

// PATCH /api/events/[id] - Update event (requires admin)
export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params
    const body = (await request.json()) as EventUpdate

    if (!id) {
      return NextResponse.json({ error: "Event ID is required" }, { status: 400 })
    }

    // Use admin client for update operations
    const { data, error } = await getSupabaseAdmin()
      .from("events")
      .update(body as never)
      .eq("id", id)
      .select()
      .single()

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
export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params

    if (!id) {
      return NextResponse.json({ error: "Event ID is required" }, { status: 400 })
    }

    // Hard delete - permanently remove from database
    const { error } = await getSupabaseAdmin().from("events").delete().eq("id", id)

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

// GET /api/events/[id] - Get single event
export async function GET(_request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params

    if (!id) {
      return NextResponse.json({ error: "Event ID is required" }, { status: 400 })
    }

    const { data, error } = await getSupabaseAdmin().from("events").select("*").eq("id", id).single()

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
