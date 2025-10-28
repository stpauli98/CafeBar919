# Supabase Authentication Setup

## Jednostavno podešavanje admin pristupa

Ova aplikacija koristi **Supabase Authentication** za zaštitu admin panela. Nema potrebe za dodatnim tabelama ili složenom konfiguracijom!

## 🔑 Dodavanje admin korisnika

### Korak 1: Otvori Supabase Dashboard
1. Idi na [supabase.com](https://supabase.com/dashboard)
2. Odaberi svoj projekat

### Korak 2: Dodaj novog korisnika
1. U levom meniju klikni na **Authentication**
2. Klikni na tab **Users**
3. Klikni dugme **Add User** (gore desno)
4. Popuni:
   - **Email**: tvoj admin email (npr. `admin@caffebar919.com`)
   - **Password**: bezbedna lozinka
   - **Auto Confirm User**: ✅ OBAVEZNO štikliraj (da bi odmah mogao da se prijaviš)
5. Klikni **Create User**

### Korak 3: Gotovo! 🎉
Sada možeš da se prijaviš na `/admin/login` sa email adresom i lozinkom koju si upravo kreirao.

## 🔐 Prijava

Admin panel se nalazi na: `/admin/events`

Kada pokušaš da pristupiš, biće preusmjeren na `/admin/login` gdje unosiš:
- **Email adresa** koja je registrovana u Supabase
- **Lozinka**

## 🔄 Kako funkcioniše

1. **Login**: Koristi `supabase.auth.signInWithPassword()`
2. **Session**: Supabase automatski upravlja session cookies
3. **Middleware**: Proverava `supabase.auth.getSession()` prije pristupa `/admin/events`
4. **Logout**: Koristi `supabase.auth.signOut()`

## ⚙️ Environment Variables

Proveri da imaš ove varijable u `.env.local`:

```env
NEXT_PUBLIC_SUPABASE_URL=tvoj_supabase_url
NEXT_PUBLIC_SUPABASE_ANON_KEY=tvoj_anon_key
SUPABASE_SERVICE_ROLE_KEY=tvoj_service_role_key
```

**Napomena**: Ne trebaš `JWT_SECRET` više! Supabase sve rešava automatski.

## 🚫 Šta je uklonjeno

Stari sistem sa custom JWT autentifikacijom je uklonjen:
- ~~lib/auth.ts~~ (više ne koristi)
- ~~app/api/auth/login/route.ts~~ (više ne koristi)
- ~~app/api/auth/logout/route.ts~~ (više ne koristi)
- ~~app/api/auth/session/route.ts~~ (više ne koristi)
- ~~sql/create_admin_users_table.sql~~ (više ne koristi)

Sve je sada **mnogo jednostavnije** sa Supabase Authentication! 🚀

## 📝 Dodavanje više admin korisnika

Jednostavno ponovi **Korak 2** za svakog novog admina. Nema potrebe za kodom ili SQL komandama.

## 🔧 Troubleshooting

### Problem: "Neispravna email adresa ili lozinka"
- Proveri da li si štikirao **Auto Confirm User** prilikom kreiranja
- Proveri da li koristiš tačnu email adresu (ne username!)
- Proveri da li je lozinka ispravna

### Problem: Redirekcija u loop
- Obriši cookies i cache u browseru
- Proveri environment variables
- Restartuj Next.js development server

### Problem: Middleware ne proverava session
- Proveri da su environment variables dostupne u `.env.local`
- Proveri `middleware.ts` config - matcher mora biti `["/admin/events/:path*"]`
