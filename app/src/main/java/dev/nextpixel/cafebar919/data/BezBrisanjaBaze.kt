package dev.nextpixel.cafebar919.data

import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory

/**
 * BAZA-01: oštećena baza se nikad ne briše.
 *
 * Podrazumijevani [SupportSQLiteOpenHelper.Callback.onCorruption] briše fajl baze, pa bi Room tiho napravio novu,
 * praznu bazu i sve noći bi nestale. Ovdje se oštećenje samo zapiše u log. Otvaranje zatim padne, [OtvaranjeBaze]
 * prijavi grešku i korisnik vidi BAZA-01, a fajl ostaje kakav jeste, da ga NextPixel može pregledati.
 */
class BezBrisanjaBaze(
    private val delegat: SupportSQLiteOpenHelper.Factory = FrameworkSQLiteOpenHelperFactory(),
) : SupportSQLiteOpenHelper.Factory {

    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        val room = configuration.callback
        val callback = object : SupportSQLiteOpenHelper.Callback(room.version) {
            override fun onConfigure(db: SupportSQLiteDatabase) = room.onConfigure(db)
            override fun onCreate(db: SupportSQLiteDatabase) = room.onCreate(db)
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = room.onUpgrade(db, oldVersion, newVersion)
            override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = room.onDowngrade(db, oldVersion, newVersion)
            override fun onOpen(db: SupportSQLiteDatabase) = room.onOpen(db)

            override fun onCorruption(db: SupportSQLiteDatabase) {
                Log.e("CafeBar919", "Baza je oštećena (${db.path}); fajl se ne briše (BAZA-01)")
            }
        }
        return delegat.create(
            SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
                .name(configuration.name)
                .callback(callback)
                .noBackupDirectory(configuration.useNoBackupDirectory)
                .allowDataLossOnRecovery(false)
                .build(),
        )
    }
}
