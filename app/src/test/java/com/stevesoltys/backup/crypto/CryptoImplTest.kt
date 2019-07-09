package com.stevesoltys.backup.crypto

import com.stevesoltys.backup.header.HeaderReaderImpl
import com.stevesoltys.backup.header.HeaderWriterImpl
import com.stevesoltys.backup.header.IV_SIZE
import com.stevesoltys.backup.header.MAX_SEGMENT_LENGTH
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.crypto.Cipher
import kotlin.random.Random

@TestInstance(PER_METHOD)
class CryptoImplTest {

    private val cipherFactory = mockk<CipherFactory>()
    private val headerWriter = HeaderWriterImpl()
    private val headerReader = HeaderReaderImpl()

    private val crypto = CryptoImpl(cipherFactory, headerWriter, headerReader)

    private val cipher = mockk<Cipher>()

    private val iv = ByteArray(IV_SIZE).apply { Random.nextBytes(this) }
    private val cleartext = ByteArray(Random.nextInt(Short.MAX_VALUE.toInt()))
            .apply { Random.nextBytes(this) }
    private val ciphertext = ByteArray(Random.nextInt(Short.MAX_VALUE.toInt()))
            .apply { Random.nextBytes(this) }
    private val outputStream = ByteArrayOutputStream()

    @Test
    fun `encrypted cleartext gets decrypted as expected`() {
        every { cipherFactory.createEncryptionCipher() } returns cipher
        every { cipher.getOutputSize(cleartext.size) } returns MAX_SEGMENT_LENGTH
        every { cipher.doFinal(cleartext) } returns ciphertext
        every { cipher.iv } returns iv

        crypto.encryptSegment(outputStream, cleartext)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())

        every { cipherFactory.createDecryptionCipher(iv) } returns cipher
        every { cipher.doFinal(ciphertext) } returns cleartext

        assertArrayEquals(cleartext, crypto.decryptSegment(inputStream))
    }

}
