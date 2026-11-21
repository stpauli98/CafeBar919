package dev.nextpixel.cafebar919.domain

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** PIN se čuva samo kao PBKDF2-SHA256 heš sa solju (obje vrijednosti u Base64). */
object Pin {
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

    private fun izvedi(pin: String, so: String): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), Base64.getDecoder().decode(so), ITERACIJE, BITA)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
