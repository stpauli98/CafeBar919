package dev.nextpixel.cafebar919.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.nextpixel.cafebar919.data.dao.ArtikalDao
import dev.nextpixel.cafebar919.data.dao.KategorijaDao
import dev.nextpixel.cafebar919.data.dao.MjeraDao
import dev.nextpixel.cafebar919.data.dao.NocDao
import dev.nextpixel.cafebar919.data.dao.PostavkeDao
import dev.nextpixel.cafebar919.data.dao.RadnikDao
import dev.nextpixel.cafebar919.data.dao.RazlogDao

/** `synchronous = FULL`: potvrđena tura ne smije nestati ni kad tablet ostane bez struje. */
private object TrajniUpis : RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        db.query("PRAGMA synchronous = FULL").close()
    }
}

@Database(
    entities = [
        KategorijaE::class, MjeraE::class, ArtikalE::class, ArtikalMjeraE::class, RadnikE::class,
        RazlogE::class, PostavkeE::class, NocE::class, NocPrimalacE::class, TuraE::class,
        StavkaTureE::class, NacrtTureE::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Konverteri::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kategorijaDao(): KategorijaDao
    abstract fun mjeraDao(): MjeraDao
    abstract fun artikalDao(): ArtikalDao
    abstract fun radnikDao(): RadnikDao
    abstract fun razlogDao(): RazlogDao
    abstract fun postavkeDao(): PostavkeDao
    abstract fun nocDao(): NocDao

    companion object {
        /** `imeFajla = null` pravi bazu u memoriji. Nikad `fallbackToDestructiveMigration`. */
        fun napravi(context: Context, imeFajla: String? = "cafebar919.db"): AppDatabase {
            val app = context.applicationContext
            val builder = if (imeFajla == null) {
                Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            } else {
                Room.databaseBuilder(app, AppDatabase::class.java, imeFajla)
            }
            return builder.addCallback(PocetniPodaci.callback).addCallback(TrajniUpis).build()
        }
    }
}
