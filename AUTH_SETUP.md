# Admin Authentication Setup - Uputstvo

## 📋 Pregled

Implementiran je kompletan authentication sistem za admin panel sa sledećim funkcionalnostima:
- ✅ Login stranica (`/admin/login`)
- ✅ Session management sa JWT tokenima
- ✅ Protected routes (middleware zaštita)
- ✅ Logout funkcionalnost
- ✅ Fiksni kredencijali u bazi podataka

## 🚀 Korak 1: Kreiraj Admin Users Tabelu u Bazi

Otvori Supabase Dashboard i pokreni SQL skriptu:

```
https://supabase.com/dashboard/project/wswizqibfqrmujlcfrav/sql
```

**Kopiraj i pokreni fajl:** `sql/create_admin_users_table.sql`

### Default Kredencijali

**VAŽNO**: Ovi kredencijali su defaultni i MORAJU biti promijenjeni!

```
Username: admin
Password: CaffeBar919!
```

## 🔧 Korak 2: Dodaj JWT Secret u .env

Dodaj sledeću liniju u `.env` fajl:

```env
JWT_SECRET="your-super-secret-key-change-this-in-production-min-32-characters"
```

**Preporuka**: Generiši random secret sa:
```bash
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```

## 📁 Kreirani Fajlovi

### Backend
- `lib/auth.ts` - Auth helper funkcije (verify, create session, JWT)
- `app/api/auth/login/route.ts` - POST /api/auth/login
- `app/api/auth/logout/route.ts` - POST /api/auth/logout
- `app/api/auth/session/route.ts` - GET /api/auth/session
- `middleware.ts` - Route protection sa JWT verifikacijom

### Frontend
- `app/admin/login/page.tsx` - Login forma
- `app/admin/events/page.tsx` - Updated sa logout dugmetom

### Database
- `sql/create_admin_users_table.sql` - Admin users tabela

## 🔐 Kako Funkcioniše

### 1. Login Flow

```
1. User ode na /admin/login
2. Unese username/password
3. POST /api/auth/login
   - Proverava kredencijale u bazi
   - Bcrypt compare sa hashom
   - Kreira JWT token (24h validity)
   - Postavlja httpOnly cookie
4. Redirect na /admin/events
```

### 2. Protected Routes

```
User pok ušava da ode na /admin/events
   ↓
Middleware provjerava JWT cookie
   ↓
├─ Valid token → Allow access
└─ Invalid/Missing → Redirect to /admin/login
```

### 3. Logout Flow

```
User klikne "Odjavi se"
   ↓
POST /api/auth/logout
   ↓
Briše admin-session cookie
   ↓
Redirect na /admin/login
```

## 🗄️ Database Schema

### admin_users tabela

```sql
CREATE TABLE admin_users (
  id UUID PRIMARY KEY,
  username TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,  -- bcrypt hash
  created_at TIMESTAMPTZ,
  last_login TIMESTAMPTZ
);
```

**RLS Policies:**
- **Service role only** - Samo backend može pristupiti tabeli
- Public nema pristup (sigurnost)

## 🔑 Promjena Lozinke (Manuelno)

Trenutno nema UI za promjenu lozinke. Promjena se vrši direktno u bazi:

### 1. Generiši novi hash:

```javascript
const bcrypt = require('bcryptjs');
const newPassword = 'MojaNovalozinka123!';
const hash = bcrypt.hashSync(newPassword, 10);
console.log(hash);
```

### 2. Update u bazi:

```sql
UPDATE admin_users
SET password_hash = '$2b$10$...'  -- tvoj novi hash
WHERE username = 'admin';
```

## 📝 API Endpoints

### POST /api/auth/login

**Request:**
```json
{
  "username": "admin",
  "password": "CaffeBar919!"
}
```

**Response (Success):**
```json
{
  "success": true,
  "user": {
    "id": "uuid",
    "username": "admin"
  }
}
```

**Response (Error):**
```json
{
  "error": "Neispravno korisničko ime ili lozinka"
}
```

### POST /api/auth/logout

**Response:**
```json
{
  "success": true
}
```

### GET /api/auth/session

**Response (Authenticated):**
```json
{
  "authenticated": true,
  "user": {
    "userId": "uuid",
    "username": "admin"
  }
}
```

**Response (Not Authenticated):**
```json
{
  "authenticated": false
}
```

## 🎨 UI Komponente

### Login Page (`/admin/login`)
- Elegantna forma u stilu aplikacije
- Lock ikona i gold akcenat
- Loading state tokom prijave
- Error poruke
- Link nazad na homepage

### Admin Events Page (`/admin/events`)
- Logout dugme u headeru (pored "Novi događaj")
- LogOut ikona
- Outline stil koji matuje ostale admin elemente

## 🔒 Sigurnost

### Implementirano:
- ✅ **Password Hashing** - bcrypt sa 10 rounds
- ✅ **JWT Tokens** - HS256 algorithm, 24h expiry
- ✅ **HttpOnly Cookies** - Zaštita od XSS napada
- ✅ **Secure Flag** - HTTPS samo u production
- ✅ **SameSite: Lax** - CSRF zaštita
- ✅ **Middleware Protection** - Server-side route guard
- ✅ **RLS Policies** - Database-level security

### Best Practices:
- Password hash se **NIKAD** ne vraća klijentu
- JWT secret je **OBAVEZAN** u .env
- Cookies su **httpOnly** (JavaScript ne može pristupiti)
- Admin tabela ima **RLS** (samo service role)

## 🐛 Troubleshooting

### "Neispravno korisničko ime ili lozinka"

1. Proveri da li je SQL skripta pokrenuta:
   ```sql
   SELECT * FROM admin_users;
   ```

2. Proveri da li password hash postoji:
   ```sql
   SELECT username, password_hash FROM admin_users WHERE username = 'admin';
   ```

3. Test password comparison:
   ```javascript
   const bcrypt = require('bcryptjs');
   const isValid = bcrypt.compareSync('CaffeBar919!', 'hash-from-database');
   console.log(isValid); // Should be true
   ```

### Session se ne čuva (redirect loop)

1. Proveri `.env` da JWT_SECRET postoji
2. Proveri browser cookies - da li `admin-session` postoji
3. Clear cookies i pokušaj ponovo

### Middleware redirect ne radi

1. Proveri `middleware.ts` config:
   ```typescript
   export const config = {
     matcher: ["/admin/events/:path*"],
   }
   ```

2. Proveri da cookie nije expired (24h validity)

## 🚀 Testing

### 1. Test Login

```bash
# Otvori browser
http://localhost:3000/admin/login

# Unesi:
Username: admin
Password: CaffeBar919!

# Trebao bi redirect na /admin/events
```

### 2. Test Protected Route

```bash
# Otvori Incognito/Private window
# Pokušaj pristupiti direktno:
http://localhost:3000/admin/events

# Trebao bi redirect na /admin/login
```

### 3. Test Logout

```bash
# Nakon logina, klikni "Odjavi se"
# Trebao bi redirect na /admin/login
# Cookie "admin-session" bi trebao biti obrisan
```

### 4. Test Session Persistence

```bash
# Logiraj se
# Zatvori tab
# Otvori novi tab i idi na /admin/events
# Trebao bi i dalje biti ulogovan (do 24h)
```

## 📊 Deployment Checklist

Pre deployment-a na Vercel:

- [ ] Promijeni default lozinku u bazi
- [ ] Generiši novi JWT_SECRET (min 32 karaktera)
- [ ] Dodaj JWT_SECRET u Vercel Environment Variables
- [ ] Testiraj login/logout flow u production
- [ ] Verifikuj da httpOnly cookies rade
- [ ] Proveri da /admin/events zahtijeva login

## 🔜 Buduća Poboljšanja

- [ ] UI za promjenu lozinke
- [ ] "Remember me" opcija (extended session)
- [ ] Logout sa svih uređaja odjednom
- [ ] Audit log (ko se logovao, kada)
- [ ] Multiple admin users (trenutno samo jedan)
- [ ] Password reset functionality
- [ ] 2FA (Two-Factor Authentication)
- [ ] Rate limiting na login endpoint
- [ ] Account lockout nakon X failed attempts

## 📖 Environment Variables Summary

Sve potrebne varijable u `.env`:

```env
# Supabase (već postoje)
NEXT_PUBLIC_SUPABASE_URL="https://wswizqibfqrmujlcfrav.supabase.co"
NEXT_PUBLIC_SUPABASE_ANON_KEY="..."
SUPABASE_SERVICE_ROLE_KEY="..."

# Authentication (DODAJ OVO)
JWT_SECRET="your-super-secret-key-min-32-characters"
```
