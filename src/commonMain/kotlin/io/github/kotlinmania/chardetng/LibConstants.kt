// port-lint: source lib.rs
package io.github.kotlinmania.chardetng

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
