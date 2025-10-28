import { NextRequest, NextResponse } from "next/server"
import { createClient } from "@supabase/supabase-js"

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl

  // TEMPORARY: Disable middleware for testing
  return NextResponse.next()

  // Protect /admin/events route
  if (pathname.startsWith("/admin/events")) {
    // Try to get access token from Supabase auth cookies
    const token =
      request.cookies.get("sb-wswizqibfqrmujlcfrav-auth-token")?.value ||
      request.cookies.get("supabase-auth-token")?.value

    console.log("Middleware - Token found:", !!token)

    if (!token) {
      // No auth token, redirect to login
      console.log("Middleware - No token, redirecting to login")
      const loginUrl = new URL("/admin/login", request.url)
      return NextResponse.redirect(loginUrl)
    }

    // Try to verify the session with Supabase
    try {
      const supabase = createClient(
        process.env.NEXT_PUBLIC_SUPABASE_URL!,
        process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
      )

      // Parse the token (it's usually a JSON string with access_token)
      let parsedToken
      try {
        parsedToken = JSON.parse(token)
      } catch {
        parsedToken = { access_token: token }
      }

      const {
        data: { user },
        error,
      } = await supabase.auth.getUser(parsedToken.access_token || token)

      console.log("Middleware - User check:", user ? "authenticated" : "not authenticated", error)

      if (error || !user) {
        console.log("Middleware - Invalid session, redirecting to login")
        const loginUrl = new URL("/admin/login", request.url)
        return NextResponse.redirect(loginUrl)
      }

      // User authenticated, allow access
      return NextResponse.next()
    } catch (err) {
      console.error("Middleware error:", err)
      const loginUrl = new URL("/admin/login", request.url)
      return NextResponse.redirect(loginUrl)
    }
  }

  // Allow access to other routes
  return NextResponse.next()
}

export const config = {
  matcher: ["/admin/events/:path*"],
}
