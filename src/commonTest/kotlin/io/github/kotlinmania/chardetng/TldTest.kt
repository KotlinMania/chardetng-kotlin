// port-lint: ignore
// Tests for Tld.kt — verifies the binary-search lookup matches the expected
// classifications hard-coded in tld.rs.
package io.github.kotlinmania.chardetng

import kotlin.test.Test
import kotlin.test.assertEquals

private fun bytes(s: String): ByteArray = ByteArray(s.length) { s[it].code.toByte() }

class TldTest {
    @Test
    fun twoLetterKnown() {
        assertEquals(Tld.Japanese, classifyTld(bytes("jp")))
        assertEquals(Tld.Korean, classifyTld(bytes("kr")))
        assertEquals(Tld.Simplified, classifyTld(bytes("cn")))
        assertEquals(Tld.Eu, classifyTld(bytes("eu")))
        assertEquals(Tld.Cyrillic, classifyTld(bytes("ru")))
        assertEquals(Tld.Hebrew, classifyTld(bytes("il")))
        assertEquals(Tld.Thai, classifyTld(bytes("th")))
        assertEquals(Tld.Arabic, classifyTld(bytes("sa")))
    }

    @Test
    fun twoLetterFirstAndLastEntries() {
        assertEquals(Tld.Generic, classifyTld(bytes("ac")))
        assertEquals(Tld.Arabic, classifyTld(bytes("ye")))
    }

    @Test
    fun twoLetterUnknownFallsBackToWestern() {
        assertEquals(Tld.Western, classifyTld(bytes("xy")))
        assertEquals(Tld.Western, classifyTld(bytes("zz")))
    }

    @Test
    fun threeLetterEduGovMil() {
        assertEquals(Tld.Western, classifyTld(bytes("edu")))
        assertEquals(Tld.Western, classifyTld(bytes("gov")))
        assertEquals(Tld.Western, classifyTld(bytes("mil")))
    }

    @Test
    fun threeLetterOtherIsGeneric() {
        assertEquals(Tld.Generic, classifyTld(bytes("com")))
        assertEquals(Tld.Generic, classifyTld(bytes("org")))
        assertEquals(Tld.Generic, classifyTld(bytes("net")))
    }

    @Test
    fun shortAndOddLengthsAreGeneric() {
        assertEquals(Tld.Generic, classifyTld(byteArrayOf()))
        assertEquals(Tld.Generic, classifyTld(bytes("a")))
        assertEquals(Tld.Generic, classifyTld(bytes("test")))
        assertEquals(Tld.Generic, classifyTld(bytes("museum")))
    }

    @Test
    fun punycodePrefixTooShortIsGeneric() {
        assertEquals(Tld.Generic, classifyTld(bytes("xn--")))
        assertEquals(Tld.Generic, classifyTld(bytes("xn--abc")))
    }

    @Test
    fun punycodeKnownClassifications() {
        assertEquals(Tld.Cyrillic, classifyTld(bytes("xn--p1ai")))
        assertEquals(Tld.Greek, classifyTld(bytes("xn--qxam")))
        assertEquals(Tld.Eu, classifyTld(bytes("xn--qxa6a")))
        assertEquals(Tld.Arabic, classifyTld(bytes("xn--ygbi2ammx")))
        assertEquals(Tld.Korean, classifyTld(bytes("xn--3e0b707e")))
        assertEquals(Tld.Thai, classifyTld(bytes("xn--o3cw4h")))
        assertEquals(Tld.WesternCyrillic, classifyTld(bytes("xn--node")))
    }

    @Test
    fun punycodeUnknownIsGeneric() {
        assertEquals(Tld.Generic, classifyTld(bytes("xn--zzzzzzzz")))
        assertEquals(Tld.Generic, classifyTld(bytes("xn--00000000")))
    }
}
