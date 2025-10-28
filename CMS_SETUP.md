# CMS Sistem za Događaje - Uputstvo za Postavljanje

## Pregled

CMS (Content Management System) omogućava vlasniku lokala da dinamički upravlja događajima bez potrebe za izmjenom koda. Događaji se čuvaju u Supabase PostgreSQL bazi podataka.

## 📋 Šta je implementirano

### Backend:
- ✅ Supabase klijent konfiguracija (`lib/supabase.ts`)
- ✅ REST API endpoint-i za CRUD operacije (`app/api/events/`)
- ✅ TypeScript tipovi za događaje
- ✅ Baza podataka schema (SQL skripta pripremljena)

### Frontend:
- ✅ Ažurirana `components/events.tsx` - prikazuje događaje iz baze
- ✅ Admin panel (`app/admin/events/page.tsx`) - upravljanje događajima
- ✅ Loading i error stanja
- ✅ Responzivni dizajn

## 🚀 Korak 1: Kreiranje Baze Podataka

Pošto Supabase MCP alati imaju problema sa dozvolama, potrebno je manuelno kreirati tabelu kroz Supabase Dashboard:

### Opcija A: Supabase Dashboard (Preporučeno)

1. Otvori Supabase Dashboard:
   ```
   https://supabase.com/dashboard/project/wswizqibfqrmujlcfrav/sql
   ```

2. Kopiraj cijeli sadržaj fajla `sql/create_events_table.sql`

3. Zalijepi u SQL Editor i pritisni **RUN**

4. Verifikuj da je tabela kreirana:
   ```sql
   SELECT * FROM events;
   ```

### Opcija B: Supabase CLI

```bash
npx supabase db push
```

## 🔧 Korak 2: Testiranje

### 1. Testiranje API endpoint-a

```bash
# Startuj development server
npm run dev

# U drugom terminalu, testiraj API
curl http://localhost:3000/api/events
```

### 2. Testiranje Admin Panela

Otvori browser:
```
http://localhost:3000/admin/events
```

**Funkcionalnosti admin panela:**
- ✅ Pregled svih događaja (aktivnih i neaktivnih)
- ✅ Kreiranje novog događaja
- ✅ Uređivanje postojećeg događaja
- ✅ Brisanje događaja (soft delete - postavlja `is_active = false`)

### 3. Testiranje Public Stranice

Otvori browser:
```
http://localhost:3000
```

Skroluj do sekcije "Događaji" - trebalo bi da vidiš događaje iz baze podataka.

## 📁 Struktura Projekta

```
caffe-bar-919/
├── app/
│   ├── api/
│   │   └── events/
│   │       ├── route.ts          # GET /api/events, POST /api/events
│   │       └── [id]/
│   │           └── route.ts      # GET, PATCH, DELETE /api/events/:id
│   └── admin/
│       └── events/
│           └── page.tsx          # Admin panel za upravljanje događajima
├── components/
│   └── events.tsx                # Komponenta za prikaz događaja (korisnici)
├── lib/
│   └── supabase.ts               # Supabase klijent i TypeScript tipovi
└── sql/
    └── create_events_table.sql   # SQL skripta za kreiranje tabele
```

## 🗄️ Šema Baze Podataka

```sql
CREATE TABLE events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title TEXT NOT NULL,
  date TEXT NOT NULL,
  time TEXT NOT NULL,
  description TEXT NOT NULL,
  type TEXT NOT NULL,
  image_url TEXT,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

### Polja:
- **id**: Jedinstveni identifikator (UUID)
- **title**: Naslov događaja (npr. "Saturday Night")
- **date**: Datum događaja (npr. "Subota, 12. Oktobar")
- **time**: Vrijeme događaja (npr. "22:00")
- **description**: Opis događaja
- **type**: Tip događaja (npr. "DJ Night", "Live Music", "Special Event")
- **image_url**: URL slike (opciono)
- **is_active**: Da li je događaj aktivan (prikazuje se korisnicima)
- **created_at**: Vrijeme kreiranja
- **updated_at**: Vrijeme posljednje izmjene (automatski se ažurira)

### Indeksi:
- `idx_events_is_active` - za brže pretraživanje aktivnih događaja
- `idx_events_date` - za sortiranje po datumu

### Row Level Security (RLS):
- **Public Read**: Svi mogu da vide aktivne događaje (`is_active = true`)
- **Admin Full Access**: Autentifikovani korisnici imaju pun pristup (CRUD)

## 🔐 Environment Varijable

Već konfigurisano u `.env`:
```env
NEXT_PUBLIC_SUPABASE_URL="https://wswizqibfqrmujlcfrav.supabase.co"
NEXT_PUBLIC_SUPABASE_ANON_KEY="eyJhbGci..."
SUPABASE_SERVICE_ROLE_KEY="eyJhbGci..."
```

## 📝 API Dokumentacija

### GET /api/events
Vraća sve aktivne događaje

**Response:**
```json
{
  "events": [
    {
      "id": "uuid",
      "title": "Saturday Night",
      "date": "Subota, 12. Oktobar",
      "time": "22:00",
      "description": "DJ set sa najboljim hitovima",
      "type": "DJ Night",
      "image_url": null,
      "is_active": true,
      "created_at": "2025-01-15T10:00:00Z",
      "updated_at": "2025-01-15T10:00:00Z"
    }
  ]
}
```

### POST /api/events
Kreira novi događaj

**Request Body:**
```json
{
  "title": "Acoustic Night",
  "date": "Petak, 18. Oktobar",
  "time": "20:00",
  "description": "Uživo akustična muzika",
  "type": "Live Music",
  "is_active": true
}
```

### PATCH /api/events/:id
Ažurira postojeći događaj

**Request Body:**
```json
{
  "title": "Updated Title",
  "is_active": false
}
```

### DELETE /api/events/:id
Briše događaj (soft delete - postavlja `is_active = false`)

## 🎨 UI Komponente

### Public Events (components/events.tsx)
- Prikazuje samo aktivne događaje
- Loading spinner dok se učitavaju podaci
- Error poruka ako API ne radi
- Empty state ako nema događaja
- Responzivan grid layout (1 kolona na mobilnom, 2 na tablet, 3 na desktop)

### Admin Panel (app/admin/events/page.tsx)
- Prikazuje sve događaje (aktivne i neaktivne)
- Modal forma za kreiranje/uređivanje
- Dugmići za uređivanje i brisanje
- Vizuelni indikator za neaktivne događaje
- Validacija forme

## 🔄 Workflow za Vlasnika Lokala

1. **Otvori admin panel**: `https://caffebar919.com/admin/events`
2. **Kreiraj novi događaj**: Klikni "Novi događaj"
3. **Popuni formu**:
   - Naslov (npr. "DJ Night")
   - Datum (npr. "Subota, 15. Januar")
   - Vrijeme (npr. "22:00")
   - Tip (npr. "DJ Night")
   - Opis (npr. "Poseban DJ set")
   - Aktivan (✓ da se prikaže korisnicima)
4. **Sačuvaj**: Događaj se odmah prikazuje na sajtu
5. **Uredi/Obriši**: Klikni "Uredi" ili "Obriši" na kartici događaja

## 🐛 Troubleshooting

### Događaji se ne prikazuju

1. Provjeri da li je SQL skripta pokrenuta:
   ```sql
   SELECT * FROM events WHERE is_active = true;
   ```

2. Provjeri browser console za greške

3. Provjeri API response:
   ```bash
   curl http://localhost:3000/api/events
   ```

### "Failed to fetch events" greška

1. Provjeri `.env` fajl - da li postoje sve varijable
2. Provjeri Supabase Dashboard - da li je projekat aktivan
3. Provjeri Row Level Security politike

### Admin panel ne čuva izmjene

1. Provjeri browser console za greške
2. Provjeri da li `SUPABASE_SERVICE_ROLE_KEY` postoji u `.env`
3. Provjeri Supabase logs za greške

## 📊 Sample Data

SQL skripta automatski dodaje 3 primjera događaja:
- Saturday Night (DJ Night)
- Acoustic Night (Live Music)
- Whiskey Night (Special Event)

## 🚀 Deployment

Nakon deployment-a na Vercel:
1. Dodaj sve environment varijable u Vercel Dashboard
2. Verifikuj da admin panel radi: `https://caffebar919.com/admin/events`
3. Testiraj kreiranje događaja

## 🔜 Buduća Poboljšanja

- [ ] Autentifikacija za admin panel (zaštita sa lozinkom)
- [ ] Upload slika direktno u Supabase Storage
- [ ] Filtriranje i pretraga događaja
- [ ] Paginacija za veliki broj događaja
- [ ] Email notifikacije za nove događaje
- [ ] Integracija sa kalendaom
- [ ] Analytics - broj pregleda po događaju
