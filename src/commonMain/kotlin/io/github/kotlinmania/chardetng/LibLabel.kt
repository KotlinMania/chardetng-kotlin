// port-lint: source lib.rs
package io.github.kotlinmania.chardetng

internal fun containsUpperCasePeriodOrNonAscii(label: ByteArray): Boolean {
    for (b in label) {
        val u = b.toInt() and 0xFF
        if (u >= 0x80) {
            return true
        }
        if (u == '.'.code) {
            return true
        }
        if (u >= 'A'.code && u <= 'Z'.code) {
            return true
        }
    }
    return false
}
