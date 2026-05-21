// port-lint: source tld.rs
package io.github.kotlinmania.chardetng

/* Any copyright is dedicated to the Public Domain.
 * https://creativecommons.org/publicdomain/zero/1.0/ */

enum class Tld {
    CentralWindows,
    Cyrillic,
    Western,
    Greek,
    TurkishAzeri,
    Hebrew,
    Arabic,
    Baltic,
    Vietnamese,
    Thai,
    Simplified,
    Traditional,
    Japanese,
    Korean,
    SimplifiedTraditional,
    TraditionalSimplified,
    CentralIso,
    IcelandicFaroese,
    WesternCyrillic,
    CentralCyrillic,
    WesternArabic,
    Generic,
    Eu,
}

fun classifyTld(tld: ByteArray): Tld {
    if (tld.size == 2) {
        val key = byteArrayOf(tld[0], tld[1])
        val i = TWO_LETTER_KEYS.binarySearchBytes(key)
        return if (i >= 0) TWO_LETTER_VALUES[i] else Tld.Western
    } else if (tld.size == 3) {
        val a = tld[0].toInt() and 0xff
        val b = tld[1].toInt() and 0xff
        val c = tld[2].toInt() and 0xff
        return if (
            (a == 'e'.code && b == 'd'.code && c == 'u'.code) ||
            (a == 'g'.code && b == 'o'.code && c == 'v'.code) ||
            (a == 'm'.code && b == 'i'.code && c == 'l'.code)
        ) {
            Tld.Western
        } else {
            Tld.Generic
        }
    } else if (tld.startsWithBytes(XN_DASH_DASH) && tld.size >= 8) {
        // It's unclear if including the IDNs here is a good idea.
        // Clearly, they are an anachronism relative to the era
        // of legacy encodings. The idea, consistent with previous
        // approach in Firefox is to address the case where one
        // of these TLDs is configured as an alternative name for
        // a server that also serves the same content from a
        // two-ASCII-letter TLD. This makes the detection result
        // the same either way even though otherwise this thing
        // does not make much sense.
        val rest = tld.copyOfRange(4, tld.size)
        val i = PUNYCODE_KEYS.binarySearchBytes(rest)
        return if (i >= 0) PUNYCODE_VALUES[i] else Tld.Generic
    } else {
        return Tld.Generic
    }
}

private val XN_DASH_DASH: ByteArray = ascii("xn--")

private fun ByteArray.startsWithBytes(prefix: ByteArray): Boolean {
    if (this.size < prefix.size) return false
    for (i in prefix.indices) {
        if (this[i] != prefix[i]) return false
    }
    return true
}

private fun Array<ByteArray>.binarySearchBytes(key: ByteArray): Int {
    var lo = 0
    var hi = this.size
    while (lo < hi) {
        val mid = (lo + hi) ushr 1
        val cmp = compareUnsignedBytes(this[mid], key)
        when {
            cmp < 0 -> lo = mid + 1
            cmp > 0 -> hi = mid
            else -> return mid
        }
    }
    return -(lo + 1)
}

private fun compareUnsignedBytes(a: ByteArray, b: ByteArray): Int {
    val n = minOf(a.size, b.size)
    for (i in 0 until n) {
        val ai = a[i].toInt() and 0xff
        val bi = b[i].toInt() and 0xff
        if (ai != bi) return ai - bi
    }
    return a.size - b.size
}

private fun ascii(s: String): ByteArray {
    val out = ByteArray(s.length)
    for (i in s.indices) {
        val c = s[i].code
        require(c in 0..0x7f) { "non-ASCII character in literal: $s" }
        out[i] = c.toByte()
    }
    return out
}

private val TWO_LETTER_VALUES: Array<Tld> = arrayOf(
    Tld.Generic,               // ac
    Tld.Arabic,                // ae
    Tld.Arabic,                // af
    Tld.Generic,               // ai
    Tld.WesternCyrillic,       // am
    Tld.TurkishAzeri,          // az
    Tld.CentralCyrillic,       // ba
    Tld.Cyrillic,              // bg
    Tld.Arabic,                // bh
    Tld.Cyrillic,              // by
    Tld.Generic,               // bz
    Tld.Generic,               // cb
    Tld.Generic,               // cc
    Tld.Generic,               // cd
    Tld.Simplified,            // cn
    Tld.Generic,               // cx
    Tld.Greek,                 // cy
    Tld.CentralWindows,        // cz
    Tld.Generic,               // dj
    Tld.Arabic,                // dz
    Tld.Arabic,                // eg
    Tld.Eu,                    // eu
    Tld.Generic,               // fm
    Tld.IcelandicFaroese,      // fo
    Tld.WesternCyrillic,       // ge
    Tld.Greek,                 // gr
    Tld.TraditionalSimplified, // hk
    Tld.CentralWindows,        // hr
    Tld.CentralIso,            // hu
    Tld.Hebrew,                // il
    Tld.Generic,               // in
    Tld.Arabic,                // iq
    Tld.Arabic,                // ir
    Tld.IcelandicFaroese,      // is
    Tld.Arabic,                // jo
    Tld.Japanese,              // jp
    Tld.Cyrillic,              // kg
    Tld.Korean,                // kp
    Tld.Korean,                // kr
    Tld.Arabic,                // kw
    Tld.Cyrillic,              // kz
    Tld.Generic,               // la
    Tld.Arabic,                // lb
    Tld.Baltic,                // lt
    Tld.Baltic,                // lv
    Tld.Arabic,                // ly
    Tld.Arabic,                // ma
    Tld.Cyrillic,              // md
    Tld.Generic,               // me
    Tld.Cyrillic,              // mk
    Tld.Cyrillic,              // mn
    Tld.TraditionalSimplified, // mo
    Tld.Arabic,                // mr
    Tld.Generic,               // ms
    Tld.WesternArabic,         // my
    Tld.Generic,               // nu
    Tld.Arabic,                // om
    Tld.Arabic,                // pk
    Tld.CentralIso,            // pl
    Tld.Arabic,                // ps
    Tld.Arabic,                // qa
    Tld.CentralWindows,        // ro
    Tld.Cyrillic,              // rs
    Tld.Cyrillic,              // ru
    Tld.Arabic,                // sa
    Tld.Arabic,                // sd
    Tld.SimplifiedTraditional, // sg
    Tld.CentralIso,            // si
    Tld.CentralWindows,        // sk
    Tld.Generic,               // st
    Tld.Cyrillic,              // su
    Tld.Arabic,                // sy
    Tld.Thai,                  // th
    Tld.Cyrillic,              // tj
    Tld.Generic,               // tk
    Tld.Cyrillic,              // tm
    Tld.Arabic,                // tn
    Tld.Generic,               // to
    Tld.TurkishAzeri,          // tr
    Tld.Generic,               // tv
    Tld.Traditional,           // tw
    Tld.Cyrillic,              // ua
    Tld.Cyrillic,              // uz
    Tld.Generic,               // vc
    Tld.Vietnamese,            // vn
    Tld.Generic,               // vu
    Tld.Arabic,                // ye
)

private val TWO_LETTER_KEYS: Array<ByteArray> = arrayOf(
    ascii("ac"), // Generic
    ascii("ae"), // Arabic
    ascii("af"), // Arabic
    ascii("ai"), // Generic
    ascii("am"), // WesternCyrillic
    ascii("az"), // TurkishAzeri
    ascii("ba"), // CentralCyrillic
    ascii("bg"), // Cyrillic
    ascii("bh"), // Arabic
    ascii("by"), // Cyrillic
    ascii("bz"), // Generic
    ascii("cb"), // Generic
    ascii("cc"), // Generic
    ascii("cd"), // Generic
    ascii("cn"), // Simplified
    ascii("cx"), // Generic
    ascii("cy"), // Greek
    ascii("cz"), // CentralWindows
    ascii("dj"), // Generic
    ascii("dz"), // Arabic
    ascii("eg"), // Arabic
    ascii("eu"), // Eu
    ascii("fm"), // Generic
    ascii("fo"), // IcelandicFaroese
    ascii("ge"), // WesternCyrillic
    ascii("gr"), // Greek
    ascii("hk"), // TraditionalSimplified
    ascii("hr"), // CentralWindows
    ascii("hu"), // CentralIso
    ascii("il"), // Hebrew
    ascii("in"), // Generic
    ascii("iq"), // Arabic
    ascii("ir"), // Arabic
    ascii("is"), // IcelandicFaroese
    ascii("jo"), // Arabic
    ascii("jp"), // Japanese
    ascii("kg"), // Cyrillic
    ascii("kp"), // Korean
    ascii("kr"), // Korean
    ascii("kw"), // Arabic
    ascii("kz"), // Cyrillic
    ascii("la"), // Generic
    ascii("lb"), // Arabic
    ascii("lt"), // Baltic
    ascii("lv"), // Baltic
    ascii("ly"), // Arabic
    ascii("ma"), // Arabic
    ascii("md"), // Cyrillic
    ascii("me"), // Generic
    ascii("mk"), // Cyrillic
    ascii("mn"), // Cyrillic
    ascii("mo"), // TraditionalSimplified
    ascii("mr"), // Arabic
    ascii("ms"), // Generic
    ascii("my"), // WesternArabic
    ascii("nu"), // Generic
    ascii("om"), // Arabic
    ascii("pk"), // Arabic
    ascii("pl"), // CentralIso
    ascii("ps"), // Arabic
    ascii("qa"), // Arabic
    ascii("ro"), // CentralWindows
    ascii("rs"), // Cyrillic
    ascii("ru"), // Cyrillic
    ascii("sa"), // Arabic
    ascii("sd"), // Arabic
    ascii("sg"), // SimplifiedTraditional
    ascii("si"), // CentralIso
    ascii("sk"), // CentralWindows
    ascii("st"), // Generic
    ascii("su"), // Cyrillic
    ascii("sy"), // Arabic
    ascii("th"), // Thai
    ascii("tj"), // Cyrillic
    ascii("tk"), // Generic
    ascii("tm"), // Cyrillic
    ascii("tn"), // Arabic
    ascii("to"), // Generic
    ascii("tr"), // TurkishAzeri
    ascii("tv"), // Generic
    ascii("tw"), // Traditional
    ascii("ua"), // Cyrillic
    ascii("uz"), // Cyrillic
    ascii("vc"), // Generic
    ascii("vn"), // Vietnamese
    ascii("vu"), // Generic
    ascii("ye"), // Arabic
)

private val PUNYCODE_KEYS: Array<ByteArray> = arrayOf(
    ascii("3e0b707e"),           // Korean
    ascii("54b7fta0cc"),         // Western
    ascii("80ao21a"),            // Cyrillic
    ascii("90a3ac"),             // Cyrillic
    ascii("90ae"),               // Cyrillic
    ascii("90ais"),              // Cyrillic
    ascii("clchc0ea0b2g2a9gcd"), // SimplifiedTraditional
    ascii("d1alf"),              // Cyrillic
    ascii("e1a4c"),              // Eu
    ascii("fiqs8S"),             // Simplified
    ascii("fiqz9S"),             // Simplified
    ascii("fzc2c9e2c"),          // Western
    ascii("j1amh"),              // Cyrillic
    ascii("j6w193g"),            // TraditionalSimplified
    ascii("kprw13d"),            // Traditional
    ascii("kpry57d"),            // Traditional
    ascii("l1acc"),              // Cyrillic
    ascii("lgbbat1ad8j"),        // Arabic
    ascii("mgb2ddes"),           // Arabic
    ascii("mgb9awbf"),           // Arabic
    ascii("mgba3a4f16a"),        // Arabic
    ascii("mgbaam7a8h"),         // Arabic
    ascii("mgbah1a3hjkrd"),      // Arabic
    ascii("mgbai9azgqp6j"),      // Arabic
    ascii("mgbayh7gpa"),         // Arabic
    ascii("mgbc0a9azcg"),        // Arabic
    ascii("mgbcpq6gpa1a"),       // Arabic
    ascii("mgberp4a5d4ar"),      // Arabic
    ascii("mgbpl2fh"),           // Arabic
    ascii("mgbtx2b"),            // Arabic
    ascii("mgbx4cd0ab"),         // WesternArabic
    ascii("mix891f"),            // TraditionalSimplified
    ascii("node"),               // WesternCyrillic
    ascii("o3cw4h"),             // Thai
    ascii("ogbpf8fl"),           // Arabic
    ascii("p1ai"),               // Cyrillic
    ascii("pgbs0dh"),            // Arabic
    ascii("q7ce6a"),             // Arabic
    ascii("qxa6a"),              // Eu
    ascii("qxam"),               // Greek
    ascii("wgbh1c"),             // Arabic
    ascii("wgbl6a"),             // Arabic
    ascii("xkc2al3hye2a"),       // Western
    ascii("y9a3aq"),             // WesternCyrillic
    ascii("yfro4i67o"),          // SimplifiedTraditional
    ascii("ygbi2ammx"),          // Arabic
)

private val PUNYCODE_VALUES: Array<Tld> = arrayOf(
    Tld.Korean,                // 3e0b707e
    Tld.Western,               // 54b7fta0cc
    Tld.Cyrillic,              // 80ao21a
    Tld.Cyrillic,              // 90a3ac
    Tld.Cyrillic,              // 90ae
    Tld.Cyrillic,              // 90ais
    Tld.SimplifiedTraditional, // clchc0ea0b2g2a9gcd
    Tld.Cyrillic,              // d1alf
    Tld.Eu,                    // e1a4c
    Tld.Simplified,            // fiqs8S
    Tld.Simplified,            // fiqz9S
    Tld.Western,               // fzc2c9e2c
    Tld.Cyrillic,              // j1amh
    Tld.TraditionalSimplified, // j6w193g
    Tld.Traditional,           // kprw13d
    Tld.Traditional,           // kpry57d
    Tld.Cyrillic,              // l1acc
    Tld.Arabic,                // lgbbat1ad8j
    Tld.Arabic,                // mgb2ddes
    Tld.Arabic,                // mgb9awbf
    Tld.Arabic,                // mgba3a4f16a
    Tld.Arabic,                // mgbaam7a8h
    Tld.Arabic,                // mgbah1a3hjkrd
    Tld.Arabic,                // mgbai9azgqp6j
    Tld.Arabic,                // mgbayh7gpa
    Tld.Arabic,                // mgbc0a9azcg
    Tld.Arabic,                // mgbcpq6gpa1a
    Tld.Arabic,                // mgberp4a5d4ar
    Tld.Arabic,                // mgbpl2fh
    Tld.Arabic,                // mgbtx2b
    Tld.WesternArabic,         // mgbx4cd0ab
    Tld.TraditionalSimplified, // mix891f
    Tld.WesternCyrillic,       // node
    Tld.Thai,                  // o3cw4h
    Tld.Arabic,                // ogbpf8fl
    Tld.Cyrillic,              // p1ai
    Tld.Arabic,                // pgbs0dh
    Tld.Arabic,                // q7ce6a
    Tld.Eu,                    // qxa6a
    Tld.Greek,                 // qxam
    Tld.Arabic,                // wgbh1c
    Tld.Arabic,                // wgbl6a
    Tld.Western,               // xkc2al3hye2a
    Tld.WesternCyrillic,       // y9a3aq
    Tld.SimplifiedTraditional, // yfro4i67o
    Tld.Arabic,                // ygbi2ammx
)
