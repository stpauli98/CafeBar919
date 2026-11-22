package dev.nextpixel.cafebar919.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.nextpixel.cafebar919.domain.Pin

/** Početni podaci iz spec 6.2, upisuju se jednom pri pravljenju baze. Bez kategorija i artikala. */
object PocetniPodaci {
    private const val PROBNI_PIN = "123456"

    val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            val (hes, so) = Pin.hesiraj(PROBNI_PIN)
            db.beginTransaction()
            try {
                // naziv, natpis, u_zbiru, litara, redoslijed
                val mjere = listOf(
                    arrayOf<Any?>("Komad", "Komad", "kom", null, 0),
                    arrayOf<Any?>("Doza", "0,03", "doza", 0.03, 1),
                    arrayOf<Any?>("Bokal", "Bokal", "bok.", 0.25, 2),
                    arrayOf<Any?>("Boca 0,7", "Boca", "boca 0,7", 0.70, 3),
                    arrayOf<Any?>("Boca 1 l", "Boca 1 l", "boca 1 l", 1.00, 4),
                )
                mjere.forEach {
                    db.execSQL("INSERT INTO mjera (naziv, natpis, u_zbiru, litara, redoslijed, aktivna) VALUES (?, ?, ?, ?, ?, 1)", it)
                }
                listOf("Vuleta" to "KONOBAR", "Srđan" to "KONOBAR", "Miloš" to "ZA_SANKOM", "Smitran" to "ZA_SANKOM")
                    .forEachIndexed { i, (ime, uloga) ->
                        db.execSQL("INSERT INTO radnik (ime, uloga, aktivan, redoslijed) VALUES (?, ?, 1, ?)", arrayOf<Any?>(ime, uloga, i))
                    }
                listOf("Pogrešan konobar", "Pogrešno piće", "Pogrešna količina", "Uneseno dvaput")
                    .forEachIndexed { i, tekst ->
                        db.execSQL("INSERT INTO razlog_ponistavanja (tekst, redoslijed, aktivan) VALUES (?, ?, 1)", arrayOf<Any?>(tekst, i))
                    }
                db.execSQL(
                    "INSERT INTO postavke (id, rok_bez_pina_s, istorija_dana, zakljucavanje_s, ispomoc_dozvoljena, " +
                        "pin_hes, pin_so, pin_probni, pin_pogresnih_zaredom, pin_blokiran_do) VALUES (1, 60, 30, 120, 1, ?, ?, 1, 0, NULL)",
                    arrayOf<Any?>(hes, so),
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
