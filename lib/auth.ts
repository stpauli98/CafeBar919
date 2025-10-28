import { SignJWT, jwtVerify } from "jose"
import { cookies } from "next/headers"
import bcrypt from "bcryptjs"
import { getSupabaseAdmin } from "./supabase"

const JWT_SECRET = new TextEncoder().encode(process.env.JWT_SECRET || "caffe-bar-919-secret-key-change-in-production")
const JWT_COOKIE_NAME = "admin-session"

// Admin user type
export interface AdminUser {
  id: string
  username: string
  created_at: string
  last_login: string | null
}

// Verify username and password
export async function verifyCredentials(username: string, password: string): Promise<AdminUser | null> {
  try {
    // Fetch user from database
    const { data: user, error } = await getSupabaseAdmin()
      .from("admin_users")
      .select("id, username, password_hash, created_at, last_login")
      .eq("username", username)
      .single()

    if (error || !user) {
      return null
    }

    // Type cast to avoid TypeScript errors (deprecated auth system)
    const userData = user as unknown as {
      id: string
      username: string
      password_hash: string
      created_at: string
      last_login: string | null
    }

    // Verify password
    const isValid = await bcrypt.compare(password, userData.password_hash)
    if (!isValid) {
      return null
    }

    // Update last_login timestamp
    await getSupabaseAdmin()
      .from("admin_users")
      .update({ last_login: new Date().toISOString() } as never)
      .eq("id", userData.id)

    // Return user without password_hash
    return {
      id: userData.id,
      username: userData.username,
      created_at: userData.created_at,
      last_login: userData.last_login,
    }
  } catch (error) {
    console.error("Error verifying credentials:", error)
    return null
  }
}

// Create JWT token
export async function createSession(user: AdminUser): Promise<string> {
  const token = await new SignJWT({
    userId: user.id,
    username: user.username,
  })
    .setProtectedHeader({ alg: "HS256" })
    .setIssuedAt()
    .setExpirationTime("24h")
    .sign(JWT_SECRET)

  return token
}

// Verify JWT token
export async function verifySession(token: string): Promise<{ userId: string; username: string } | null> {
  try {
    const verified = await jwtVerify(token, JWT_SECRET)
    return verified.payload as { userId: string; username: string }
  } catch {
    return null
  }
}

// Get current session from cookies
export async function getSession(): Promise<{ userId: string; username: string } | null> {
  const cookieStore = await cookies()
  const token = cookieStore.get(JWT_COOKIE_NAME)?.value

  if (!token) {
    return null
  }

  return verifySession(token)
}

// Set session cookie
export async function setSessionCookie(token: string) {
  const cookieStore = await cookies()
  cookieStore.set(JWT_COOKIE_NAME, token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    maxAge: 60 * 60 * 24, // 24 hours
    path: "/",
  })
}

// Clear session cookie
export async function clearSessionCookie() {
  const cookieStore = await cookies()
  cookieStore.delete(JWT_COOKIE_NAME)
}

// Check if user is authenticated (for use in Server Components)
export async function isAuthenticated(): Promise<boolean> {
  const session = await getSession()
  return session !== null
}
