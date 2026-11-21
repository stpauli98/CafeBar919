package dev.nextpixel.cafebar919.domain

import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** PIN se čuva samo kao PBKDF2-SHA256 heš sa solju (obje vrijednosti u Base64). */
object Pin {
    const val DUZINA = 6
    const val MAX_POGRESNIH = 10
    val BLOKADA: Duration = Duration.ofMinutes(5)

    private const val ITERACIJE = 20_000
    private const val BITA = 256
    private const val SO_BAJTOVA = 16

    fun novaSo(): String =
        Base64.getEncoder().encodeToString(ByteArray(SO_BAJTOVA).also { SecureRandom().nextBytes(it) })

    /** Vraća (hesBase64, soBase64). */
    fun hesiraj(pin: String, so: String = novaSo()): Pair<String, String> =
        izvedi(pin, so).let { Base64.getEncoder().encodeToString(it) to so }

    fun provjeri(pin: String, hes: String, so: String): Boolean =
        MessageDigest.isEqual(izvedi(pin, so), Base64.getDecoder().decode(hes))

    /** Zabranjeni su PIN-ovi sa svih šest istih cifara te „123456“ i „654321“. */
    fun zabranjen(pin: String): Boolean =
        (pin.length == DUZINA && pin.all { it == pin[0] }) || pin == "123456" || pin == "654321"

    private fun izvedi(pin: String, so: String): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), Base64.getDecoder().decode(so), ITERACIJE, BITA)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
