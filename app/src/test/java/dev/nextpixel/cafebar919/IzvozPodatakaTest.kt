package dev.nextpixel.cafebar919

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.xmlpull.v1.XmlPullParser
import java.io.File

/** Spec 9.3: podaci ne izlaze sa tableta; ni oblak ni prenos sa uređaja na uređaj ne smiju nositi ništa. */
@RunWith(RobolectricTestRunner::class)
class IzvozPodatakaTest {
    @Test fun manifestKoristiPraviloIzvlacenjaIBezBeckapa() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android:allowBackup=\"false\""))
        assertTrue(manifest.contains("android:dataExtractionRules=\"@xml/data_extraction_rules\""))
    }

    @Test fun pravilaIskljucujuSveZaOblakIPrenos() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val xml = ctx.resources.getXml(R.xml.data_extraction_rules)
        val isklj = mutableMapOf<String, MutableSet<String>>()
        var odjeljak = ""
        while (xml.next() != XmlPullParser.END_DOCUMENT) {
            if (xml.eventType != XmlPullParser.START_TAG) continue
            when (xml.name) {
                "cloud-backup", "device-transfer" -> odjeljak = xml.name
                "exclude" -> isklj.getOrPut(odjeljak) { mutableSetOf() }.add(xml.getAttributeValue(null, "domain"))
                "include" -> throw AssertionError("include nije dozvoljen u $odjeljak")
            }
        }
        val svi = setOf("root", "file", "database", "sharedpref", "external")
        assertEquals(svi, isklj["cloud-backup"])
        assertEquals(svi, isklj["device-transfer"])
    }
}
