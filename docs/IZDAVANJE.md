# Izdavanje nove verzije aplikacije

## Pravilo

Uvijek isti ključ, `versionCode` raste, instalacija preko stare.

Android dozvoljava instalaciju preko postojeće aplikacije samo ako je potpisana istim ključem i ako je `versionCode` veći. Ako se ključ promijeni, mora se prvo obrisati stara aplikacija, a time se brišu i svi podaci na tabletu (artikli, radnici, podešavanja).

## Gdje je ključ

- Fajl ključa: `~/Desktop/2-Klijenti/Marinko919/Kljuc-potpisa/cafebar919-release.jks` (alias `cafebar919`).
- Lozinka je u macOS Keychain-u pod servisom `cafebar919-release` (nalog `nextpixel`).
- Gradle čita putanju i lozinke iz `~/.gradle/gradle.properties`: `CAFEBAR_STORE_FILE`, `CAFEBAR_STORE_PASSWORD`, `CAFEBAR_KEY_ALIAS`, `CAFEBAR_KEY_PASSWORD`.
- Ništa od toga ne ide u git. Lozinka se nikad ne upisuje u kod ni u dokumentaciju.

Napravi rezervnu kopiju fajla ključa i lozinke na sigurno mjesto van računara (npr. šifrovani USB ili menadžer lozinki).

## Šta ako se ključ izgubi

Nova verzija se više ne može instalirati preko stare. Jedini izlaz je napraviti novi ključ, obrisati aplikaciju na tabletu i instalirati je ponovo, pri čemu se gube svi podaci na tabletu. Zato čuvaj ključ.

## Pravljenje release APK-a

1. U `app/build.gradle.kts` povećaj `versionCode` (i po potrebi `versionName`).
2. Pokreni:

```
./gradlew assembleRelease
```

3. Rezultat je `app/build/outputs/apk/release/app-release.apk`. Ako u `~/.gradle/gradle.properties` nema svojstava za ključ, release build ne uspijeva; debug build i testovi rade i bez njih.

Provjera potpisa:

```
$ANDROID_HOME/build-tools/<verzija>/apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

Sertifikat mora biti `CN=Caffe Bar 919`, a SHA-256 mora počinjati sa `37:67:7C:FB`. Ako nije, APK nije potpisan pravim ključem i ne smije se instalirati.

## Instalacija na tablet

Uključi USB debugging na tabletu, poveži ga i pokreni:

```
adb install -r app/build/outputs/apk/release/app-release.apk
```

Opcija `-r` instalira preko stare verzije i čuva podatke.
