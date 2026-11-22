package dev.nextpixel.cafebar919.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class StavkaNacrta(val artikalId: Long, val mjeraId: Long, val kolicina: Int)

object Nacrt {
    const val MAX_KOLICINA = 99

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(StavkaNacrta.serializer())

    fun kolicina(s: List<StavkaNacrta>, artikalId: Long, mjeraId: Long): Int =
        s.firstOrNull { it.artikalId == artikalId && it.mjeraId == mjeraId }?.kolicina ?: 0

    fun dodaj(s: List<StavkaNacrta>, artikalId: Long, mjeraId: Long): List<StavkaNacrta> =
        promijeni(s, artikalId, mjeraId, 1)

    fun promijeni(s: List<StavkaNacrta>, artikalId: Long, mjeraId: Long, delta: Int): List<StavkaNacrta> {
        val postoji = s.any { it.artikalId == artikalId && it.mjeraId == mjeraId }
        if (!postoji) {
            val nova = delta.coerceIn(0, MAX_KOLICINA)
            return if (nova == 0) s else s + StavkaNacrta(artikalId, mjeraId, nova)
        }
        return s.mapNotNull {
            if (it.artikalId == artikalId && it.mjeraId == mjeraId) {
                val nova = (it.kolicina + delta).coerceAtMost(MAX_KOLICINA)
                if (nova <= 0) null else it.copy(kolicina = nova)
            } else it
        }
    }

    fun uJson(s: List<StavkaNacrta>): String = json.encodeToString(serializer, s)

    /** Oštećen ili nečitljiv zapis je prazan nacrt; količine se svode na 1..99, duplikati se spajaju. */
    fun izJsona(json: String): List<StavkaNacrta> = try {
        val spojeno = LinkedHashMap<Pair<Long, Long>, Int>()
        this.json.decodeFromString(serializer, json).filter { it.kolicina > 0 }.forEach {
            val kljuc = it.artikalId to it.mjeraId
            spojeno[kljuc] = ((spojeno[kljuc] ?: 0) + it.kolicina).coerceAtMost(MAX_KOLICINA)
        }
        spojeno.map { (k, v) -> StavkaNacrta(k.first, k.second, v) }
    } catch (e: Exception) {
        emptyList()
    }

    /** Poruka uz onemogućeno „Potvrdi“ (spec 17); null kad se tura može potvrditi. */
    fun porukaPotvrde(imaPica: Boolean, imaPrimaoca: Boolean, imaStavki: Boolean): String? = when {
        !imaPica -> "Prvo treba dodati pića."
        !imaPrimaoca && !imaStavki -> "Izaberi konobara i dodaj piće."
        !imaStavki -> "Dodaj piće u turu."
        !imaPrimaoca -> "Izaberi konobara."
        else -> null
    }
}
