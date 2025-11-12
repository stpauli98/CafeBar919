# SEO Setup - Sitemap & Robots.txt

## 🎯 Overview

Aplikacija sada ima kompletno SEO setup sa sitemap.xml i robots.txt za optimizaciju pretrage.

---

## 📄 Files Created

### 1. **Sitemap** (`app/sitemap.ts`)
**Lokacija**: `/sitemap.xml`
**URL**: `https://www.919bar.com/sitemap.xml`

**Funkcionalnost:**
- Dinamički Next.js sitemap (TypeScript)
- Automatski se generiše pri build-u
- Uključuje sve javne stranice
- Ima `lastModified`, `changeFrequency`, i `priority` meta podatke

**Trenutne Stranice u Sitemap-u:**
- ✅ Homepage (`/`) - Priority: 1.0, Change Frequency: weekly

**Šta NIJE u sitemap-u (namerno):**
- ❌ `/admin/login` - Private route, ne treba indexirati
- ❌ `/admin/events` - Private route, ne treba indexirati

### 2. **Robots.txt** (`public/robots.txt`)
**Lokacija**: `/robots.txt`
**URL**: `https://www.919bar.com/robots.txt`

**Funkcionalnost:**
- Dozvoljava svim crawlerima pristup javnim stranicama
- Blokira admin i API rute od indeksiranja
- Ukazuje na sitemap lokaciju

**Pravila:**
```txt
# Dozvoli crawlerima pristup javnim stranicama
User-agent: *
Allow: /

# Blokiraj admin panel i API
Disallow: /admin/
Disallow: /api/

# Sitemap lokacija
Sitemap: https://www.919bar.com/sitemap.xml
```

---

## 🔍 Kako Funkcionišu

### Sitemap.xml
Next.js automatski generiše `sitemap.xml` iz `app/sitemap.ts` fajla tokom build procesa:

```typescript
import { MetadataRoute } from "next"

export default function sitemap(): MetadataRoute.Sitemap {
  const baseUrl = "https://www.919bar.com"

  return [
    {
      url: baseUrl,
      lastModified: new Date(),
      changeFrequency: "weekly",
      priority: 1,
    },
  ]
}
```

**Output (sitemap.xml):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url>
    <loc>https://www.919bar.com</loc>
    <lastmod>2025-01-12</lastmod>
    <changefreq>weekly</changefreq>
    <priority>1.0</priority>
  </url>
</urlset>
```

### Robots.txt
Statički fajl koji se servira iz `public/` direktorijuma:
- Search enginei ga automatski čitaju
- Definišu pravila crawling-a
- Ukazuju na sitemap lokaciju

---

## 🧪 Testiranje

### 1. Lokalno Testiranje
```bash
# Start dev server
npm run dev

# Test sitemap
curl http://localhost:3000/sitemap.xml

# Test robots.txt
curl http://localhost:3000/robots.txt
```

### 2. Production Testiranje
```bash
# Test sitemap
curl https://www.919bar.com/sitemap.xml

# Test robots.txt
curl https://www.919bar.com/robots.txt
```

### 3. Google Search Console
1. Dodaj sajt u Google Search Console
2. Submit sitemap URL: `https://www.919bar.com/sitemap.xml`
3. Proveri da li Google uspešno čita sitemap

### 4. Robots.txt Validator
- Google: https://www.google.com/webmasters/tools/robots-testing-tool
- Online tool: https://support.google.com/webmasters/answer/6062598

---

## 📊 SEO Benefits

### ✅ Šta Je Poboljšano

1. **Crawler Discovery**: Search enginei lakše pronalaze stranice
2. **Indexing Speed**: Brže indeksiranje novih/ažuriranih stranica
3. **Privacy**: Admin panel nije javno dostupan search engineima
4. **Crawl Budget**: Blokirane nepotrebne stranice (API routes)
5. **Standards Compliance**: Standardizovan SEO setup

### 📈 Očekivani Rezultati

- **Google**: Indexira homepage u roku od 24-48h
- **Bing**: Indexira u roku od 1-2 nedelje
- **Organic Traffic**: Postepeni rast kroz nekoliko nedelja/meseci

---

## 🔧 Kako Dodati Nove Stranice

Ako dodaš nove javne stranice, ažuriraj `app/sitemap.ts`:

```typescript
export default function sitemap(): MetadataRoute.Sitemap {
  const baseUrl = "https://www.919bar.com"

  return [
    {
      url: baseUrl,
      lastModified: new Date(),
      changeFrequency: "weekly",
      priority: 1,
    },
    // Novi route
    {
      url: `${baseUrl}/about`,
      lastModified: new Date(),
      changeFrequency: "monthly",
      priority: 0.8,
    },
    {
      url: `${baseUrl}/contact`,
      lastModified: new Date(),
      changeFrequency: "monthly",
      priority: 0.7,
    },
  ]
}
```

**Priority Guidelines:**
- `1.0` - Homepage (najvažnija stranica)
- `0.8` - Važne landing pages (about, services)
- `0.5-0.7` - Sekundarne stranice (blog posts, events)
- `0.3-0.4` - Archive/old content

**Change Frequency Guidelines:**
- `always` - Real-time content (live feeds)
- `hourly` - Very dynamic content
- `daily` - Blog, news
- `weekly` - Regular updates (homepage)
- `monthly` - Relatively static pages
- `yearly` - Archived content
- `never` - Permanently static

---

## 🚀 Napredne Opcije (Buduće)

### 1. Dynamic Event Pages u Sitemap
Ako dodaš `/events/[id]` stranice:

```typescript
export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const baseUrl = "https://www.919bar.com"

  // Fetch events from API
  const events = await fetch(`${baseUrl}/api/events`).then(res => res.json())

  // Static routes
  const routes = [
    {
      url: baseUrl,
      lastModified: new Date(),
      changeFrequency: "weekly" as const,
      priority: 1,
    },
  ]

  // Dynamic event routes
  const eventRoutes = events.events.map((event: any) => ({
    url: `${baseUrl}/events/${event.id}`,
    lastModified: new Date(event.updated_at),
    changeFrequency: "weekly" as const,
    priority: 0.6,
  }))

  return [...routes, ...eventRoutes]
}
```

### 2. Multi-language Sitemap
Za multi-language sajt:

```typescript
<url>
  <loc>https://www.919bar.com</loc>
  <xhtml:link rel="alternate" hreflang="sr" href="https://www.919bar.com" />
  <xhtml:link rel="alternate" hreflang="en" href="https://www.919bar.com/en" />
</url>
```

### 3. Image Sitemap
Ako imaš mnogo slika:

```typescript
{
  url: `${baseUrl}/gallery`,
  images: [
    {
      loc: `${baseUrl}/images/event1.webp`,
      title: "Saturday Night Event",
      caption: "DJ Night at Caffe Bar 919",
    },
  ],
}
```

---

## ✅ Checklist Post-Deployment

- [ ] Submit sitemap to Google Search Console
- [ ] Submit sitemap to Bing Webmaster Tools
- [ ] Verify robots.txt is accessible
- [ ] Check sitemap.xml syntax with validator
- [ ] Monitor indexing status (1-2 weeks)
- [ ] Set up Google Analytics (if not already)
- [ ] Add structured data (JSON-LD) for local business

---

## 📚 Resources

- [Next.js Sitemap Documentation](https://nextjs.org/docs/app/api-reference/file-conventions/metadata/sitemap)
- [Google Sitemap Guidelines](https://developers.google.com/search/docs/crawling-indexing/sitemaps/build-sitemap)
- [Robots.txt Specification](https://developers.google.com/search/docs/crawling-indexing/robots/intro)
- [Google Search Console](https://search.google.com/search-console)

---

**Last Updated**: 2025-01-12
**Status**: ✅ Production Ready
**Build Size**: 143 B (sitemap.xml)