// port-lint: source lib.rs
package io.github.kotlinmania.chardetng

import io.github.kotlinmania.encodingrs.Ascii
import io.github.kotlinmania.encodingrs.Decoder
import io.github.kotlinmania.encodingrs.DecoderResult
import io.github.kotlinmania.encodingrs.Encoding
import io.github.kotlinmania.encodingrs.Encoding.Companion.BIG5
import io.github.kotlinmania.encodingrs.Encoding.Companion.EUC_JP
import io.github.kotlinmania.encodingrs.Encoding.Companion.EUC_KR
import io.github.kotlinmania.encodingrs.Encoding.Companion.GBK
import io.github.kotlinmania.encodingrs.Encoding.Companion.ISO_2022_JP
import io.github.kotlinmania.encodingrs.Encoding.Companion.ISO_8859_8
import io.github.kotlinmania.encodingrs.Encoding.Companion.SHIFT_JIS
import io.github.kotlinmania.encodingrs.Encoding.Companion.UTF_8
import io.github.kotlinmania.encodingrs.Encoding.Companion.WINDOWS_1255

internal const val LATIN_ADJACENCY_PENALTY: Long = -50L

internal const val IMPLAUSIBILITY_PENALTY: Long = -220L

internal const val ORDINAL_BONUS: Long = 300L

/**
 * Must match the ISO-8859-2 score for " Š ". Note: There
 * are four Slovenian Wikipedia list page titles where the
 * list is split by letter so that Š stands alone for the
 * list part for Š. Let's assume that's a special case not
 * worth detecting even though the copyright sign detection
 * makes Slovenian title detection round to one percentage
 * point worse.
 */
internal const val COPYRIGHT_BONUS: Long = 222L

internal const val IMPLAUSIBLE_LATIN_CASE_TRANSITION_PENALTY: Long = -180L

internal const val NON_LATIN_CAPITALIZATION_BONUS: Long = 40L

internal const val NON_LATIN_ALL_CAPS_PENALTY: Long = -40L

internal const val NON_LATIN_MIXED_CASE_PENALTY: Long = -20L

// Manually calibrated relative to windows-1256 Arabic
internal const val CJK_BASE_SCORE: Long = 41L

internal const val CJK_SECONDARY_BASE_SCORE: Long = 20L // Was 20

internal const val SHIFT_JIS_SCORE_PER_KANA: Long = 20L

internal const val SHIFT_JIS_SCORE_PER_LEVEL_1_KANJI: Long = CJK_BASE_SCORE

internal const val SHIFT_JIS_SCORE_PER_LEVEL_2_KANJI: Long = CJK_SECONDARY_BASE_SCORE

// Manually calibrated relative to windows-1256 Persian and Urdu
internal const val SHIFT_JIS_INITIAL_HALF_WIDTH_KATAKANA_PENALTY: Long = -75L

internal const val HALF_WIDTH_KATAKANA_SCORE: Long = 1L

// Unclear if this is a good idea; seems not harmful, but can't be sure.
internal const val HALF_WIDTH_KATAKANA_VOICING_SCORE: Long = 10L

internal const val SHIFT_JIS_PUA_PENALTY: Long = -(CJK_BASE_SCORE * 10L) // Should this be larger?

internal const val SHIFT_JIS_EXTENSION_PENALTY: Long = SHIFT_JIS_PUA_PENALTY * 2L

internal const val SHIFT_JIS_SINGLE_BYTE_EXTENSION_PENALTY: Long = SHIFT_JIS_EXTENSION_PENALTY

internal const val EUC_JP_SCORE_PER_KANA: Long = CJK_BASE_SCORE + (CJK_BASE_SCORE / 3L) // Relative to Big5

internal const val EUC_JP_SCORE_PER_NEAR_OBSOLETE_KANA: Long = CJK_BASE_SCORE - 1L

internal const val EUC_JP_SCORE_PER_LEVEL_1_KANJI: Long = CJK_BASE_SCORE

internal const val EUC_JP_SCORE_PER_LEVEL_2_KANJI: Long = CJK_SECONDARY_BASE_SCORE

internal const val EUC_JP_SCORE_PER_OTHER_KANJI: Long = CJK_SECONDARY_BASE_SCORE / 4L

internal const val EUC_JP_INITIAL_KANA_PENALTY: Long = -((CJK_BASE_SCORE / 3L) + 1L)

internal const val EUC_JP_EXTENSION_PENALTY: Long = -(CJK_BASE_SCORE * 50L) // Needs to be more severe than for Shift-JIS to avoid misdetecting EUC-KR!

internal const val BIG5_SCORE_PER_LEVEL_1_HANZI: Long = CJK_BASE_SCORE

internal const val BIG5_SCORE_PER_OTHER_HANZI: Long = CJK_SECONDARY_BASE_SCORE

internal const val BIG5_PUA_PENALTY: Long = -(CJK_BASE_SCORE * 30L) // More severe than other PUA penalties to avoid misdetecting EUC-KR! (25 as the multiplier is too little)

internal const val BIG5_SINGLE_BYTE_EXTENSION_PENALTY: Long = -(CJK_BASE_SCORE * 40L)

internal const val EUC_KR_SCORE_PER_EUC_HANGUL: Long = CJK_BASE_SCORE + 1L

internal const val EUC_KR_SCORE_PER_NON_EUC_HANGUL: Long = CJK_SECONDARY_BASE_SCORE / 5L

internal const val EUC_KR_SCORE_PER_HANJA: Long = CJK_SECONDARY_BASE_SCORE / 2L

internal const val EUC_KR_HANJA_AFTER_HANGUL_PENALTY: Long = -(CJK_BASE_SCORE * 10L)

internal const val EUC_KR_LONG_WORD_PENALTY: Long = -6L

internal const val GBK_PUA_PENALTY: Long = -(CJK_BASE_SCORE * 10L) // Factor should be at least 2, but should it be larger?

internal const val EUC_KR_PUA_PENALTY: Long = GBK_PUA_PENALTY - 1L // Break tie in favor of GBK

internal const val EUC_KR_MAC_KOREAN_PENALTY: Long = EUC_KR_PUA_PENALTY * 2L

internal const val EUC_KR_SINGLE_BYTE_EXTENSION_PENALTY: Long = EUC_KR_MAC_KOREAN_PENALTY

internal const val GBK_SCORE_PER_LEVEL_1: Long = CJK_BASE_SCORE

internal const val GBK_SCORE_PER_LEVEL_2: Long = CJK_SECONDARY_BASE_SCORE

internal const val GBK_SCORE_PER_NON_EUC: Long = CJK_SECONDARY_BASE_SCORE / 4L

internal const val GBK_SINGLE_BYTE_EXTENSION_PENALTY: Long = GBK_PUA_PENALTY * 4L

internal const val CJK_LATIN_ADJACENCY_PENALTY: Long = -CJK_BASE_SCORE // smaller penalty than LATIN_ADJACENCY_PENALTY

internal const val CJ_PUNCTUATION: Long = CJK_BASE_SCORE / 2L

internal const val CJK_OTHER: Long = CJK_SECONDARY_BASE_SCORE / 4L

/** Latin letter caseless class */
internal const val LATIN_LETTER: Byte = 1

internal fun containsUpperCasePeriodOrNonAscii(label: ByteArray): Boolean {
    for (b in label) {
        val u = b.toInt() and 0xFF
        if (u >= 0x80) {
            return true
        }
        if (u == '.'.code) {
            return true
        }
        if (u in 'A'.code..'Z'.code) {
            return true
        }
    }
    return false
}

// For Latin, we only penalize pairwise bad transitions
// if one participant is non-ASCII. This avoids violating
// the principle that ASCII pairs never contribute to the
// score. (Maybe that's a bad principle, though!)
internal enum class LatinCaseState {
    Space,
    Upper,
    Lower,
    AllCaps,
}

// For non-Latin, we calculate case-related penalty
// or bonus on a per-non-Latin-word basis.
internal enum class NonLatinCaseState {
    Space,
    Upper,
    Lower,
    UpperLower,
    AllCaps,
    Mix,
}

internal class NonLatinCasedCandidate(
    val data: SingleByteData,
) {
    private var prev: Int = 0
    private var caseState: NonLatinCaseState = NonLatinCaseState.Space
    private var prevAscii: Boolean = true
    internal var currentWordLen: Long = 0L
    internal var longestWord: Long = 0L
    private val ibm866: Boolean = data === SINGLE_BYTE_DATA[IBM866_INDEX]
    private var prevWasA0: Boolean = false

    fun feed(buffer: ByteArray): Long? {
        var score = 0L
        for (rawB in buffer) {
            val b = rawB.toInt() and 0xFF
            val classVal = data.classify(rawB)
            if (classVal == 255) {
                return null
            }
            val caselessClass = classVal and 0x7F

            val ascii = b < 0x80
            val asciiPair = prevAscii && ascii

            val nonAsciiAlphabetic = data.isNonLatinAlphabetic(caselessClass, false)

            // The purpose of this state machine is to avoid misdetecting Greek as
            // Cyrillic by:
            //
            // * Giving a small bonus to words that start with an upper-case letter
            //   and are lower-case for the rest.
            // * Giving a large penalty to start with one lower-case letter followed
            //   by all upper-case (obviously upper and lower case inverted, which
            //   unfortunately is possible due to KOI8-U).
            // * Giving a small per-word penalty to all-uppercase KOI8-U (to favor
            //   all-lowercase Greek over all-caps KOI8-U).
            // * Giving large penalties for mixed-case other than initial upper-case.
            //   This also helps relative to non-cased encodings.

            // ASCII doesn't participate in non-Latin casing.
            if (caselessClass == LATIN_LETTER.toInt()) {
                // Latin
                // Mark this word as a mess. If there end up being non-Latin
                // letters in this word, the ASCII-adjacency penalty gets
                // applied to Latin/non-Latin pairs and the mix penalty
                // to non-Latin/non-Latin pairs.
                caseState = NonLatinCaseState.Mix
            } else if (!nonAsciiAlphabetic) {
                // Space
                when (caseState) {
                    NonLatinCaseState.Space,
                    NonLatinCaseState.Upper,
                    NonLatinCaseState.Lower,
                    -> {}
                    NonLatinCaseState.UpperLower -> {
                        // Intentionally applied only once per word.
                        score += NON_LATIN_CAPITALIZATION_BONUS
                    }
                    NonLatinCaseState.AllCaps -> {
                        // Intentionally applied only once per word.
                        if (data === SINGLE_BYTE_DATA[KOI8_U_INDEX]) {
                            // Apply only to KOI8-U.
                            score += NON_LATIN_ALL_CAPS_PENALTY
                        }
                    }
                    NonLatinCaseState.Mix -> {
                        // Per letter
                        score += NON_LATIN_MIXED_CASE_PENALTY * currentWordLen
                    }
                }
                caseState = NonLatinCaseState.Space
            } else if ((classVal ushr 7) == 0) {
                // Lower case
                when (caseState) {
                    NonLatinCaseState.Space -> {
                        caseState = NonLatinCaseState.Lower
                    }
                    NonLatinCaseState.Upper -> {
                        caseState = NonLatinCaseState.UpperLower
                    }
                    NonLatinCaseState.Lower,
                    NonLatinCaseState.UpperLower,
                    NonLatinCaseState.Mix,
                    -> {}
                    NonLatinCaseState.AllCaps -> {
                        caseState = NonLatinCaseState.Mix
                    }
                }
            } else {
                // Upper case
                when (caseState) {
                    NonLatinCaseState.Space -> {
                        caseState = NonLatinCaseState.Upper
                    }
                    NonLatinCaseState.Upper -> {
                        caseState = NonLatinCaseState.AllCaps
                    }
                    NonLatinCaseState.Lower,
                    NonLatinCaseState.UpperLower,
                    -> {
                        caseState = NonLatinCaseState.Mix
                    }
                    NonLatinCaseState.AllCaps,
                    NonLatinCaseState.Mix,
                    -> {}
                }
            }

            if (nonAsciiAlphabetic) {
                currentWordLen += 1L
            } else {
                if (currentWordLen > longestWord) {
                    longestWord = currentWordLen
                }
                currentWordLen = 0L
            }

            val isA0 = b == 0xA0
            if (!asciiPair) {
                // 0xA0 is no-break space in many other encodings, so avoid
                // assigning score to IBM866 when 0xA0 occurs next to itself
                // or a space-like byte.
                if (!(ibm866 && ((isA0 && (prevWasA0 || prev == 0)) || (caselessClass == 0 && prevWasA0)))) {
                    score += data.score(caselessClass, prev, false)
                }

                if (prev == LATIN_LETTER.toInt() && nonAsciiAlphabetic) {
                    score += LATIN_ADJACENCY_PENALTY
                } else if (caselessClass == LATIN_LETTER.toInt() && data.isNonLatinAlphabetic(prev, false)) {
                    score += LATIN_ADJACENCY_PENALTY
                }
            }

            prevAscii = ascii
            prev = caselessClass
            prevWasA0 = isA0
        }
        return score
    }
}

internal enum class OrdinalState {
    Other,
    Space,
    PeriodAfterN,
    OrdinalExpectingSpace,
    OrdinalExpectingSpaceUndoImplausibility,
    OrdinalExpectingSpaceOrDigit,
    OrdinalExpectingSpaceOrDigitUndoImplausibily,
    UpperN,
    LowerN,
    FeminineAbbreviationStartLetter,
    Digit,
    Roman,
    Copyright,
}

internal class LatinCandidate(
    val data: SingleByteData,
) {
    private var prev: Int = 0
    private var caseState: LatinCaseState = LatinCaseState.Space
    private var prevNonAscii: Long = 0L
    private var ordinalState: OrdinalState = OrdinalState.Space
    private val windows1252: Boolean = data === SINGLE_BYTE_DATA[WINDOWS_1252_INDEX]

    fun feed(buffer: ByteArray): Long? {
        var score = 0L
        for (rawB in buffer) {
            val b = rawB.toInt() and 0xFF
            val classVal = data.classify(rawB)
            if (classVal == 255) {
                return null
            }
            val caselessClass = classVal and 0x7F

            val ascii = b < 0x80
            val asciiPair = prevNonAscii == 0L && ascii

            val nonAsciiPenalty: Long =
                when (prevNonAscii) {
                    0L, 1L, 2L -> 0L
                    3L -> -5L
                    4L -> -20L
                    else -> -200L
                }
            score += nonAsciiPenalty

            if (!data.isLatinAlphabetic(caselessClass)) {
                caseState = LatinCaseState.Space
            } else if ((classVal ushr 7) == 0) {
                // Penalizing lower case after two upper case
                // is important for avoiding misdetecting
                // windows-1250 as windows-1252 (byte 0x9F).
                if (caseState == LatinCaseState.AllCaps && !asciiPair) {
                    score += IMPLAUSIBLE_LATIN_CASE_TRANSITION_PENALTY
                }
                caseState = LatinCaseState.Lower
            } else {
                when (caseState) {
                    LatinCaseState.Space -> {
                        caseState = LatinCaseState.Upper
                    }
                    LatinCaseState.Upper,
                    LatinCaseState.AllCaps,
                    -> {
                        caseState = LatinCaseState.AllCaps
                    }
                    LatinCaseState.Lower -> {
                        if (!asciiPair) {
                            score += IMPLAUSIBLE_LATIN_CASE_TRANSITION_PENALTY
                        }
                        caseState = LatinCaseState.Upper
                    }
                }
            }

            // Treat pairing space-like, which can be non-ASCII, with ASCII as
            // ASCIIish enough not to get a score in order to avoid giving
            // ASCII i and I in windows-1254 next to windows-125x apostrophe/quote
            // a score. This avoids detecting English I’ as Turkish.
            val asciiIshPair =
                asciiPair ||
                    (ascii && prev == 0) ||
                    (caselessClass == 0 && prevNonAscii == 0L)

            if (!asciiIshPair) {
                score += data.score(caselessClass, prev, false)
            }

            if (windows1252) {
                // This state machine assigns score to the sequences
                // * " º " (Spanish)
                // * " ª " (Spanish)
                // * ".ª " (Spanish)
                // * ".º " (Spanish)
                // * "n.º1" (Spanish)
                // * " Mª " (Spanish)
                // * " Dª " (Spanish)
                // * " Nª " (Spanish)
                // * " Sª " (Spanish)
                // * " 3º " (Italian, where 3 is an ASCII digit)
                // * " 3ª " (Italian, where 3 is an ASCII digit)
                // * " Xº " (Italian, where X is a small Roman numeral)
                // * " Xª " (Italian, where X is a small Roman numeral)
                // * " Nº1" (Italian, where 1 is an ASCII digit)
                // * " Nº " (Italian)
                // * " © " (otherwise ASCII-only)
                // which are problematic to deal with by pairwise scoring
                // without messing up Romanian detection.
                when (ordinalState) {
                    OrdinalState.Other -> {
                        if (caselessClass == 0) {
                            ordinalState = OrdinalState.Space
                        }
                    }
                    OrdinalState.Space -> {
                        if (caselessClass == 0) {
                            // pass
                        } else if (b == 0xAA || b == 0xBA) {
                            ordinalState = OrdinalState.OrdinalExpectingSpace
                        } else if (b == 'M'.code || b == 'D'.code || b == 'S'.code) {
                            ordinalState = OrdinalState.FeminineAbbreviationStartLetter
                        } else if (b == 'N'.code) {
                            // numero or Nuestra
                            ordinalState = OrdinalState.UpperN
                        } else if (b == 'n'.code) {
                            // numero
                            ordinalState = OrdinalState.LowerN
                        } else if (caselessClass == ASCII_DIGIT) {
                            ordinalState = OrdinalState.Digit
                        } else if (caselessClass == 9 /* I */ || caselessClass == 22 /* V */ || caselessClass == 24 /* X */) {
                            ordinalState = OrdinalState.Roman
                        } else if (b == 0xA9) {
                            ordinalState = OrdinalState.Copyright
                        } else {
                            ordinalState = OrdinalState.Other
                        }
                    }
                    OrdinalState.OrdinalExpectingSpace -> {
                        if (caselessClass == 0) {
                            score += ORDINAL_BONUS
                            ordinalState = OrdinalState.Space
                        } else {
                            ordinalState = OrdinalState.Other
                        }
                    }
                    OrdinalState.OrdinalExpectingSpaceUndoImplausibility -> {
                        if (caselessClass == 0) {
                            score += ORDINAL_BONUS - IMPLAUSIBILITY_PENALTY
                            ordinalState = OrdinalState.Space
                        } else {
                            ordinalState = OrdinalState.Other
                        }
                    }
                    OrdinalState.OrdinalExpectingSpaceOrDigit -> {
                        if (caselessClass == 0) {
                            score += ORDINAL_BONUS
                            ordinalState = OrdinalState.Space
                        } else if (caselessClass == ASCII_DIGIT) {
                            score += ORDINAL_BONUS
                            ordinalState = OrdinalState.Other
                        } else {
                            ordinalState = OrdinalState.Other
                        }
                    }
                    OrdinalState.OrdinalExpectingSpaceOrDigitUndoImplausibily -> {
                        if (caselessClass == 0) {
                            score += ORDINAL_BONUS - IMPLAUSIBILITY_PENALTY
                            ordinalState = OrdinalState.Space
                        } else if (caselessClass == ASCII_DIGIT) {
                            score += ORDINAL_BONUS - IMPLAUSIBILITY_PENALTY
                            ordinalState = OrdinalState.Other
                        } else {
                            ordinalState = OrdinalState.Other
                        }
                    }
                    OrdinalState.UpperN -> {
                        if (b == 0xAA) {
                            ordinalState = OrdinalState.OrdinalExpectingSpaceUndoImplausibility
                        } else if (b == 0xBA) {
                            ordinalState = OrdinalState.OrdinalExpectingSpaceOrDigitUndoImplausibily
                        } else if (b == '.'.code) {
                            ordinalState = OrdinalState.PeriodAfterN
                        } else if (caselessClass == 0) {
                            ordinalState = OrdinalState.Space
                        } else {
                            ordinalState = OrdinalState.Other
                        }
                    }
                    OrdinalState.LowerN -> {
                        if (b == 0xBA) {
                            ordinalState = OrdinalState.OrdinalExpectingSpaceOrDigitUndoImplausibily
                        } else if (b == '.'.code) {
                            ordinalState = OrdinalState.PeriodAfterN
                        } else if (caselessClass == 0) {
                            ordinalState = OrdinalState.Space
                        } else {
                            ordinalState = OrdinalState.Other
                        }
                    }
                    OrdinalState.FeminineAbbreviationStartLetter -> {
                        if (b == 0xAA) {
                            ordinalState = OrdinalState.OrdinalExpectingSpaceUndoImplausibility
                        } else if (caselessClass == 0) {
                            ordinalState = OrdinalState.Space
                        } else {
                            ordinalState = OrdinalState.Other
                        }
                    }
                    OrdinalState.Digit -> {
                        if (b == 0xAA || b == 0xBA) {
                            ordinalState = OrdinalState.OrdinalExpectingSpace
                        } else if (caselessClass == 0) {
                            ordinalState = OrdinalState.Space
                        } else if (caselessClass == ASCII_DIGIT) {
                            // pass
                        } else {
                            ordinalState = OrdinalState.Other
                        }
                    }
                    OrdinalState.Roman -> {
                        if (b == 0xAA || b == 0xBA) {
                            ordinalState = OrdinalState.OrdinalExpectingSpaceUndoImplausibility
                        } else if (caselessClass == 0) {
                            ordinalState = OrdinalState.Space
                        } else if (caselessClass == 9 /* I */ || caselessClass == 22 /* V */ || caselessClass == 24 /* X */) {
                            // pass
                        } else {
                            ordinalState = OrdinalState.Other
                        }
                    }
                    OrdinalState.PeriodAfterN -> {
                        if (b == 0xBA) {
                            ordinalState = OrdinalState.OrdinalExpectingSpaceOrDigit
                        } else if (caselessClass == 0) {
                            ordinalState = OrdinalState.Space
                        } else {
                            ordinalState = OrdinalState.Other
                        }
                    }
                    OrdinalState.Copyright -> {
                        if (caselessClass == 0) {
                            score += COPYRIGHT_BONUS
                            ordinalState = OrdinalState.Space
                        } else {
                            ordinalState = OrdinalState.Other
                        }
                    }
                }
            }

            if (ascii) {
                prevNonAscii = 0L
            } else {
                prevNonAscii += 1L
            }
            prev = caselessClass
        }
        return score
    }
}

internal class ArabicFrenchCandidate(
    val data: SingleByteData,
) {
    private var prev: Int = 0
    private var caseState: LatinCaseState = LatinCaseState.Space
    private var prevAscii: Boolean = true
    internal var currentWordLen: Long = 0L
    internal var longestWord: Long = 0L

    fun feed(buffer: ByteArray): Long? {
        var score = 0L
        for (rawB in buffer) {
            val b = rawB.toInt() and 0xFF
            val classVal = data.classify(rawB)
            if (classVal == 255) {
                return null
            }
            val caselessClass = classVal and 0x7F

            val ascii = b < 0x80
            val asciiPair = prevAscii && ascii

            if (caselessClass != LATIN_LETTER.toInt()) {
                // We compute case penalties for French only
                caseState = LatinCaseState.Space
            } else if ((classVal ushr 7) == 0) {
                if (caseState == LatinCaseState.AllCaps && !asciiPair) {
                    score += IMPLAUSIBLE_LATIN_CASE_TRANSITION_PENALTY
                }
                caseState = LatinCaseState.Lower
            } else {
                when (caseState) {
                    LatinCaseState.Space -> {
                        caseState = LatinCaseState.Upper
                    }
                    LatinCaseState.Upper,
                    LatinCaseState.AllCaps,
                    -> {
                        caseState = LatinCaseState.AllCaps
                    }
                    LatinCaseState.Lower -> {
                        if (!asciiPair) {
                            score += IMPLAUSIBLE_LATIN_CASE_TRANSITION_PENALTY
                        }
                        caseState = LatinCaseState.Upper
                    }
                }
            }

            // Count only Arabic word length and ignore French
            val nonAsciiAlphabetic = data.isNonLatinAlphabetic(caselessClass, true)
            if (nonAsciiAlphabetic) {
                currentWordLen += 1L
            } else {
                if (currentWordLen > longestWord) {
                    longestWord = currentWordLen
                }
                currentWordLen = 0L
            }

            if (!asciiPair) {
                score += data.score(caselessClass, prev, true)

                if (prev == LATIN_LETTER.toInt() && nonAsciiAlphabetic) {
                    score += LATIN_ADJACENCY_PENALTY
                } else if (caselessClass == LATIN_LETTER.toInt() && data.isNonLatinAlphabetic(prev, true)) {
                    score += LATIN_ADJACENCY_PENALTY
                }
            }

            prevAscii = ascii
            prev = caselessClass
        }
        return score
    }
}

internal class CaselessCandidate(
    val data: SingleByteData,
) {
    private var prev: Int = 0
    private var prevAscii: Boolean = true
    internal var currentWordLen: Long = 0L
    internal var longestWord: Long = 0L

    fun feed(buffer: ByteArray): Long? {
        var score = 0L
        for (rawB in buffer) {
            val b = rawB.toInt() and 0xFF
            val classVal = data.classify(rawB)
            if (classVal == 255) {
                return null
            }
            val caselessClass = classVal and 0x7F

            val ascii = b < 0x80
            val asciiPair = prevAscii && ascii

            val nonAsciiAlphabetic = data.isNonLatinAlphabetic(caselessClass, false)
            if (nonAsciiAlphabetic) {
                currentWordLen += 1L
            } else {
                if (currentWordLen > longestWord) {
                    longestWord = currentWordLen
                }
                currentWordLen = 0L
            }

            if (!asciiPair) {
                score += data.score(caselessClass, prev, false)

                if (prev == LATIN_LETTER.toInt() && nonAsciiAlphabetic) {
                    score += LATIN_ADJACENCY_PENALTY
                } else if (caselessClass == LATIN_LETTER.toInt() && data.isNonLatinAlphabetic(prev, false)) {
                    score += LATIN_ADJACENCY_PENALTY
                }
            }

            prevAscii = ascii
            prev = caselessClass
        }
        return score
    }
}

private fun isAsciiPunctuation(byte: Byte): Boolean {
    val b = byte.toInt() and 0xFF
    return when (b.toChar()) {
        '.', ',', ':', ';', '?', '!' -> true
        else -> false
    }
}

internal class LogicalCandidate(
    val data: SingleByteData,
) {
    private var prev: Int = 0
    private var prevAscii: Boolean = true
    internal var plausiblePunctuation: Long = 0L
    internal var currentWordLen: Long = 0L
    internal var longestWord: Long = 0L

    fun feed(buffer: ByteArray): Long? {
        var score = 0L
        for (rawB in buffer) {
            val b = rawB.toInt() and 0xFF
            val classVal = data.classify(rawB)
            if (classVal == 255) {
                return null
            }
            val caselessClass = classVal and 0x7F

            val ascii = b < 0x80
            val asciiPair = prevAscii && ascii

            val nonAsciiAlphabetic = data.isNonLatinAlphabetic(caselessClass, false)
            if (nonAsciiAlphabetic) {
                currentWordLen += 1L
            } else {
                if (currentWordLen > longestWord) {
                    longestWord = currentWordLen
                }
                currentWordLen = 0L
            }

            if (!asciiPair) {
                score += data.score(caselessClass, prev, false)

                val prevNonAsciiAlphabetic = data.isNonLatinAlphabetic(prev, false)
                if (caselessClass == 0 && prevNonAsciiAlphabetic && isAsciiPunctuation(rawB)) {
                    plausiblePunctuation += 1L
                }

                if (prev == LATIN_LETTER.toInt() && nonAsciiAlphabetic) {
                    score += LATIN_ADJACENCY_PENALTY
                } else if (caselessClass == LATIN_LETTER.toInt() && prevNonAsciiAlphabetic) {
                    score += LATIN_ADJACENCY_PENALTY
                }
            }

            prevAscii = ascii
            prev = caselessClass
        }
        return score
    }
}

internal class VisualCandidate(
    val data: SingleByteData,
) {
    private var prev: Int = 0
    private var prevAscii: Boolean = true
    private var prevPunctuation: Boolean = false
    internal var plausiblePunctuation: Long = 0L
    internal var currentWordLen: Long = 0L
    internal var longestWord: Long = 0L

    fun feed(buffer: ByteArray): Long? {
        var score = 0L
        for (rawB in buffer) {
            val b = rawB.toInt() and 0xFF
            val classVal = data.classify(rawB)
            if (classVal == 255) {
                return null
            }
            val caselessClass = classVal and 0x7F

            val ascii = b < 0x80
            val asciiPair = prevAscii && ascii

            val nonAsciiAlphabetic = data.isNonLatinAlphabetic(caselessClass, false)
            if (nonAsciiAlphabetic) {
                currentWordLen += 1L
            } else {
                if (currentWordLen > longestWord) {
                    longestWord = currentWordLen
                }
                currentWordLen = 0L
            }

            if (!asciiPair) {
                score += data.score(caselessClass, prev, false)

                if (nonAsciiAlphabetic && prevPunctuation) {
                    plausiblePunctuation += 1L
                }

                if (prev == LATIN_LETTER.toInt() && nonAsciiAlphabetic) {
                    score += LATIN_ADJACENCY_PENALTY
                } else if (caselessClass == LATIN_LETTER.toInt() && data.isNonLatinAlphabetic(prev, false)) {
                    score += LATIN_ADJACENCY_PENALTY
                }
            }

            prevAscii = ascii
            prev = caselessClass
            prevPunctuation = caselessClass == 0 && isAsciiPunctuation(rawB)
        }
        return score
    }
}

internal class Utf8Candidate(
    private val decoder: Decoder = UTF_8.newDecoderWithoutBomHandling(),
) {
    fun feed(buffer: ByteArray, last: Boolean): Long? {
        val dst = ByteArray(1024)
        var totalRead = 0
        while (true) {
            val slice = if (totalRead == 0) buffer else buffer.copyOfRange(totalRead, buffer.size)
            val (result, read, _) = decoder.decodeToUtf8WithoutReplacement(slice, dst, last)
            totalRead += read
            when (result) {
                is DecoderResult.InputEmpty -> return 0L
                is DecoderResult.Malformed -> return null
                is DecoderResult.OutputFull -> continue
            }
        }
    }
}

internal class Iso2022Candidate(
    private val decoder: Decoder = ISO_2022_JP.newDecoderWithoutBomHandling(),
) {
    fun feed(buffer: ByteArray, last: Boolean): Long? {
        val dst = CharArray(1024)
        var totalRead = 0
        while (true) {
            val slice = if (totalRead == 0) buffer else buffer.copyOfRange(totalRead, buffer.size)
            val (result, read, _) = decoder.decodeToUtf16Raw(slice, dst, last)
            totalRead += read
            when (result) {
                is DecoderResult.InputEmpty -> return 0L
                is DecoderResult.Malformed -> return null
                is DecoderResult.OutputFull -> continue
            }
        }
    }
}

internal enum class LatinCj {
    AsciiLetter,
    Cj,
    Other,
}

internal enum class HalfWidthKatakana {
    DakutenForbidden,
    DakutenAllowed,
    DakutenOrHandakutenAllowed,
}

internal enum class LatinKorean {
    AsciiLetter,
    Hangul,
    Hanja,
    Other,
}

internal fun cjkExtraScore(u: Int, table: IntArray): Long {
    val pos = table.indexOf(u)
    return if (pos >= 0) {
        ((128 - pos) / 16).toLong()
    } else {
        0L
    }
}

internal class GbkCandidate(
    private var decoder: Decoder = GBK.newDecoderWithoutBomHandling(),
    private var prevByte: Int = 0,
    private var prev: LatinCj = LatinCj.Other,
    private var pendingScore: Long? = null,
) {
    private fun maybeSetAsPending(s: Long): Long {
        check(pendingScore == null)
        return if (prev == LatinCj.Cj || !moreProblematicLead(prevByte)) {
            s
        } else {
            pendingScore = s
            0L
        }
    }

    fun feed(buffer: ByteArray, last: Boolean): Long? {
        var score = 0L
        val src = ByteArray(1)
        val dst = CharArray(2)
        for (rawB in buffer) {
            val b = rawB.toInt() and 0xFF
            src[0] = rawB
            val (result, _, written) = decoder.decodeToUtf16Raw(src, dst, false)
            if (written == 1) {
                val u = dst[0].code
                if ((u in 'a'.code..'z'.code) || (u in 'A'.code..'Z'.code)) {
                    pendingScore = null
                    if (prev == LatinCj.Cj) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.AsciiLetter
                } else if (u == 0x20AC) {
                    pendingScore = null
                    prev = LatinCj.Other
                } else if (u in 0x4E00..0x9FA5) {
                    val pending = pendingScore
                    if (pending != null) {
                        score += pending
                        pendingScore = null
                    }
                    if (b in 0xA1..0xFE) {
                        when (prevByte) {
                            in 0xA1..0xD7 -> {
                                score += GBK_SCORE_PER_LEVEL_1
                                score += cjkExtraScore(u, DETECTOR_DATA.frequentSimplified)
                            }
                            in 0xD8..0xFE -> score += GBK_SCORE_PER_LEVEL_2
                            else -> score += GBK_SCORE_PER_NON_EUC
                        }
                    } else {
                        score += maybeSetAsPending(GBK_SCORE_PER_NON_EUC)
                    }
                    if (prev == LatinCj.AsciiLetter) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.Cj
                } else if ((u in 0x3400 until 0xA000) || (u in 0xF900 until 0xFB00)) {
                    val pending = pendingScore
                    if (pending != null) {
                        score += pending
                        pendingScore = null
                    }
                    if (prev == LatinCj.AsciiLetter) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.Cj
                } else if (u in 0xE000 until 0xF900) {
                    val pending = pendingScore
                    if (pending != null) {
                        score += pending
                        pendingScore = null
                    }
                    when (u) {
                        in 0xE78D..0xE796,
                        in 0xE816..0xE818,
                        0xE81E,
                        0xE826,
                        0xE82B,
                        0xE82C,
                        0xE831,
                        0xE832,
                        0xE83B,
                        0xE843,
                        0xE854,
                        0xE855,
                        0xE864,
                        -> {
                            score += GBK_SCORE_PER_NON_EUC
                            if (prev == LatinCj.AsciiLetter) {
                                score += CJK_LATIN_ADJACENCY_PENALTY
                            }
                            prev = LatinCj.Cj
                        }
                        else -> {
                            score += GBK_PUA_PENALTY
                            prev = LatinCj.Other
                        }
                    }
                } else {
                    when (u) {
                        0x3000,
                        0x3001,
                        0x3002,
                        0xFF08,
                        0xFF09,
                        0xFF01,
                        0xFF0C,
                        0xFF1B,
                        0xFF1F,
                        -> {
                            val pending = pendingScore
                            if (pending != null) {
                                score += pending
                                pendingScore = null
                            }
                            score += CJ_PUNCTUATION
                        }
                        in 0..0x7F -> {
                            pendingScore = null
                        }
                        else -> {
                            val pending = pendingScore
                            if (pending != null) {
                                score += pending
                                pendingScore = null
                            }
                            score += CJK_OTHER
                        }
                    }
                    prev = LatinCj.Other
                }
            } else if (written == 2) {
                val pending = pendingScore
                if (pending != null) {
                    score += pending
                    pendingScore = null
                }
                val u = dst[0].code
                if (u in 0xDB80..0xDBFF) {
                    score += GBK_PUA_PENALTY
                    prev = LatinCj.Other
                } else if (u in 0xD480 until 0xD880) {
                    score += GBK_SCORE_PER_NON_EUC
                    if (prev == LatinCj.AsciiLetter) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.Cj
                } else {
                    score += CJK_OTHER
                    prev = LatinCj.Other
                }
            }
            when (result) {
                is DecoderResult.InputEmpty -> {}
                is DecoderResult.Malformed -> {
                    val malformedLen = result.length.toInt()
                    if ((prevByte == 0xA0 || prevByte == 0xFE || prevByte == 0xFD) &&
                        (b < 0x80 || b == 0xFF)
                    ) {
                        pendingScore = null
                        score += GBK_SINGLE_BYTE_EXTENSION_PENALTY
                        if ((b in 'a'.code..'z'.code) || (b in 'A'.code..'Z'.code)) {
                            prev = LatinCj.AsciiLetter
                        } else if (b == 0xFF) {
                            score += GBK_SINGLE_BYTE_EXTENSION_PENALTY
                            prev = LatinCj.Other
                        } else {
                            prev = LatinCj.Other
                        }
                        decoder = GBK.newDecoderWithoutBomHandling()
                    } else if (malformedLen == 1 && b == 0xFF) {
                        pendingScore = null
                        score += GBK_SINGLE_BYTE_EXTENSION_PENALTY
                        prev = LatinCj.Other
                        decoder = GBK.newDecoderWithoutBomHandling()
                    } else {
                        return null
                    }
                }
                is DecoderResult.OutputFull -> error("unreachable")
            }
            prevByte = b
        }
        if (last) {
            val (result, _, _) = decoder.decodeToUtf16Raw(ByteArray(0), dst, true)
            when (result) {
                is DecoderResult.InputEmpty -> {}
                is DecoderResult.Malformed -> return null
                is DecoderResult.OutputFull -> error("unreachable")
            }
        }
        return score
    }
}

// Shift-JIS and Big5
internal fun problematicLead(b: Int): Boolean =
    when (b) {
        in 0x91..0x97, 0x9A, 0x8A, 0x9B, 0x8B, 0x9E, 0x8E, 0xB0 -> true
        else -> false
    }

// GBK and EUC-KR
internal fun moreProblematicLead(b: Int): Boolean =
    problematicLead(b) || b == 0x82 || b == 0x84 || b == 0x85 || b == 0xA0

internal class ShiftJisCandidate(
    private var decoder: Decoder = SHIFT_JIS.newDecoderWithoutBomHandling(),
    private var halfWidthKatakanaSeen: Boolean = false,
    private var halfWidthKatakanaState: HalfWidthKatakana = HalfWidthKatakana.DakutenForbidden,
    private var prev: LatinCj = LatinCj.Other,
    private var prevByte: Int = 0,
    private var pendingScore: Long? = null,
) {
    private fun maybeSetAsPending(s: Long): Long {
        check(pendingScore == null)
        return if (prev == LatinCj.Cj || !problematicLead(prevByte)) {
            s
        } else {
            pendingScore = s
            0L
        }
    }

    fun feed(buffer: ByteArray, last: Boolean): Long? {
        var score = 0L
        val src = ByteArray(1)
        val dst = CharArray(2)
        for (rawB in buffer) {
            val b = rawB.toInt() and 0xFF
            src[0] = rawB
            val (result, _, written) = decoder.decodeToUtf16Raw(src, dst, false)
            if (written > 0) {
                val currentHwState = halfWidthKatakanaState
                halfWidthKatakanaState = HalfWidthKatakana.DakutenForbidden
                val u = dst[0].code
                if ((u in 'a'.code..'z'.code) || (u in 'A'.code..'Z'.code)) {
                    pendingScore = null
                    if (prev == LatinCj.Cj) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.AsciiLetter
                } else if (u in 0xFF61..0xFF9F) {
                    if (!halfWidthKatakanaSeen) {
                        halfWidthKatakanaSeen = true
                        score += SHIFT_JIS_INITIAL_HALF_WIDTH_KATAKANA_PENALTY
                    }
                    pendingScore = null
                    score += HALF_WIDTH_KATAKANA_SCORE

                    if ((u in 0xFF76..0xFF84) || u == 0xFF73) {
                        halfWidthKatakanaState = HalfWidthKatakana.DakutenAllowed
                    } else if (u in 0xFF8A..0xFF8E) {
                        halfWidthKatakanaState = HalfWidthKatakana.DakutenOrHandakutenAllowed
                    } else if (u == 0xFF9E) {
                        if (currentHwState == HalfWidthKatakana.DakutenForbidden) {
                            score += IMPLAUSIBILITY_PENALTY
                        } else {
                            score += HALF_WIDTH_KATAKANA_VOICING_SCORE
                        }
                    } else if (u == 0xFF9F) {
                        if (currentHwState != HalfWidthKatakana.DakutenOrHandakutenAllowed) {
                            score += IMPLAUSIBILITY_PENALTY
                        } else {
                            score += HALF_WIDTH_KATAKANA_VOICING_SCORE
                        }
                    }

                    if (prev == LatinCj.AsciiLetter) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.Cj
                } else if (u in 0x3040 until 0x3100) {
                    val pending = pendingScore
                    if (pending != null) {
                        score += pending
                        pendingScore = null
                    }
                    score += SHIFT_JIS_SCORE_PER_KANA
                    if (prev == LatinCj.AsciiLetter) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.Cj
                } else if ((u in 0x3400 until 0xA000) || (u in 0xF900 until 0xFB00)) {
                    val pending = pendingScore
                    if (pending != null) {
                        score += pending
                        pendingScore = null
                    }
                    if (prevByte < 0x98 || (prevByte == 0x98 && b < 0x73)) {
                        score +=
                            maybeSetAsPending(
                                SHIFT_JIS_SCORE_PER_LEVEL_1_KANJI +
                                    cjkExtraScore(u, DETECTOR_DATA.frequentKanji),
                            )
                    } else {
                        score += maybeSetAsPending(SHIFT_JIS_SCORE_PER_LEVEL_2_KANJI)
                    }
                    if (prev == LatinCj.AsciiLetter) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.Cj
                } else if (u in 0xE000 until 0xF900) {
                    val pending = pendingScore
                    if (pending != null) {
                        score += pending
                        pendingScore = null
                    }
                    score += SHIFT_JIS_PUA_PENALTY
                    prev = LatinCj.Other
                } else {
                    when (u) {
                        0x3000,
                        0x3001,
                        0x3002,
                        0xFF08,
                        0xFF09,
                        -> {
                            val pending = pendingScore
                            if (pending != null) {
                                score += pending
                                pendingScore = null
                            }
                            score += CJ_PUNCTUATION
                        }
                        in 0..0x7F -> {
                            pendingScore = null
                        }
                        0x80 -> {
                            pendingScore = null
                            score += IMPLAUSIBILITY_PENALTY
                        }
                        else -> {
                            val pending = pendingScore
                            if (pending != null) {
                                score += pending
                                pendingScore = null
                            }
                            score += CJK_OTHER
                        }
                    }
                    prev = LatinCj.Other
                }
            }
            when (result) {
                is DecoderResult.InputEmpty -> {}
                is DecoderResult.Malformed -> {
                    val malformedLen = result.length.toInt()
                    if ((
                            ((prevByte in 0x81..0x9F) || (prevByte in 0xE0..0xFC)) &&
                                ((b in 0x40..0x7E) || (b in 0x80..0xFC))
                        ) &&
                        !(
                            (prevByte == 0x82 && b >= 0xFA) ||
                                (prevByte == 0x84 && ((b in 0xDD..0xE4) || b >= 0xFB)) ||
                                (prevByte == 0x86 && b in 0xF2..0xFA) ||
                                (prevByte == 0x87 && b in 0x77..0x7D) ||
                                (prevByte == 0xFC && b >= 0xF5)
                        )
                    ) {
                        val pending = pendingScore
                        if (pending != null) {
                            score += pending
                            pendingScore = null
                        }
                        score += SHIFT_JIS_EXTENSION_PENALTY
                        if (prevByte < 0x87) {
                            prev = LatinCj.Other
                        } else {
                            if (prev == LatinCj.AsciiLetter) {
                                score += CJK_LATIN_ADJACENCY_PENALTY
                            }
                            prev = LatinCj.Cj
                        }
                    } else if (malformedLen == 1 && (b == 0xA0 || b >= 0xFD)) {
                        pendingScore = null
                        score += SHIFT_JIS_SINGLE_BYTE_EXTENSION_PENALTY
                        prev = LatinCj.Other
                    } else {
                        return null
                    }
                }
                is DecoderResult.OutputFull -> error("unreachable")
            }
            prevByte = b
        }
        if (last) {
            val (result, _, _) = decoder.decodeToUtf16Raw(ByteArray(0), dst, true)
            when (result) {
                is DecoderResult.InputEmpty -> {}
                is DecoderResult.Malformed -> return null
                is DecoderResult.OutputFull -> error("unreachable")
            }
        }
        return score
    }
}

internal class EucJpCandidate(
    private var decoder: Decoder = EUC_JP.newDecoderWithoutBomHandling(),
    private var nonAsciiSeen: Boolean = false,
    private var halfWidthKatakanaState: HalfWidthKatakana = HalfWidthKatakana.DakutenForbidden,
    private var prev: LatinCj = LatinCj.Other,
    private var prevByte: Int = 0,
    private var prevPrevByte: Int = 0,
) {
    fun feed(buffer: ByteArray, last: Boolean): Long? {
        var score = 0L
        val src = ByteArray(1)
        val dst = CharArray(2)
        for (rawB in buffer) {
            val b = rawB.toInt() and 0xFF
            src[0] = rawB
            val (result, _, written) = decoder.decodeToUtf16Raw(src, dst, false)
            if (written > 0) {
                val currentHwState = halfWidthKatakanaState
                halfWidthKatakanaState = HalfWidthKatakana.DakutenForbidden
                val u = dst[0].code
                if (!nonAsciiSeen && u >= 0x80) {
                    nonAsciiSeen = true
                    if (u in 0x3040 until 0x3100) {
                        score += EUC_JP_INITIAL_KANA_PENALTY
                    }
                }
                if ((u in 'a'.code..'z'.code) || (u in 'A'.code..'Z'.code)) {
                    if (prev == LatinCj.Cj) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.AsciiLetter
                } else if (u in 0xFF61..0xFF9F) {
                    score += HALF_WIDTH_KATAKANA_SCORE

                    if ((u in 0xFF76..0xFF84) || u == 0xFF73) {
                        halfWidthKatakanaState = HalfWidthKatakana.DakutenAllowed
                    } else if (u in 0xFF8A..0xFF8E) {
                        halfWidthKatakanaState = HalfWidthKatakana.DakutenOrHandakutenAllowed
                    } else if (u == 0xFF9E) {
                        if (currentHwState == HalfWidthKatakana.DakutenForbidden) {
                            score += IMPLAUSIBILITY_PENALTY
                        } else {
                            score += HALF_WIDTH_KATAKANA_VOICING_SCORE
                        }
                    } else if (u == 0xFF9F) {
                        if (currentHwState != HalfWidthKatakana.DakutenOrHandakutenAllowed) {
                            score += IMPLAUSIBILITY_PENALTY
                        } else {
                            score += HALF_WIDTH_KATAKANA_VOICING_SCORE
                        }
                    }

                    if (prev == LatinCj.AsciiLetter) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.Other
                } else if ((u in 0x3041..0x3093) || (u in 0x30A1..0x30F6)) {
                    when (u) {
                        0x3090, 0x3091, 0x30F0, 0x30F1 -> {
                            score += EUC_JP_SCORE_PER_NEAR_OBSOLETE_KANA
                        }
                        else -> {
                            score += EUC_JP_SCORE_PER_KANA
                        }
                    }
                    if (prev == LatinCj.AsciiLetter) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.Cj
                } else if ((u in 0x3400 until 0xA000) || (u in 0xF900 until 0xFB00)) {
                    if (prevPrevByte == 0x8F) {
                        score += EUC_JP_SCORE_PER_OTHER_KANJI
                    } else if (prevByte < 0xD0) {
                        score += EUC_JP_SCORE_PER_LEVEL_1_KANJI
                        score += cjkExtraScore(u, DETECTOR_DATA.frequentKanji)
                    } else {
                        score += EUC_JP_SCORE_PER_LEVEL_2_KANJI
                    }
                    if (prev == LatinCj.AsciiLetter) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.Cj
                } else {
                    when (u) {
                        0x3000, 0x3001, 0x3002, 0xFF08, 0xFF09 -> {
                            score += CJ_PUNCTUATION
                        }
                        in 0..0x7F -> {}
                        else -> {
                            score += CJK_OTHER
                        }
                    }
                    prev = LatinCj.Other
                }
            }
            when (result) {
                is DecoderResult.InputEmpty -> {}
                is DecoderResult.Malformed -> {
                    if (b in 0xA1..0xFE &&
                        prevByte in 0xA1..0xFE &&
                        (
                            (
                                prevPrevByte != 0x8F &&
                                    !(prevByte == 0xA8 && b in 0xDF..0xE6) &&
                                    !(prevByte == 0xAC && b in 0xF4..0xFC) &&
                                    !(prevByte == 0xAD && b in 0xD8..0xDE)
                            ) ||
                                (
                                    prevPrevByte == 0x8F &&
                                        prevByte != 0xA2 &&
                                        prevByte != 0xA6 &&
                                        prevByte != 0xA7 &&
                                        prevByte != 0xA9 &&
                                        prevByte != 0xAA &&
                                        prevByte != 0xAB &&
                                        prevByte != 0xED &&
                                        !(prevByte == 0xFE && b >= 0xF7)
                                )
                        )
                    ) {
                        score += EUC_JP_EXTENSION_PENALTY
                        if (prev == LatinCj.AsciiLetter) {
                            score += CJK_LATIN_ADJACENCY_PENALTY
                        }
                        prev = LatinCj.Cj
                    } else {
                        return null
                    }
                }
                is DecoderResult.OutputFull -> error("unreachable")
            }
            prevPrevByte = prevByte
            prevByte = b
        }
        if (last) {
            val (result, _, _) = decoder.decodeToUtf16Raw(ByteArray(0), dst, true)
            when (result) {
                is DecoderResult.InputEmpty -> {}
                is DecoderResult.Malformed -> return null
                is DecoderResult.OutputFull -> error("unreachable")
            }
        }
        return score
    }
}

internal class Big5Candidate(
    private var decoder: Decoder = BIG5.newDecoderWithoutBomHandling(),
    private var prev: LatinCj = LatinCj.Other,
    private var prevByte: Int = 0,
    private var pendingScore: Long? = null,
) {
    private fun maybeSetAsPending(s: Long): Long {
        check(pendingScore == null)
        return if (prev == LatinCj.Cj || !problematicLead(prevByte)) {
            s
        } else {
            pendingScore = s
            0L
        }
    }

    fun feed(buffer: ByteArray, last: Boolean): Long? {
        var score = 0L
        val src = ByteArray(1)
        val dst = CharArray(2)
        for (rawB in buffer) {
            val b = rawB.toInt() and 0xFF
            src[0] = rawB
            val (result, _, written) = decoder.decodeToUtf16Raw(src, dst, false)
            if (written == 1) {
                val u = dst[0].code
                if ((u in 'a'.code..'z'.code) || (u in 'A'.code..'Z'.code)) {
                    pendingScore = null
                    if (prev == LatinCj.Cj) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.AsciiLetter
                } else if ((u in 0x3400 until 0xA000) || (u in 0xF900 until 0xFB00)) {
                    val pending = pendingScore
                    if (pending != null) {
                        score += pending
                        pendingScore = null
                    }
                    when (prevByte) {
                        in 0xA4..0xC6 -> {
                            score += maybeSetAsPending(BIG5_SCORE_PER_LEVEL_1_HANZI)
                        }
                        else -> {
                            score += maybeSetAsPending(BIG5_SCORE_PER_OTHER_HANZI)
                        }
                    }
                    if (prev == LatinCj.AsciiLetter) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.Cj
                } else {
                    when (u) {
                        0x3000, 0x3001, 0x3002, 0xFF08, 0xFF09, 0xFF01, 0xFF0C, 0xFF1B, 0xFF1F -> {
                            val pending = pendingScore
                            if (pending != null) {
                                score += pending
                                pendingScore = null
                            }
                            score += CJ_PUNCTUATION
                        }
                        in 0..0x7F -> {
                            pendingScore = null
                        }
                        else -> {
                            val pending = pendingScore
                            if (pending != null) {
                                score += pending
                                pendingScore = null
                            }
                            score += CJK_OTHER
                        }
                    }
                    prev = LatinCj.Other
                }
            } else if (written == 2) {
                val pending = pendingScore
                if (pending != null) {
                    score += pending
                    pendingScore = null
                }
                val u = dst[0].code
                if (u == 0xCA || u == 0xEA) {
                    score += CJK_OTHER
                    prev = LatinCj.Other
                } else {
                    score += maybeSetAsPending(BIG5_SCORE_PER_OTHER_HANZI)
                    if (prev == LatinCj.AsciiLetter) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinCj.Cj
                }
            }
            when (result) {
                is DecoderResult.InputEmpty -> {}
                is DecoderResult.Malformed -> {
                    val malformedLen = result.length.toInt()
                    if (prevByte in 0x81..0xFE &&
                        ((b in 0x40..0x7E) || (b in 0xA1..0xFE))
                    ) {
                        val pending = pendingScore
                        if (pending != null) {
                            score += pending
                            pendingScore = null
                        }
                        score += BIG5_PUA_PENALTY
                        if (prev == LatinCj.AsciiLetter) {
                            score += CJK_LATIN_ADJACENCY_PENALTY
                        }
                        prev = LatinCj.Cj
                    } else if ((prevByte == 0xA0 || prevByte == 0xFD || prevByte == 0xFE) &&
                        (b < 0x80 || b == 0xFF)
                    ) {
                        pendingScore = null
                        score += BIG5_SINGLE_BYTE_EXTENSION_PENALTY
                        if ((b in 'a'.code..'z'.code) || (b in 'A'.code..'Z'.code)) {
                            prev = LatinCj.AsciiLetter
                        } else if (b == 0xFF) {
                            score += BIG5_SINGLE_BYTE_EXTENSION_PENALTY
                            prev = LatinCj.Other
                        } else {
                            prev = LatinCj.Other
                        }
                    } else if (malformedLen == 1 && b == 0xFF) {
                        pendingScore = null
                        score += BIG5_SINGLE_BYTE_EXTENSION_PENALTY
                        prev = LatinCj.Other
                    } else {
                        return null
                    }
                }
                is DecoderResult.OutputFull -> error("unreachable")
            }
            prevByte = b
        }
        if (last) {
            val (result, _, _) = decoder.decodeToUtf16Raw(ByteArray(0), dst, true)
            when (result) {
                is DecoderResult.InputEmpty -> {}
                is DecoderResult.Malformed -> return null
                is DecoderResult.OutputFull -> error("unreachable")
            }
        }
        return score
    }
}

internal class EucKrCandidate(
    private var decoder: Decoder = EUC_KR.newDecoderWithoutBomHandling(),
    private var prevByte: Int = 0,
    private var prevWasEucRange: Boolean = false,
    private var prev: LatinKorean = LatinKorean.Other,
    private var currentWordLen: Long = 0L,
    private var pendingScore: Long? = null,
) {
    private fun maybeSetAsPending(s: Long): Long {
        check(pendingScore == null)
        return if (prev == LatinKorean.Hangul || !moreProblematicLead(prevByte)) {
            s
        } else {
            pendingScore = s
            0L
        }
    }

    fun feed(buffer: ByteArray, last: Boolean): Long? {
        var score = 0L
        val src = ByteArray(1)
        val dst = CharArray(2)
        for (rawB in buffer) {
            val b = rawB.toInt() and 0xFF
            val inEucRange = b in 0xA1..0xFE
            src[0] = rawB
            val (result, _, written) = decoder.decodeToUtf16Raw(src, dst, false)
            if (written > 0) {
                val u = dst[0].code
                if ((u in 'a'.code..'z'.code) || (u in 'A'.code..'Z'.code)) {
                    pendingScore = null
                    when (prev) {
                        LatinKorean.Hangul, LatinKorean.Hanja -> {
                            score += CJK_LATIN_ADJACENCY_PENALTY
                        }
                        else -> {}
                    }
                    prev = LatinKorean.AsciiLetter
                    currentWordLen = 0L
                } else if (u in 0xAC00..0xD7A3) {
                    val pending = pendingScore
                    if (pending != null) {
                        score += pending
                        pendingScore = null
                    }
                    if (prevWasEucRange && inEucRange) {
                        score += EUC_KR_SCORE_PER_EUC_HANGUL
                        score += cjkExtraScore(u, DETECTOR_DATA.frequentHangul)
                    } else {
                        score += maybeSetAsPending(EUC_KR_SCORE_PER_NON_EUC_HANGUL)
                    }
                    if (prev == LatinKorean.AsciiLetter) {
                        score += CJK_LATIN_ADJACENCY_PENALTY
                    }
                    prev = LatinKorean.Hangul
                    currentWordLen += 1L
                    if (currentWordLen > 5L) {
                        score += EUC_KR_LONG_WORD_PENALTY
                    }
                } else if ((u in 0x4E00 until 0xAC00) || (u in 0xF900..0xFA0B)) {
                    val pending = pendingScore
                    if (pending != null) {
                        score += pending
                        pendingScore = null
                    }
                    score += EUC_KR_SCORE_PER_HANJA
                    when (prev) {
                        LatinKorean.AsciiLetter -> {
                            score += CJK_LATIN_ADJACENCY_PENALTY
                        }
                        LatinKorean.Hangul -> {
                            score += EUC_KR_HANJA_AFTER_HANGUL_PENALTY
                        }
                        else -> {}
                    }
                    prev = LatinKorean.Hanja
                    currentWordLen += 1L
                    if (currentWordLen > 5L) {
                        score += EUC_KR_LONG_WORD_PENALTY
                    }
                } else {
                    if (u >= 0x80) {
                        val pending = pendingScore
                        if (pending != null) {
                            score += pending
                            pendingScore = null
                        }
                        score += CJK_OTHER
                    } else {
                        pendingScore = null
                    }
                    prev = LatinKorean.Other
                    currentWordLen = 0L
                }
            }
            when (result) {
                is DecoderResult.InputEmpty -> {}
                is DecoderResult.Malformed -> {
                    val malformedLen = result.length.toInt()
                    if ((prevByte == 0xC9 || prevByte == 0xFE) && b in 0xA1..0xFE) {
                        val pending = pendingScore
                        if (pending != null) {
                            score += pending
                            pendingScore = null
                        }
                        score += EUC_KR_PUA_PENALTY
                        when (prev) {
                            LatinKorean.AsciiLetter -> {
                                score += CJK_LATIN_ADJACENCY_PENALTY
                            }
                            LatinKorean.Hangul -> {
                                score += EUC_KR_HANJA_AFTER_HANGUL_PENALTY
                            }
                            else -> {}
                        }
                        prev = LatinKorean.Hanja
                        currentWordLen += 1L
                        if (currentWordLen > 5L) {
                            score += EUC_KR_LONG_WORD_PENALTY
                        }
                    } else if ((prevByte == 0xA1 || (prevByte in 0xA3..0xA8) || (prevByte in 0xAA..0xAD)) &&
                        (b in 0x7B..0x7D)
                    ) {
                        val pending = pendingScore
                        if (pending != null) {
                            score += pending
                            pendingScore = null
                        }
                        score += EUC_KR_MAC_KOREAN_PENALTY
                        prev = LatinKorean.Other
                        currentWordLen = 0L
                    } else if ((prevByte in 0x81..0x84) && (b <= 0x80 || b == 0xFF)) {
                        pendingScore = null
                        score += EUC_KR_SINGLE_BYTE_EXTENSION_PENALTY
                        if ((b in 'a'.code..'z'.code) || (b in 'A'.code..'Z'.code)) {
                            prev = LatinKorean.AsciiLetter
                        } else if (b == 0x80 || b == 0xFF) {
                            score += EUC_KR_SINGLE_BYTE_EXTENSION_PENALTY
                            prev = LatinKorean.Other
                        } else {
                            prev = LatinKorean.Other
                        }
                        currentWordLen = 0L
                    } else if (malformedLen == 1 && (b == 0x80 || b == 0xFF)) {
                        pendingScore = null
                        score += EUC_KR_SINGLE_BYTE_EXTENSION_PENALTY
                        prev = LatinKorean.Other
                        currentWordLen = 0L
                    } else {
                        return null
                    }
                }
                is DecoderResult.OutputFull -> error("unreachable")
            }
            prevWasEucRange = inEucRange
            prevByte = b
        }
        if (last) {
            val (result, _, _) = decoder.decodeToUtf16Raw(ByteArray(0), dst, true)
            when (result) {
                is DecoderResult.InputEmpty -> {}
                is DecoderResult.Malformed -> return null
                is DecoderResult.OutputFull -> error("unreachable")
            }
        }
        return score
    }
}

internal sealed class InnerCandidate {
    data class Latin(
        val candidate: LatinCandidate,
    ) : InnerCandidate()

    data class NonLatinCased(
        val candidate: NonLatinCasedCandidate,
    ) : InnerCandidate()

    data class Caseless(
        val candidate: CaselessCandidate,
    ) : InnerCandidate()

    data class ArabicFrench(
        val candidate: ArabicFrenchCandidate,
    ) : InnerCandidate()

    data class Logical(
        val candidate: LogicalCandidate,
    ) : InnerCandidate()

    data class Visual(
        val candidate: VisualCandidate,
    ) : InnerCandidate()

    data class Utf8(
        val candidate: Utf8Candidate,
    ) : InnerCandidate()

    data class Iso2022(
        val candidate: Iso2022Candidate,
    ) : InnerCandidate()

    data class Shift(
        val candidate: ShiftJisCandidate,
    ) : InnerCandidate()

    data class EucJp(
        val candidate: EucJpCandidate,
    ) : InnerCandidate()

    data class EucKr(
        val candidate: EucKrCandidate,
    ) : InnerCandidate()

    data class Big5(
        val candidate: Big5Candidate,
    ) : InnerCandidate()

    data class Gbk(
        val candidate: GbkCandidate,
    ) : InnerCandidate()

    fun feed(buffer: ByteArray, last: Boolean): Long? {
        val spaceBuffer = byteArrayOf(' '.code.toByte())
        return when (this) {
            is Latin -> {
                val newScore = candidate.feed(buffer)
                if (newScore != null) {
                    if (last) {
                        val additionalScore = candidate.feed(spaceBuffer)
                        if (additionalScore != null) newScore + additionalScore else null
                    } else {
                        newScore
                    }
                } else {
                    null
                }
            }
            is NonLatinCased -> {
                val newScore = candidate.feed(buffer)
                if (newScore != null) {
                    if (last) {
                        val additionalScore = candidate.feed(spaceBuffer)
                        if (additionalScore != null) newScore + additionalScore else null
                    } else {
                        newScore
                    }
                } else {
                    null
                }
            }
            is Caseless -> {
                val newScore = candidate.feed(buffer)
                if (newScore != null) {
                    if (last) {
                        val additionalScore = candidate.feed(spaceBuffer)
                        if (additionalScore != null) newScore + additionalScore else null
                    } else {
                        newScore
                    }
                } else {
                    null
                }
            }
            is ArabicFrench -> {
                val newScore = candidate.feed(buffer)
                if (newScore != null) {
                    if (last) {
                        val additionalScore = candidate.feed(spaceBuffer)
                        if (additionalScore != null) newScore + additionalScore else null
                    } else {
                        newScore
                    }
                } else {
                    null
                }
            }
            is Logical -> {
                val newScore = candidate.feed(buffer)
                if (newScore != null) {
                    if (last) {
                        val additionalScore = candidate.feed(spaceBuffer)
                        if (additionalScore != null) newScore + additionalScore else null
                    } else {
                        newScore
                    }
                } else {
                    null
                }
            }
            is Visual -> {
                val newScore = candidate.feed(buffer)
                if (newScore != null) {
                    if (last) {
                        val additionalScore = candidate.feed(spaceBuffer)
                        if (additionalScore != null) newScore + additionalScore else null
                    } else {
                        newScore
                    }
                } else {
                    null
                }
            }
            is Utf8 -> candidate.feed(buffer, last)
            is Iso2022 -> candidate.feed(buffer, last)
            is Shift -> candidate.feed(buffer, last)
            is EucJp -> candidate.feed(buffer, last)
            is EucKr -> candidate.feed(buffer, last)
            is Big5 -> candidate.feed(buffer, last)
            is Gbk -> candidate.feed(buffer, last)
        }
    }
}

internal fun encodingForTld(tld: Tld): Int =
    when (tld) {
        Tld.CentralWindows, Tld.CentralCyrillic -> EncodingDetector.CENTRAL_WINDOWS_INDEX
        Tld.Cyrillic -> EncodingDetector.CYRILLIC_WINDOWS_INDEX
        Tld.Generic, Tld.Western, Tld.WesternCyrillic, Tld.WesternArabic, Tld.Eu ->
            EncodingDetector.WESTERN_INDEX
        Tld.IcelandicFaroese -> EncodingDetector.ICELANDIC_INDEX
        Tld.Greek -> EncodingDetector.GREEK_ISO_INDEX
        Tld.TurkishAzeri -> EncodingDetector.TURKISH_INDEX
        Tld.Hebrew -> EncodingDetector.LOGICAL_INDEX
        Tld.Arabic -> EncodingDetector.ARABIC_WINDOWS_INDEX
        Tld.Baltic -> EncodingDetector.BALTIC_WINDOWS_INDEX
        Tld.Vietnamese -> EncodingDetector.VIETNAMESE_INDEX
        Tld.Thai -> EncodingDetector.THAI_INDEX
        Tld.Simplified, Tld.SimplifiedTraditional -> EncodingDetector.GBK_INDEX
        Tld.Traditional, Tld.TraditionalSimplified -> EncodingDetector.BIG5_INDEX
        Tld.Japanese -> EncodingDetector.SHIFT_JIS_INDEX
        Tld.Korean -> EncodingDetector.EUC_KR_INDEX
        Tld.CentralIso -> EncodingDetector.CENTRAL_ISO_INDEX
    }

internal fun encodingIsNativeToTld(tld: Tld, encoding: Int): Boolean =
    when (tld) {
        Tld.CentralWindows -> encoding == EncodingDetector.CENTRAL_WINDOWS_INDEX
        Tld.Cyrillic -> {
            encoding == EncodingDetector.CYRILLIC_WINDOWS_INDEX ||
                encoding == EncodingDetector.CYRILLIC_KOI_INDEX ||
                encoding == EncodingDetector.CYRILLIC_IBM_INDEX ||
                encoding == EncodingDetector.CYRILLIC_ISO_INDEX
        }
        Tld.Western -> encoding == EncodingDetector.WESTERN_INDEX
        Tld.Greek -> {
            encoding == EncodingDetector.GREEK_WINDOWS_INDEX ||
                encoding == EncodingDetector.GREEK_ISO_INDEX
        }
        Tld.TurkishAzeri -> encoding == EncodingDetector.TURKISH_INDEX
        Tld.Hebrew -> encoding == EncodingDetector.LOGICAL_INDEX
        Tld.Arabic -> {
            encoding == EncodingDetector.ARABIC_WINDOWS_INDEX ||
                encoding == EncodingDetector.ARABIC_ISO_INDEX
        }
        Tld.Baltic -> {
            encoding == EncodingDetector.BALTIC_WINDOWS_INDEX ||
                encoding == EncodingDetector.BALTIC_ISO13_INDEX ||
                encoding == EncodingDetector.BALTIC_ISO4_INDEX
        }
        Tld.Vietnamese -> encoding == EncodingDetector.VIETNAMESE_INDEX
        Tld.Thai -> encoding == EncodingDetector.THAI_INDEX
        Tld.Simplified -> encoding == EncodingDetector.GBK_INDEX
        Tld.Traditional -> encoding == EncodingDetector.BIG5_INDEX
        Tld.Japanese -> {
            encoding == EncodingDetector.SHIFT_JIS_INDEX ||
                encoding == EncodingDetector.EUC_JP_INDEX
        }
        Tld.Korean -> encoding == EncodingDetector.EUC_KR_INDEX
        Tld.SimplifiedTraditional, Tld.TraditionalSimplified -> {
            encoding == EncodingDetector.GBK_INDEX || encoding == EncodingDetector.BIG5_INDEX
        }
        Tld.CentralIso -> encoding == EncodingDetector.CENTRAL_ISO_INDEX
        Tld.IcelandicFaroese -> encoding == EncodingDetector.ICELANDIC_INDEX
        Tld.WesternCyrillic -> {
            encoding == EncodingDetector.WESTERN_INDEX ||
                encoding == EncodingDetector.CYRILLIC_WINDOWS_INDEX ||
                encoding == EncodingDetector.CYRILLIC_KOI_INDEX ||
                encoding == EncodingDetector.CYRILLIC_IBM_INDEX ||
                encoding == EncodingDetector.CYRILLIC_ISO_INDEX
        }
        Tld.CentralCyrillic -> {
            encoding == EncodingDetector.CENTRAL_WINDOWS_INDEX ||
                encoding == EncodingDetector.CENTRAL_ISO_INDEX ||
                encoding == EncodingDetector.CYRILLIC_WINDOWS_INDEX ||
                encoding == EncodingDetector.CYRILLIC_KOI_INDEX ||
                encoding == EncodingDetector.CYRILLIC_IBM_INDEX ||
                encoding == EncodingDetector.CYRILLIC_ISO_INDEX
        }
        Tld.WesternArabic -> {
            encoding == EncodingDetector.WESTERN_INDEX ||
                encoding == EncodingDetector.ARABIC_WINDOWS_INDEX ||
                encoding == EncodingDetector.ARABIC_ISO_INDEX
        }
        Tld.Eu -> {
            encoding == EncodingDetector.WESTERN_INDEX ||
                encoding == EncodingDetector.ICELANDIC_INDEX ||
                encoding == EncodingDetector.CENTRAL_WINDOWS_INDEX ||
                encoding == EncodingDetector.CENTRAL_ISO_INDEX ||
                encoding == EncodingDetector.CYRILLIC_WINDOWS_INDEX ||
                encoding == EncodingDetector.CYRILLIC_KOI_INDEX ||
                encoding == EncodingDetector.CYRILLIC_IBM_INDEX ||
                encoding == EncodingDetector.CYRILLIC_ISO_INDEX ||
                encoding == EncodingDetector.GREEK_WINDOWS_INDEX ||
                encoding == EncodingDetector.GREEK_ISO_INDEX ||
                encoding == EncodingDetector.BALTIC_WINDOWS_INDEX ||
                encoding == EncodingDetector.BALTIC_ISO13_INDEX ||
                encoding == EncodingDetector.BALTIC_ISO4_INDEX
        }
        Tld.Generic -> false
    }

internal fun scoreAdjustment(score: Long, encoding: Int, tld: Tld): Long {
    if (score < 1L) {
        return 0L
    }
    val (divisor, constant) =
        when (tld) {
            Tld.Generic -> error("unreachable")
            Tld.CentralWindows, Tld.CentralIso -> {
                when (encoding) {
                    EncodingDetector.WESTERN_INDEX,
                    EncodingDetector.ICELANDIC_INDEX,
                    EncodingDetector.BALTIC_WINDOWS_INDEX,
                    EncodingDetector.BALTIC_ISO4_INDEX,
                    EncodingDetector.BALTIC_ISO13_INDEX,
                    EncodingDetector.VIETNAMESE_INDEX,
                    EncodingDetector.TURKISH_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
            Tld.Cyrillic -> {
                when (encoding) {
                    EncodingDetector.BIG5_INDEX,
                    EncodingDetector.GBK_INDEX,
                    EncodingDetector.EUC_JP_INDEX,
                    EncodingDetector.CENTRAL_WINDOWS_INDEX,
                    EncodingDetector.CENTRAL_ISO_INDEX,
                    EncodingDetector.GREEK_WINDOWS_INDEX,
                    EncodingDetector.GREEK_ISO_INDEX,
                    EncodingDetector.VISUAL_INDEX,
                    EncodingDetector.LOGICAL_INDEX,
                    EncodingDetector.BALTIC_WINDOWS_INDEX,
                    EncodingDetector.BALTIC_ISO4_INDEX,
                    EncodingDetector.BALTIC_ISO13_INDEX,
                    EncodingDetector.TURKISH_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
            Tld.Western, Tld.WesternCyrillic, Tld.WesternArabic -> {
                when (encoding) {
                    EncodingDetector.CENTRAL_WINDOWS_INDEX,
                    EncodingDetector.CENTRAL_ISO_INDEX,
                    EncodingDetector.BALTIC_WINDOWS_INDEX,
                    EncodingDetector.BALTIC_ISO4_INDEX,
                    EncodingDetector.BALTIC_ISO13_INDEX,
                    EncodingDetector.TURKISH_INDEX,
                    EncodingDetector.VIETNAMESE_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
            Tld.Greek -> {
                when (encoding) {
                    EncodingDetector.BIG5_INDEX,
                    EncodingDetector.GBK_INDEX,
                    EncodingDetector.EUC_JP_INDEX,
                    EncodingDetector.CENTRAL_WINDOWS_INDEX,
                    EncodingDetector.CENTRAL_ISO_INDEX,
                    EncodingDetector.CYRILLIC_WINDOWS_INDEX,
                    EncodingDetector.CYRILLIC_ISO_INDEX,
                    EncodingDetector.CYRILLIC_KOI_INDEX,
                    EncodingDetector.CYRILLIC_IBM_INDEX,
                    EncodingDetector.VISUAL_INDEX,
                    EncodingDetector.LOGICAL_INDEX,
                    EncodingDetector.BALTIC_WINDOWS_INDEX,
                    EncodingDetector.BALTIC_ISO4_INDEX,
                    EncodingDetector.BALTIC_ISO13_INDEX,
                    EncodingDetector.TURKISH_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
            Tld.TurkishAzeri -> {
                when (encoding) {
                    EncodingDetector.CENTRAL_WINDOWS_INDEX,
                    EncodingDetector.CENTRAL_ISO_INDEX,
                    EncodingDetector.BALTIC_WINDOWS_INDEX,
                    EncodingDetector.BALTIC_ISO4_INDEX,
                    EncodingDetector.BALTIC_ISO13_INDEX,
                    EncodingDetector.VIETNAMESE_INDEX,
                    EncodingDetector.ICELANDIC_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
            Tld.Hebrew -> {
                when (encoding) {
                    EncodingDetector.CENTRAL_WINDOWS_INDEX,
                    EncodingDetector.CENTRAL_ISO_INDEX,
                    EncodingDetector.CYRILLIC_WINDOWS_INDEX,
                    EncodingDetector.CYRILLIC_ISO_INDEX,
                    EncodingDetector.CYRILLIC_KOI_INDEX,
                    EncodingDetector.CYRILLIC_IBM_INDEX,
                    EncodingDetector.GREEK_WINDOWS_INDEX,
                    EncodingDetector.GREEK_ISO_INDEX,
                    EncodingDetector.BALTIC_WINDOWS_INDEX,
                    EncodingDetector.BALTIC_ISO4_INDEX,
                    EncodingDetector.BALTIC_ISO13_INDEX,
                    EncodingDetector.VIETNAMESE_INDEX,
                    EncodingDetector.TURKISH_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
            Tld.Arabic -> {
                when (encoding) {
                    EncodingDetector.BIG5_INDEX,
                    EncodingDetector.GBK_INDEX,
                    EncodingDetector.EUC_JP_INDEX,
                    EncodingDetector.EUC_KR_INDEX,
                    EncodingDetector.CENTRAL_WINDOWS_INDEX,
                    EncodingDetector.CENTRAL_ISO_INDEX,
                    EncodingDetector.CYRILLIC_WINDOWS_INDEX,
                    EncodingDetector.CYRILLIC_ISO_INDEX,
                    EncodingDetector.CYRILLIC_KOI_INDEX,
                    EncodingDetector.CYRILLIC_IBM_INDEX,
                    EncodingDetector.GREEK_WINDOWS_INDEX,
                    EncodingDetector.GREEK_ISO_INDEX,
                    EncodingDetector.VISUAL_INDEX,
                    EncodingDetector.LOGICAL_INDEX,
                    EncodingDetector.BALTIC_WINDOWS_INDEX,
                    EncodingDetector.BALTIC_ISO4_INDEX,
                    EncodingDetector.BALTIC_ISO13_INDEX,
                    EncodingDetector.VIETNAMESE_INDEX,
                    EncodingDetector.TURKISH_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
            Tld.Baltic -> {
                when (encoding) {
                    EncodingDetector.CENTRAL_WINDOWS_INDEX,
                    EncodingDetector.CENTRAL_ISO_INDEX,
                    EncodingDetector.ICELANDIC_INDEX,
                    EncodingDetector.TURKISH_INDEX,
                    EncodingDetector.VIETNAMESE_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
            Tld.Vietnamese -> {
                when (encoding) {
                    EncodingDetector.CENTRAL_WINDOWS_INDEX,
                    EncodingDetector.CENTRAL_ISO_INDEX,
                    EncodingDetector.BALTIC_WINDOWS_INDEX,
                    EncodingDetector.BALTIC_ISO4_INDEX,
                    EncodingDetector.BALTIC_ISO13_INDEX,
                    EncodingDetector.TURKISH_INDEX,
                    EncodingDetector.ICELANDIC_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
            Tld.Thai -> {
                when (encoding) {
                    EncodingDetector.BIG5_INDEX,
                    EncodingDetector.GBK_INDEX,
                    EncodingDetector.EUC_JP_INDEX,
                    EncodingDetector.EUC_KR_INDEX,
                    EncodingDetector.SHIFT_JIS_INDEX,
                    EncodingDetector.CENTRAL_WINDOWS_INDEX,
                    EncodingDetector.CENTRAL_ISO_INDEX,
                    EncodingDetector.CYRILLIC_WINDOWS_INDEX,
                    EncodingDetector.CYRILLIC_ISO_INDEX,
                    EncodingDetector.CYRILLIC_KOI_INDEX,
                    EncodingDetector.CYRILLIC_IBM_INDEX,
                    EncodingDetector.GREEK_WINDOWS_INDEX,
                    EncodingDetector.GREEK_ISO_INDEX,
                    EncodingDetector.ARABIC_WINDOWS_INDEX,
                    EncodingDetector.ARABIC_ISO_INDEX,
                    EncodingDetector.VISUAL_INDEX,
                    EncodingDetector.LOGICAL_INDEX,
                    EncodingDetector.BALTIC_WINDOWS_INDEX,
                    EncodingDetector.BALTIC_ISO4_INDEX,
                    EncodingDetector.BALTIC_ISO13_INDEX,
                    EncodingDetector.TURKISH_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
            Tld.Simplified,
            Tld.Traditional,
            Tld.TraditionalSimplified,
            Tld.SimplifiedTraditional,
            Tld.Japanese,
            Tld.Korean,
            -> return score
            Tld.IcelandicFaroese -> {
                when (encoding) {
                    EncodingDetector.CENTRAL_WINDOWS_INDEX,
                    EncodingDetector.CENTRAL_ISO_INDEX,
                    EncodingDetector.BALTIC_WINDOWS_INDEX,
                    EncodingDetector.BALTIC_ISO4_INDEX,
                    EncodingDetector.BALTIC_ISO13_INDEX,
                    EncodingDetector.TURKISH_INDEX,
                    EncodingDetector.VIETNAMESE_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
            Tld.CentralCyrillic -> {
                when (encoding) {
                    EncodingDetector.BIG5_INDEX,
                    EncodingDetector.GBK_INDEX,
                    EncodingDetector.EUC_JP_INDEX,
                    EncodingDetector.GREEK_WINDOWS_INDEX,
                    EncodingDetector.GREEK_ISO_INDEX,
                    EncodingDetector.VISUAL_INDEX,
                    EncodingDetector.LOGICAL_INDEX,
                    EncodingDetector.BALTIC_WINDOWS_INDEX,
                    EncodingDetector.BALTIC_ISO4_INDEX,
                    EncodingDetector.BALTIC_ISO13_INDEX,
                    EncodingDetector.TURKISH_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
            Tld.Eu -> {
                when (encoding) {
                    EncodingDetector.BIG5_INDEX,
                    EncodingDetector.GBK_INDEX,
                    EncodingDetector.EUC_JP_INDEX,
                    EncodingDetector.TURKISH_INDEX,
                    EncodingDetector.VIETNAMESE_INDEX,
                    -> return score
                    else -> Pair(50L, 60L)
                }
            }
        }
    return (score / divisor) + constant
}

internal class Candidate(
    val inner: InnerCandidate,
    var score: Long?,
) {
    fun feed(buffer: ByteArray, last: Boolean) {
        val oldScore = score
        if (oldScore != null) {
            val newScore = inner.feed(buffer, last)
            if (newScore != null) {
                score = oldScore + newScore
            } else {
                score = null
            }
        }
    }

    fun qualified(): Boolean = score != null

    fun score(encoding: Int, tld: Tld, expectationIsValid: Boolean): Long? {
        when (val inC = inner) {
            is InnerCandidate.NonLatinCased -> {
                if (inC.candidate.longestWord < 2L) {
                    return null
                }
            }
            is InnerCandidate.Caseless -> {
                if (inC.candidate.longestWord < 2L && !encodingIsNativeToTld(tld, encoding)) {
                    return null
                }
            }
            is InnerCandidate.ArabicFrench -> {
                if (inC.candidate.longestWord < 2L && !encodingIsNativeToTld(tld, encoding)) {
                    return null
                }
            }
            is InnerCandidate.Logical -> {
                if (inC.candidate.longestWord < 2L && !encodingIsNativeToTld(tld, encoding)) {
                    return null
                }
            }
            is InnerCandidate.Visual -> {
                if (inC.candidate.longestWord < 2L && !encodingIsNativeToTld(tld, encoding)) {
                    return null
                }
            }
            else -> {}
        }
        if (tld == Tld.Generic) {
            return score
        }
        val currentScore = score
        if (currentScore != null) {
            if (encoding == encodingForTld(tld)) {
                return currentScore + 1L
            }
            if (encodingIsNativeToTld(tld, encoding)) {
                return currentScore
            }
            if (expectationIsValid) {
                return currentScore - scoreAdjustment(currentScore, encoding, tld)
            }
            return currentScore
        }
        return null
    }

    fun plausiblePunctuation(): Long =
        when (val inC = inner) {
            is InnerCandidate.Logical -> inC.candidate.plausiblePunctuation
            is InnerCandidate.Visual -> inC.candidate.plausiblePunctuation
            else -> error("unreachable")
        }

    fun encoding(): Encoding =
        when (val inC = inner) {
            is InnerCandidate.Latin -> inC.candidate.data.encoding
            is InnerCandidate.NonLatinCased -> inC.candidate.data.encoding
            is InnerCandidate.Caseless -> inC.candidate.data.encoding
            is InnerCandidate.ArabicFrench -> inC.candidate.data.encoding
            is InnerCandidate.Logical -> inC.candidate.data.encoding
            is InnerCandidate.Visual -> inC.candidate.data.encoding
            is InnerCandidate.Shift -> SHIFT_JIS
            is InnerCandidate.EucJp -> EUC_JP
            is InnerCandidate.Big5 -> BIG5
            is InnerCandidate.EucKr -> EUC_KR
            is InnerCandidate.Gbk -> GBK
            is InnerCandidate.Utf8 -> UTF_8
            is InnerCandidate.Iso2022 -> ISO_2022_JP
        }

    companion object {
        fun newLatin(data: SingleByteData): Candidate =
            Candidate(InnerCandidate.Latin(LatinCandidate(data)), 0L)

        fun newNonLatinCased(data: SingleByteData): Candidate =
            Candidate(InnerCandidate.NonLatinCased(NonLatinCasedCandidate(data)), 0L)

        fun newCaseless(data: SingleByteData): Candidate =
            Candidate(InnerCandidate.Caseless(CaselessCandidate(data)), 0L)

        fun newArabicFrench(data: SingleByteData): Candidate =
            Candidate(InnerCandidate.ArabicFrench(ArabicFrenchCandidate(data)), 0L)

        fun newLogical(data: SingleByteData): Candidate =
            Candidate(InnerCandidate.Logical(LogicalCandidate(data)), 0L)

        fun newVisual(data: SingleByteData): Candidate =
            Candidate(InnerCandidate.Visual(VisualCandidate(data)), 0L)

        fun newUtf8(): Candidate =
            Candidate(InnerCandidate.Utf8(Utf8Candidate()), 0L)

        fun newIso2022Jp(): Candidate =
            Candidate(InnerCandidate.Iso2022(Iso2022Candidate()), 0L)

        fun newShiftJis(): Candidate =
            Candidate(InnerCandidate.Shift(ShiftJisCandidate()), 0L)

        fun newEucJp(): Candidate =
            Candidate(InnerCandidate.EucJp(EucJpCandidate()), 0L)

        fun newEucKr(): Candidate =
            Candidate(InnerCandidate.EucKr(EucKrCandidate()), 0L)

        fun newBig5(): Candidate =
            Candidate(InnerCandidate.Big5(Big5Candidate()), 0L)

        fun newGbk(): Candidate =
            Candidate(InnerCandidate.Gbk(GbkCandidate()), 0L)
    }
}

private fun countNonAscii(buffer: ByteArray): Long {
    var count = 0L
    for (b in buffer) {
        if ((b.toInt() and 0xFF) >= 0x80) {
            count += 1L
        }
    }
    return count
}

internal sealed class BeforeNonAscii {
    data object None : BeforeNonAscii()

    data class One(
        val arr: ByteArray,
    ) : BeforeNonAscii() {
        override fun equals(other: Any?): Boolean =
            other is One && arr.contentEquals(other.arr)

        override fun hashCode(): Int = arr.contentHashCode()
    }

    data class Two(
        val arr: ByteArray,
    ) : BeforeNonAscii() {
        override fun equals(other: Any?): Boolean =
            other is Two && arr.contentEquals(other.arr)

        override fun hashCode(): Int = arr.contentHashCode()
    }

    fun asSlice(): ByteArray =
        when (this) {
            is None -> byteArrayOf()
            is One -> arr
            is Two -> arr
        }

    fun push(buffer: ByteArray): BeforeNonAscii {
        val len = buffer.size
        return if (len >= 2) {
            Two(byteArrayOf(buffer[len - 2], buffer[len - 1]))
        } else if (len == 1) {
            when (this) {
                is None -> One(byteArrayOf(buffer[0]))
                is One -> Two(byteArrayOf(arr[0], buffer[0]))
                is Two -> Two(byteArrayOf(arr[1], buffer[0]))
            }
        } else {
            this
        }
    }
}

/**
 * A Web browser-oriented detector for guessing what character
 * encoding a stream of bytes is encoded in.
 *
 * The bytes are fed to the detector incrementally using the `feed`
 * method. The current guess of the detector can be queried using
 * the `guess` method. The guessing parameters are arguments to the
 * `guess` method rather than arguments to the constructor in order
 * to enable the application to check if the arguments affect the
 * guessing outcome. (The specific use case is to disable UI for
 * re-running the detector with UTF-8 allowed and the top-level
 * domain name ignored if those arguments don't change the guess.)
 */
public class EncodingDetector internal constructor(
    private val candidates: Array<Candidate>,
    private var nonAsciiSeen: Long,
    private var lastBeforeNonAscii: BeforeNonAscii,
    private var escSeen: Boolean,
    private var closed: Boolean,
) {
    public constructor() : this(
        arrayOf(
            Candidate.newUtf8(), // 0
            Candidate.newIso2022Jp(), // 1
            Candidate.newVisual(SINGLE_BYTE_DATA[ISO_8859_8_INDEX]), // 2
            Candidate.newGbk(), // 3
            Candidate.newEucJp(), // 4
            Candidate.newEucKr(), // 5
            Candidate.newShiftJis(), // 6
            Candidate.newBig5(), // 7
            Candidate.newLatin(SINGLE_BYTE_DATA[WINDOWS_1252_INDEX]), // 8
            Candidate.newNonLatinCased(SINGLE_BYTE_DATA[WINDOWS_1251_INDEX]), // 9
            Candidate.newLatin(SINGLE_BYTE_DATA[WINDOWS_1250_INDEX]), // 10
            Candidate.newLatin(SINGLE_BYTE_DATA[ISO_8859_2_INDEX]), // 11
            Candidate.newArabicFrench(SINGLE_BYTE_DATA[WINDOWS_1256_INDEX]), // 12
            Candidate.newLatin(SINGLE_BYTE_DATA[WINDOWS_1252_ICELANDIC_INDEX]), // 13
            Candidate.newLatin(SINGLE_BYTE_DATA[WINDOWS_1254_INDEX]), // 14
            Candidate.newCaseless(SINGLE_BYTE_DATA[WINDOWS_874_INDEX]), // 15
            Candidate.newLogical(SINGLE_BYTE_DATA[WINDOWS_1255_INDEX]), // 16
            Candidate.newNonLatinCased(SINGLE_BYTE_DATA[WINDOWS_1253_INDEX]), // 17
            Candidate.newNonLatinCased(SINGLE_BYTE_DATA[ISO_8859_7_INDEX]), // 18
            Candidate.newLatin(SINGLE_BYTE_DATA[WINDOWS_1257_INDEX]), // 19
            Candidate.newLatin(SINGLE_BYTE_DATA[ISO_8859_13_INDEX]), // 20
            Candidate.newNonLatinCased(SINGLE_BYTE_DATA[KOI8_U_INDEX]), // 21
            Candidate.newNonLatinCased(SINGLE_BYTE_DATA[IBM866_INDEX]), // 22
            Candidate.newCaseless(SINGLE_BYTE_DATA[ISO_8859_6_INDEX]), // 23
            Candidate.newLatin(SINGLE_BYTE_DATA[WINDOWS_1258_INDEX]), // 24
            Candidate.newLatin(SINGLE_BYTE_DATA[ISO_8859_4_INDEX]), // 25
            Candidate.newNonLatinCased(SINGLE_BYTE_DATA[ISO_8859_5_INDEX]), // 26
        ),
        0L,
        BeforeNonAscii.None,
        false,
        false,
    )

    private fun feedImpl(buffer: ByteArray, last: Boolean) {
        for (candidate in candidates) {
            candidate.feed(buffer, last)
        }
        nonAsciiSeen += countNonAscii(buffer)
    }

    /**
     * Inform the detector of a chunk of input.
     *
     * The byte stream is represented as a sequence of calls to this
     * method such that the concatenation of the arguments to this
     * method form the byte stream. It does not matter how the application
     * chooses to chunk the stream. It is OK to call this method with
     * a zero-length byte slice.
     *
     * The end of the stream is indicated by calling this method with
     * `last` set to `true`. In that case, the end of the stream is
     * considered to occur after the last byte of the `buffer` (which
     * may be zero-length) passed in the same call. Once this method
     * has been called with `last` set to `true` this method must not
     * be called again.
     *
     * If you want to perform detection on just the prefix of a longer
     * stream, do not pass `last=true` after the prefix if the stream
     * actually still continues.
     *
     * Returns `true` if after processing `buffer` the stream has
     * contained at least one non-ASCII byte and `false` if only
     * ASCII has been seen so far.
     *
     * @throws IllegalArgumentException If this method has previously been called with `last` set to `true`.
     */
    public fun feed(buffer: ByteArray, last: Boolean): Boolean {
        require(!closed) { "Must not feed again after feeding with last equaling true." }
        if (last) {
            closed = true
        }
        val start =
            if (nonAsciiSeen == 0L && !escSeen) {
                val upTo = Ascii.asciiValidUpTo(buffer)
                var escape: Int? = null
                for (i in 0 until upTo) {
                    if (buffer[i] == 0x1B.toByte()) {
                        escape = i
                        break
                    }
                }
                val s =
                    if (escape != null) {
                        escSeen = true
                        escape
                    } else {
                        upTo
                    }
                if (s == buffer.size) {
                    lastBeforeNonAscii = lastBeforeNonAscii.push(buffer)
                    return nonAsciiSeen != 0L
                }
                if (s == 0 || s == 1) {
                    val lastBefore = lastBeforeNonAscii
                    lastBeforeNonAscii = BeforeNonAscii.None
                    feedImpl(lastBefore.asSlice(), false)
                    0
                } else {
                    s - 2
                }
            } else {
                0
            }
        val slice = if (start == 0) buffer else buffer.copyOfRange(start, buffer.size)
        feedImpl(slice, last)
        return nonAsciiSeen != 0L
    }

    /**
     * Guess the encoding given the bytes pushed to the detector so far
     * (via `feed()`), the top-level domain name from which the bytes were
     * loaded, and an indication of whether to consider UTF-8 as a permissible
     * guess.
     *
     * The `tld` argument takes the rightmost DNS label of the hostname of the
     * host the stream was loaded from in lower-case ASCII form. That is, if
     * the label is an internationalized top-level domain name, it must be
     * provided in its Punycode form. If the TLD that the stream was loaded
     * from is unavailable, `null` may be passed instead, which is equivalent
     * to passing `Some(b"com")`.
     *
     * If the `allowUtf8` argument is set to `false`, the return value of
     * this method won't be `UTF_8`. When performing detection
     * on `text/html` on non-`file:` URLs, Web browsers must pass `false`,
     * unless the user has taken a specific contextual action to request an
     * override. This way, Web developers cannot start depending on UTF-8
     * detection. Such reliance would make the Web Platform more brittle.
     *
     * Returns the guessed encoding.
     *
     * @throws IllegalArgumentException If `tld` contains non-ASCII, period, or upper-case letters.
     */
    public fun guess(tld: ByteArray?, allowUtf8: Boolean): Encoding =
        guessAssess(tld, allowUtf8).first

    /**
     * Same as `guess()`, but also returns a Boolean indicating
     * whether the guessed encoding had a higher score than at least
     * one other candidate. If this method returns `false`, the
     * guessed encoding is likely to be wrong.
     */
    public fun guessAssess(tld: ByteArray?, allowUtf8: Boolean): Pair<Encoding, Boolean> {
        var tldType =
            if (tld != null) {
                require(!containsUpperCasePeriodOrNonAscii(tld))
                classifyTld(tld)
            } else {
                Tld.Generic
            }

        if (nonAsciiSeen == 0L && escSeen && candidates[ISO_2022_JP_INDEX].score != null) {
            return Pair(ISO_2022_JP, true)
        }

        if (candidates[UTF_8_INDEX].score != null) {
            if (allowUtf8) {
                return Pair(UTF_8, true)
            }
            return Pair(candidates[encodingForTld(tldType)].encoding(), true)
        }

        var encoding = candidates[encodingForTld(tldType)].encoding()
        var max = 0L
        var expectationIsValid = false
        if (tldType != Tld.Generic) {
            for (i in FIRST_NORMAL until candidates.size) {
                val candidate = candidates[i]
                if (encodingIsNativeToTld(tldType, i) && candidate.score != null) {
                    expectationIsValid = true
                    break
                }
            }
        }
        if (!expectationIsValid) {
            when (tldType) {
                Tld.Simplified -> {
                    if (candidates[BIG5_INDEX].score != null) {
                        tldType = Tld.Traditional
                        expectationIsValid = true
                    }
                }
                Tld.Traditional -> {
                    if (candidates[GBK_INDEX].score != null) {
                        tldType = Tld.Simplified
                        expectationIsValid = true
                    }
                }
                Tld.CentralWindows -> {
                    if (candidates[CENTRAL_ISO_INDEX].score != null) {
                        tldType = Tld.CentralIso
                        expectationIsValid = true
                    }
                }
                Tld.CentralIso -> {
                    if (candidates[CENTRAL_WINDOWS_INDEX].score != null) {
                        tldType = Tld.CentralWindows
                        expectationIsValid = true
                    }
                }
                else -> {}
            }
        }
        for (i in FIRST_NORMAL until candidates.size) {
            val candidate = candidates[i]
            val score = candidate.score(i, tldType, expectationIsValid)
            if (score != null) {
                if (score > max) {
                    max = score
                    encoding = candidate.encoding()
                }
            }
        }
        val visual = candidates[VISUAL_INDEX]
        val visualScore = visual.score(VISUAL_INDEX, tldType, expectationIsValid)
        if (visualScore != null) {
            if ((visualScore > max || encoding === WINDOWS_1255) &&
                visual.plausiblePunctuation() > candidates[LOGICAL_INDEX].plausiblePunctuation()
            ) {
                encoding = ISO_8859_8
            }
        }
        return Pair(encoding, max >= 0)
    }

    public fun findScore(encoding: Encoding): Long? {
        var tldType = Tld.Generic
        var expectationIsValid = false
        if (tldType != Tld.Generic) {
            for (i in FIRST_NORMAL until candidates.size) {
                val candidate = candidates[i]
                if (encodingIsNativeToTld(tldType, i) && candidate.score != null) {
                    expectationIsValid = true
                    break
                }
            }
        }
        if (!expectationIsValid) {
            when (tldType) {
                Tld.Simplified -> {
                    if (candidates[BIG5_INDEX].score != null) {
                        tldType = Tld.Traditional
                        expectationIsValid = true
                    }
                }
                Tld.Traditional -> {
                    if (candidates[GBK_INDEX].score != null) {
                        tldType = Tld.Simplified
                        expectationIsValid = true
                    }
                }
                Tld.CentralWindows -> {
                    if (candidates[CENTRAL_ISO_INDEX].score != null) {
                        tldType = Tld.CentralIso
                        expectationIsValid = true
                    }
                }
                Tld.CentralIso -> {
                    if (candidates[CENTRAL_WINDOWS_INDEX].score != null) {
                        tldType = Tld.CentralWindows
                        expectationIsValid = true
                    }
                }
                else -> {}
            }
        }
        for (i in candidates.indices) {
            val candidate = candidates[i]
            if (encoding === candidate.encoding()) {
                return candidate.score(i, tldType, expectationIsValid)
            }
        }
        return 0L
    }

    public companion object {
        internal const val FIRST_NORMAL: Int = 3
        internal const val UTF_8_INDEX: Int = 0
        internal const val ISO_2022_JP_INDEX: Int = 1
        internal const val VISUAL_INDEX: Int = 2
        internal const val GBK_INDEX: Int = 3
        internal const val EUC_JP_INDEX: Int = 4
        internal const val EUC_KR_INDEX: Int = 5
        internal const val SHIFT_JIS_INDEX: Int = 6
        internal const val BIG5_INDEX: Int = 7
        internal const val WESTERN_INDEX: Int = 8
        internal const val CYRILLIC_WINDOWS_INDEX: Int = 9
        internal const val CENTRAL_WINDOWS_INDEX: Int = 10
        internal const val CENTRAL_ISO_INDEX: Int = 11
        internal const val ARABIC_WINDOWS_INDEX: Int = 12
        internal const val ICELANDIC_INDEX: Int = 13
        internal const val TURKISH_INDEX: Int = 14
        internal const val THAI_INDEX: Int = 15
        internal const val LOGICAL_INDEX: Int = 16
        internal const val GREEK_WINDOWS_INDEX: Int = 17
        internal const val GREEK_ISO_INDEX: Int = 18
        internal const val BALTIC_WINDOWS_INDEX: Int = 19
        internal const val BALTIC_ISO13_INDEX: Int = 20
        internal const val CYRILLIC_KOI_INDEX: Int = 21
        internal const val CYRILLIC_IBM_INDEX: Int = 22
        internal const val ARABIC_ISO_INDEX: Int = 23
        internal const val VIETNAMESE_INDEX: Int = 24
        internal const val BALTIC_ISO4_INDEX: Int = 25
        internal const val CYRILLIC_ISO_INDEX: Int = 26

        public fun new(): EncodingDetector = EncodingDetector()
    }
}
