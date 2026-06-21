// port-lint: source lib.rs
package io.github.kotlinmania.chardetng

// Copyright Mozilla Foundation. See the COPYRIGHT
// file at the top-level directory of this distribution.
//
// Licensed under the Apache License, Version 2.0 <LICENSE-APACHE or
// https://www.apache.org/licenses/LICENSE-2.0> or the MIT license
// <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. This file may not be copied, modified, or distributed
// except according to those terms.

/**
 * `chardetng` is a character encoding detector for legacy Web content.
 *
 * It is optimized for binary size in applications that already depend
 * on the encoding-rs character set library for other reasons.
 */

// Upstream lib.rs is parceled across multiple Kotlin files. This file is the
// module tracking ledger for that split: it carries the upstream license header
// and module-level documentation, and records which upstream items currently
// live in which Kotlin file.
//
// Parceled from lib.rs:
//   - score and penalty constants (lines 52..155) -> LibConstants.kt
//   - containsUpperCasePeriodOrNonAscii            -> LibLabel.kt
//
// Remaining upstream items are not yet ported. They depend on the
// encoding-rs-kotlin sibling port (Decoder, DecoderResult, Encoding, and the
// per-charset singletons Big5, EucJp, EucKr, Gbk, Iso2022Jp, Iso88598,
// ShiftJis, Utf8, Windows1255), on Data.kt translated from data.rs
// (DetectorData, DETECTOR_DATA, SingleByteData, SINGLE_BYTE_DATA, the various
// charset index constants, ASCII_DIGIT, computeIndex), and on the
// multithreading-only items gated behind the upstream multithreading Cargo
// feature (rayon parallel iterators and the ArrayVec stack vector).

internal const val CHARDETNG_LIB = "chardetng.Lib"
