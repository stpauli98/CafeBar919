package dev.nextpixel.cafebar919.data

import androidx.room.TypeConverter

class Konverteri {
    @TypeConverter fun ulogaUTekst(u: Uloga): String = u.name
    @TypeConverter fun tekstUUlogu(t: String): Uloga = Uloga.valueOf(t)
    @TypeConverter fun vrstaUTekst(v: VrstaPrimaoca): String = v.name
    @TypeConverter fun tekstUVrstu(t: String): VrstaPrimaoca = VrstaPrimaoca.valueOf(t)
}
