import { NextRequest, NextResponse } from "next/server"
import { verifyCredentials, createSession, setSessionCookie } from "@/lib/auth"

export async function POST(request: NextRequest) {
  try {
    const { username, password } = await request.json()

    // Validate input
    if (!username || !password) {
      return NextResponse.json({ error: "Korisničko ime i lozinka su obavezni" }, { status: 400 })
    }

    // Verify credentials
    const user = await verifyCredentials(username, password)

    if (!user) {
      return NextResponse.json({ error: "Neispravno korisničko ime ili lozinka" }, { status: 401 })
    }

    // Create session token
    const token = await createSession(user)

    // Set session cookie
    await setSessionCookie(token)

    return NextResponse.json({
      success: true,
      user: {
        id: user.id,
        username: user.username,
      },
    })
  } catch (error) {
    console.error("Login error:", error)
    return NextResponse.json({ error: "Greška pri prijavljivanju" }, { status: 500 })
  }
}
