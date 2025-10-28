import { NextRequest, NextResponse } from "next/server"
import { createClient } from "@supabase/supabase-js"

export async function middleware(_request: NextRequest) {
  // TEMPORARY: Disable middleware for testing
  // TODO: Re-enable authentication after fixing Supabase cookie handling
  return NextResponse.next()

  /* DISABLED - Uncomment when ready to enable auth protection
  const { pathname } = request.nextUrl

  // Protect /admin/events route
  if (pathname.startsWith("/admin/events")) {
    // Try to get access token from Supabase auth cookies
    const token =
      request.cookies.get("sb-wswizqibfqrmujlcfrav-auth-token")?.value ||
      request.cookies.get("supabase-auth-token")?.value

    if (!token) {
      const loginUrl = new URL("/admin/login", request.url)
      return NextResponse.redirect(loginUrl)
    }

    try {
      const supabase = createClient(
        process.env.NEXT_PUBLIC_SUPABASE_URL!,
        process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
      )

      let accessToken: string
      try {
        const parsedToken = JSON.parse(token)
        accessToken = parsedToken.access_token || token
      } catch {
        accessToken = token
      }

      const {
        data: { user },
        error,
      } = await supabase.auth.getUser(accessToken)

      if (error || !user) {
        const loginUrl = new URL("/admin/login", request.url)
        return NextResponse.redirect(loginUrl)
      }

      return NextResponse.next()
    } catch (err) {
      console.error("Middleware error:", err)
      const loginUrl = new URL("/admin/login", request.url)
      return NextResponse.redirect(loginUrl)
    }
  }

  return NextResponse.next()
  */
}

export const config = {
  matcher: ["/admin/events/:path*"],
}
