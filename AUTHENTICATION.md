# Authentication System - Supabase Auth

## 🔐 Overview

Aplikacija koristi **Supabase Authentication** za zaštitu admin panel-a i API endpoints-a.

---

## 🏗️ Architecture

### Client-Side Protection
- **Admin stranice** (`/admin/events`, `/admin/login`) imaju client-side auth provere
- Redirect na `/admin/login` ako user nije autentifikovan
- Auth token se automatski šalje u svim API pozivima

### Server-Side Protection
- **API endpoints** (`POST/PATCH/DELETE /api/events`) zahtevaju `Authorization` header
- Token verification se dešava server-side pre bilo kakve operacije
- RLS (Row Level Security) policies se primenjuju na sve database operacije

### Row Level Security (RLS)
```sql
-- Public read access (only active events)
CREATE POLICY "Public can view active events" ON events
  FOR SELECT
  USING (is_active = true);

-- Authenticated users can do everything
CREATE POLICY "Admins can do everything" ON events
  FOR ALL
  USING (auth.role() = 'authenticated');
```

---

## 🚀 Flow Diagram

```
1. User Login:
   Login Page → Supabase Auth → Session Token → Redirect to /admin/events

2. Admin Panel Access:
   /admin/events → Check supabase.auth.getUser() → If no user → Redirect to /admin/login

3. API Operations (Create/Update/Delete Event):
   Client → Get session token → API request with Authorization header
        → Server verifies token → supabase.auth.getUser(token)
        → If valid → Perform operation with RLS applied
        → If invalid → 401 Unauthorized
```

---

## 🔧 Implementation Details

### 1. Admin Events Page (`/app/admin/events/page.tsx`)

**Auth Check on Mount:**
```typescript
useEffect(() => {
  const checkAuth = async () => {
    const { data: { user } } = await supabase.auth.getUser()
    if (!user) {
      router.push("/admin/login")
      return
    }
  }
  checkAuth()
  fetchAllEvents()
}, [router])
```

**Auth Token in API Calls:**
```typescript
const { data: { session } } = await supabase.auth.getSession()

const response = await fetch('/api/events', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${session.access_token}`
  },
  body: JSON.stringify(eventData)
})
```

### 2. API Endpoints (`/app/api/events/`)

**Auth Verification Helper:**
```typescript
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

  const { data: { user }, error } = await supabaseClient.auth.getUser()

  if (error || !user) {
    return { error: "Unauthorized - Invalid token", status: 401 }
  }

  return { supabaseClient, user }
}
```

**Protected Endpoint Example:**
```typescript
export async function POST(request: NextRequest) {
  // Verify authentication
  const authResult = await verifyAuth(request)
  if ("error" in authResult) {
    return NextResponse.json({ error: authResult.error }, { status: authResult.status })
  }

  const { supabaseClient } = authResult

  // Use authenticated client (RLS applies)
  const { data, error } = await supabaseClient
    .from("events")
    .insert([body])
    .select()
    .single()
}
```

---

## 🧪 Testing Authentication

### Test 1: Unauthorized API Access
```bash
# Should return 401 Unauthorized
curl -X POST https://yoursite.com/api/events \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Event"}'
```

### Test 2: Admin Panel Without Login
1. Open incognito tab
2. Navigate to `https://yoursite.com/admin/events`
3. Should redirect to `/admin/login`

### Test 3: Valid Login & Create Event
1. Login via `/admin/login` with valid Supabase Auth credentials
2. Try creating an event
3. Should succeed with auth token automatically included

---

## ⚠️ Important Notes

### What Was Removed:
- ❌ Custom JWT auth system (`lib/auth.ts`)
- ❌ Custom auth API routes (`/api/auth/login`, `/api/auth/logout`, `/api/auth/session`)
- ❌ Middleware (`middleware.ts`) - using client-side checks instead
- ❌ `admin_users` table SQL - not needed with Supabase Auth

### What Remains:
- ✅ Supabase Auth integration
- ✅ Client-side auth checks
- ✅ Server-side token verification
- ✅ RLS policies on database

### Security Features:
- 🛡️ Token-based authentication
- 🛡️ Server-side token verification
- 🛡️ Row Level Security (RLS) policies
- 🛡️ Client-side redirect for unauthorized access
- 🛡️ Session expiration handling

---

## 🔐 Environment Variables Required

```bash
NEXT_PUBLIC_SUPABASE_URL="https://your-project.supabase.co"
NEXT_PUBLIC_SUPABASE_ANON_KEY="your-anon-key"
```

**Note**: `SUPABASE_SERVICE_ROLE_KEY` is no longer used in application code (previously bypassed RLS).

---

## 📚 Next Steps

1. **Setup Supabase Auth Users**: Create admin users in Supabase Dashboard
2. **Test Login Flow**: Verify login works with created users
3. **Test API Protection**: Verify unauthenticated requests are blocked
4. **Monitor**: Check Supabase Auth logs for authentication events

---

## 🐛 Troubleshooting

### Issue: "Unauthorized - Invalid token"
- **Cause**: Session expired or invalid token
- **Solution**: Logout and login again

### Issue: Events not creating
- **Cause**: RLS policy blocking authenticated user
- **Solution**: Verify RLS policy allows `auth.role() = 'authenticated'`

### Issue: Redirect loop on /admin/events
- **Cause**: `supabase.auth.getUser()` not returning user
- **Solution**: Check if Supabase Auth session is properly set

---

## 📊 Authentication Status

| Component | Status | Protection Level |
|-----------|--------|------------------|
| `/admin/login` | ✅ Public | None (login page) |
| `/admin/events` | ✅ Protected | Client-side check + redirect |
| `GET /api/events` | ✅ Public | RLS policy (active events only) |
| `POST /api/events` | ✅ Protected | Token verification + RLS |
| `PATCH /api/events/[id]` | ✅ Protected | Token verification + RLS |
| `DELETE /api/events/[id]` | ✅ Protected | Token verification + RLS |

---

**Last Updated**: 2025-01-12
**Authentication System**: Supabase Auth
**Status**: ✅ Production Ready