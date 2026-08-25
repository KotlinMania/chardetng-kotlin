// port-lint: tests lib.rs
package io.github.kotlinmania.chardetng

import io.github.kotlinmania.encodingrs.Encoding
import io.github.kotlinmania.encodingrs.Encoding.Companion.BIG5
import io.github.kotlinmania.encodingrs.Encoding.Companion.EUC_JP
import io.github.kotlinmania.encodingrs.Encoding.Companion.EUC_KR
import io.github.kotlinmania.encodingrs.Encoding.Companion.GBK
import io.github.kotlinmania.encodingrs.Encoding.Companion.IBM866
import io.github.kotlinmania.encodingrs.Encoding.Companion.ISO_2022_JP
import io.github.kotlinmania.encodingrs.Encoding.Companion.ISO_8859_2
import io.github.kotlinmania.encodingrs.Encoding.Companion.ISO_8859_4
import io.github.kotlinmania.encodingrs.Encoding.Companion.ISO_8859_5
import io.github.kotlinmania.encodingrs.Encoding.Companion.ISO_8859_6
import io.github.kotlinmania.encodingrs.Encoding.Companion.ISO_8859_7
import io.github.kotlinmania.encodingrs.Encoding.Companion.ISO_8859_8
import io.github.kotlinmania.encodingrs.Encoding.Companion.KOI8_U
import io.github.kotlinmania.encodingrs.Encoding.Companion.SHIFT_JIS
import io.github.kotlinmania.encodingrs.Encoding.Companion.WINDOWS_1250
import io.github.kotlinmania.encodingrs.Encoding.Companion.WINDOWS_1251
import io.github.kotlinmania.encodingrs.Encoding.Companion.WINDOWS_1252
import io.github.kotlinmania.encodingrs.Encoding.Companion.WINDOWS_1253
import io.github.kotlinmania.encodingrs.Encoding.Companion.WINDOWS_1254
import io.github.kotlinmania.encodingrs.Encoding.Companion.WINDOWS_1255
import io.github.kotlinmania.encodingrs.Encoding.Companion.WINDOWS_1256
import io.github.kotlinmania.encodingrs.Encoding.Companion.WINDOWS_1257
import io.github.kotlinmania.encodingrs.Encoding.Companion.WINDOWS_1258
import io.github.kotlinmania.encodingrs.Encoding.Companion.WINDOWS_874
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LibTest {
    private fun checkBytes(bytes: ByteArray, encoding: Encoding) {
        val det = EncodingDetector()
        det.feed(bytes, true)
        val enc = det.guess(null, false)
        assertEquals(encoding, enc)
    }

    private fun decomposeVietnameseTones(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            when (ch) {
                'à' -> sb.append("a\u0300")
                'á' -> sb.append("a\u0301")
                'ả' -> sb.append("a\u0309")
                'ã' -> sb.append("a\u0303")
                'ạ' -> sb.append("a\u0323")
                'ầ' -> sb.append("â\u0300")
                'ấ' -> sb.append("â\u0301")
                'ẩ' -> sb.append("â\u0309")
                'ẫ' -> sb.append("â\u0303")
                'ậ' -> sb.append("â\u0323")
                'ằ' -> sb.append("ă\u0300")
                'ắ' -> sb.append("ă\u0301")
                'ẳ' -> sb.append("ă\u0309")
                'ẵ' -> sb.append("ă\u0303")
                'ặ' -> sb.append("ă\u0323")
                'è' -> sb.append("e\u0300")
                'é' -> sb.append("e\u0301")
                'ẻ' -> sb.append("e\u0309")
                'ẽ' -> sb.append("e\u0303")
                'ẹ' -> sb.append("e\u0323")
                'ề' -> sb.append("ê\u0300")
                'ế' -> sb.append("ê\u0301")
                'ể' -> sb.append("ê\u0309")
                'ễ' -> sb.append("ê\u0303")
                'ệ' -> sb.append("ê\u0323")
                'ì' -> sb.append("i\u0300")
                'í' -> sb.append("i\u0301")
                'ỉ' -> sb.append("i\u0309")
                'ĩ' -> sb.append("i\u0303")
                'ị' -> sb.append("i\u0323")
                'ò' -> sb.append("o\u0300")
                'ó' -> sb.append("o\u0301")
                'ỏ' -> sb.append("o\u0309")
                'õ' -> sb.append("o\u0303")
                'ọ' -> sb.append("o\u0323")
                'ồ' -> sb.append("ô\u0300")
                'ố' -> sb.append("ô\u0301")
                'ổ' -> sb.append("ô\u0309")
                'ỗ' -> sb.append("ô\u0303")
                'ộ' -> sb.append("ô\u0323")
                'ờ' -> sb.append("ơ\u0300")
                'ớ' -> sb.append("ơ\u0301")
                'ở' -> sb.append("ơ\u0309")
                'ỡ' -> sb.append("ơ\u0303")
                'ợ' -> sb.append("ơ\u0323")
                'ù' -> sb.append("u\u0300")
                'ú' -> sb.append("u\u0301")
                'ủ' -> sb.append("u\u0309")
                'ũ' -> sb.append("u\u0303")
                'ụ' -> sb.append("u\u0323")
                'ừ' -> sb.append("ư\u0300")
                'ứ' -> sb.append("ư\u0301")
                'ử' -> sb.append("ư\u0309")
                'ữ' -> sb.append("ư\u0303")
                'ự' -> sb.append("ư\u0323")
                'ỳ' -> sb.append("y\u0300")
                'ý' -> sb.append("y\u0301")
                'ỷ' -> sb.append("y\u0309")
                'ỹ' -> sb.append("y\u0303")
                'ỵ' -> sb.append("y\u0323")
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun check(input: String, encoding: Encoding) {
        val (bytes, _, _) =
            if (encoding === WINDOWS_1258) {
                val orthographic = decomposeVietnameseTones(input)
                encoding.encode(orthographic)
            } else {
                encoding.encode(input)
            }
        checkBytes(bytes, encoding)
    }

    @Test
    fun testIApostrophe() {
        val det = EncodingDetector()
        det.feed(byteArrayOf('I'.code.toByte(), 0x92.toByte()), true)
        val enc = det.guess(null, false)
        assertEquals(WINDOWS_1252, enc)
    }

    @Test
    fun testStreamingNumeroOneByOne() {
        val det = EncodingDetector()
        det.feed(byteArrayOf('n'.code.toByte()), false)
        det.feed(byteArrayOf('.'.code.toByte()), false)
        det.feed(byteArrayOf(0xBA.toByte()), false)
        det.feed(byteArrayOf('1'.code.toByte()), true)
        val enc = det.guess(null, false)
        assertEquals(WINDOWS_1252, enc)
    }

    @Test
    fun testStreamingNumeroTwoTogether() {
        val det = EncodingDetector()
        det.feed(byteArrayOf('n'.code.toByte(), '.'.code.toByte()), false)
        det.feed(byteArrayOf(0xBA.toByte()), false)
        det.feed(byteArrayOf('1'.code.toByte()), true)
        val enc = det.guess(null, false)
        assertEquals(WINDOWS_1252, enc)
    }

    @Test
    fun testStreamingNumeroOneByOneExtraBefore() {
        val det = EncodingDetector()
        det.feed(byteArrayOf(' '.code.toByte(), 'n'.code.toByte()), false)
        det.feed(byteArrayOf('.'.code.toByte()), false)
        det.feed(byteArrayOf(0xBA.toByte()), false)
        det.feed(byteArrayOf('1'.code.toByte()), true)
        val enc = det.guess(null, false)
        assertEquals(WINDOWS_1252, enc)
    }

    @Test
    fun testStreamingNumeroOneBefore() {
        val det = EncodingDetector()
        det.feed(byteArrayOf('n'.code.toByte()), false)
        det.feed(byteArrayOf('.'.code.toByte(), 0xBA.toByte()), false)
        det.feed(byteArrayOf('1'.code.toByte()), true)
        val enc = det.guess(null, false)
        assertEquals(WINDOWS_1252, enc)
    }

    @Test
    fun testStreamingNumeroLongerFirstBuffer() {
        val det = EncodingDetector()
        det.feed(byteArrayOf('r'.code.toByte(), 'r'.code.toByte(), 'n'.code.toByte(), '.'.code.toByte()), false)
        det.feed(byteArrayOf(0xBA.toByte()), false)
        det.feed(byteArrayOf('1'.code.toByte()), true)
        val enc = det.guess(null, false)
        assertEquals(WINDOWS_1252, enc)
    }

    @Test
    fun testEmpty() {
        val det = EncodingDetector()
        val seenNonAscii = det.feed(ByteArray(0), true)
        val enc = det.guess(null, false)
        assertEquals(WINDOWS_1252, enc)
        assertFalse(seenNonAscii)
    }

    @Test
    fun testFi() {
        check("Ääni", WINDOWS_1252)
    }

    @Test
    fun testFiBis() {
        check("Tämä", WINDOWS_1252)
    }

    @Test
    fun testPt() {
        check("Este é um teste de codificação de caracteres.", WINDOWS_1252)
    }

    @Test
    fun testIs() {
        check("Þetta er kóðunarpróf á staf. Fyrir sum tungumál sem nota latneska stafi þurfum við meira inntak til að taka ákvörðunina.", WINDOWS_1252)
    }

    @Test
    fun testRuShort() {
        check("Русский", WINDOWS_1251)
    }

    @Test
    fun testRu() {
        check("Это тест кодировки символов.", WINDOWS_1251)
    }

    @Test
    fun testRuIso() {
        check("Это тест кодировки символов.", ISO_8859_5)
    }

    @Test
    fun testRuIbm() {
        check("Это тест кодировки символов.", IBM866)
    }

    @Test
    fun testRuKoi() {
        check("Это тест кодировки символов.", KOI8_U)
    }

    @Test
    fun testUk() {
        check("Це тест на кодування символів.", WINDOWS_1251)
    }

    @Test
    fun testUkKoi() {
        check("Це тест на кодування символів.", KOI8_U)
    }

    @Test
    fun testElShort() {
        check("Ελληνικά", WINDOWS_1253)
    }

    @Test
    fun testEl() {
        check("Πρόκειται για δοκιμή κωδικοποίησης χαρακτήρων: Άρης", WINDOWS_1253)
    }

    @Test
    fun testElIso() {
        check("Πρόκειται για δοκιμή κωδικοποίησης χαρακτήρων: Άρης", ISO_8859_7)
    }

    @Test
    fun testDe() {
        check("Straße", WINDOWS_1252)
    }

    @Test
    fun testEnWindows1252() {
        // "Don't "
        checkBytes(byteArrayOf(68, 111, 110, 180.toByte(), 116, 32), WINDOWS_1252)
    }

    @Test
    fun testHe() {
        check("\u05E2\u05D1\u05E8\u05D9\u05EA", WINDOWS_1255)
    }

    @Test
    fun test2022() {
        check("日本語", ISO_2022_JP)
    }

    @Test
    fun testTh() {
        check("นี่คือการทดสอบการเข้ารหัสอักขระ", WINDOWS_874)
    }

    @Test
    fun testVi() {
        check("Đây là một thử nghiệm mã hóa ký tự.", WINDOWS_1258)
    }

    @Test
    fun testTr() {
        check("Bu bir karakter kodlama testidir. Latince karakterleri kullanan bazı dillerde karar vermek için daha fazla girdiye ihtiyacımız var.", WINDOWS_1254)
    }

    @Test
    fun testSimplified() {
        check("这是一个字符编码测试。", GBK)
    }

    @Test
    fun testTraditional() {
        check("這是一個字符編碼測試。", BIG5)
    }

    @Test
    fun testKo() {
        check("이것은 문자 인코딩 테스트입니다.", EUC_KR)
    }

    @Test
    fun testShift() {
        check("これは文字実験です。", SHIFT_JIS)
    }

    @Test
    fun testEuc() {
        check("これは文字実験です。", EUC_JP)
    }

    @Test
    fun testAr() {
        check("هذا هو اختبار ترميز الأحرف.", WINDOWS_1256)
    }

    @Test
    fun testArIso() {
        check("هذا هو اختبار ترميز الأحرف.", ISO_8859_6)
    }

    @Test
    fun testFa() {
        check("این یک تست رمزگذاری کاراکتر است.", WINDOWS_1256)
    }

    @Test
    fun testVisual() {
        check(".םיוות דודיק ןחבמ והז", ISO_8859_8)
    }

    @Test
    fun testYi() {
        check("דאָס איז אַ טעסט פֿאַר קאָדירונג פון כאַראַקטער.", WINDOWS_1255)
    }

    @Test
    fun testIt() {
        check("è", WINDOWS_1252)
    }

    @Test
    fun testEn() {
        check("isn’t", WINDOWS_1252)
    }

    @Test
    fun testEnBis() {
        check("Rock ’n Roll", WINDOWS_1252)
    }

    @Test
    fun testCa() {
        check("Codificació de caràcters", WINDOWS_1252)
    }

    @Test
    fun testEt() {
        check("või", WINDOWS_1252)
    }

    @Test
    fun testPlIso() {
        check("To jest test kodowania znaków. W przypadku niektórych języków, które używają znaków łacińskich, potrzebujemy więcej danych, aby podjąć decyzję.", ISO_8859_2)
    }

    @Test
    fun testPl() {
        check("To jest test kodowania znaków. W przypadku niektórych języków, które używają znaków łacińskich, potrzebujemy więcej danych, aby podjąć decyzję.", WINDOWS_1250)
    }

    @Test
    fun testLt() {
        check("Tai simbolių kodavimo testas. Kai kurioms kalboms, naudojančioms lotyniškus rašmenis, mums reikia daugiau informacijos, kad galėtume priimti sprendimą.", WINDOWS_1257)
    }

    // Upstream comment: Detected as ISO-8859-2 in upstream tests
    // @Test
    // fun testLtWindowsIso88594() {
    //     check("Tai simbolių kodavimo testas. Kai kurioms kalboms, naudojančioms lotyniškus rašmenis, mums reikia daugiau informacijos, kad galėtume priimti sprendimą.", ISO_8859_4)
    // }

    @Test
    fun testLv() {
        check("Šis ir rakstzīmju kodēšanas tests. Dažās valodās, kurās tiek izmantotas latīņu valodas burti, lēmuma pieņemšanai mums ir nepieciešams vairāk ieguldījuma.", WINDOWS_1257)
    }

    @Test
    fun testLvIso88594() {
        check("Šis ir rakstzīmju kodēšanas tests. Dažās valodās, kurās tiek izmantotas latīņu valodas burti, lēmuma pieņemšanai mums ir nepieciešams vairāk ieguldījuma.", ISO_8859_4)
    }

    @Test
    fun testA0() {
        // Test that this isn't IBM866
        check("\u00A0\u00A0 \u00A0", WINDOWS_1252)
    }

    @Test
    fun testA0a0() {
        // Test that this isn't GBK or EUC-KR.
        check("\u00A0\u00A0", WINDOWS_1252)
    }

    @Test
    fun testSpaceCopyrightSpace() {
        check(" © ", WINDOWS_1252)
    }

    @Test
    fun testSpaceMasculineSpace() {
        check(" º ", WINDOWS_1252)
    }

    @Test
    fun testSpaceFeminineSpace() {
        check(" ª ", WINDOWS_1252)
    }

    @Test
    fun testPeriodMasculineSpace() {
        check(".º ", WINDOWS_1252)
    }

    @Test
    fun testPeriodFeminineSpace() {
        check(".ª ", WINDOWS_1252)
    }

    @Test
    fun testMaria() {
        check(" Mª ", WINDOWS_1252)
    }

    @Test
    fun testDona() {
        check(" Dª ", WINDOWS_1252)
    }

    @Test
    fun testNuestra() {
        check(" Nª ", WINDOWS_1252)
    }

    @Test
    fun testSenora() {
        check(" Sª ", WINDOWS_1252)
    }

    @Test
    fun testDigitFeminine() {
        check(" 42ª ", WINDOWS_1252)
    }

    @Test
    fun testDigitMasculine() {
        check(" 42º ", WINDOWS_1252)
    }

    @Test
    fun testRomanFeminine() {
        check(" XIVª ", WINDOWS_1252)
    }

    @Test
    fun testRomanMasculine() {
        check(" XIVº ", WINDOWS_1252)
    }

    @Test
    fun testNumeroUno() {
        check("Nº1", WINDOWS_1252)
    }

    @Test
    fun testNumero() {
        check("Nº", WINDOWS_1252)
    }

    @Test
    fun testEuro() {
        check(" €9", WINDOWS_1252)
    }

    @Test
    fun testShiftJisHalfWidthKatakana() {
        check("ﾊｰﾄﾞｳｪｱﾊｰﾄﾞｳｪｱﾊｰﾄﾞｳｪｱﾊｰﾄﾞｳｪｱﾊｰﾄﾞｳｪｱ", SHIFT_JIS)
    }

    @Test
    fun testBig5Pua() {
        val v = mutableListOf<Byte>()
        for (i in 0 until 40) {
            v.add(0xA4.toByte())
            v.add(0x40.toByte())
        }
        v.add(0x81.toByte())
        v.add(0x40.toByte())
        v.add(0xA4.toByte())
        v.add(0x40.toByte())
        checkBytes(v.toByteArray(), BIG5)
    }

    @Test
    fun testBig5SingleByteA0() {
        val v = mutableListOf<Byte>()
        for (i in 0 until 80) {
            v.add(0xA4.toByte())
            v.add(0x40.toByte())
        }
        v.add(0x81.toByte())
        v.add(0x40.toByte())
        v.add(0xA0.toByte())
        v.add(' '.code.toByte())
        checkBytes(v.toByteArray(), BIG5)
    }

    @Test
    fun testBig5SingleByteFf() {
        val v = mutableListOf<Byte>()
        for (i in 0 until 80) {
            v.add(0xA4.toByte())
            v.add(0x40.toByte())
        }
        v.add(0x81.toByte())
        v.add(0x40.toByte())
        v.add(0xFF.toByte())
        v.add(' '.code.toByte())
        checkBytes(v.toByteArray(), BIG5)
    }

    @Test
    fun testNotBig5() {
        val v = mutableListOf<Byte>()
        for (i in 0 until 40) {
            v.add(0xA4.toByte())
            v.add(0x40.toByte())
        }
        v.add(0x81.toByte())
        v.add(0x40.toByte())
        v.add(0xA0.toByte())
        v.add(0xA0.toByte())
        checkBytes(v.toByteArray(), IBM866)
    }

    @Test
    fun testEucKrPua() {
        val v = mutableListOf<Byte>()
        v.add(0xC9.toByte())
        v.add(0xA1.toByte())
        v.add(0xB0.toByte())
        v.add(0xA1.toByte())
        v.add(' '.code.toByte())
        for (i in 0 until 40) {
            v.add(0xC5.toByte())
            v.add(0xD7.toByte())
            v.add(0xBD.toByte())
            v.add(0xBA.toByte())
            v.add(0xC6.toByte())
            v.add(0xAE.toByte())
            v.add('.'.code.toByte())
            v.add(' '.code.toByte())
        }
        checkBytes(v.toByteArray(), EUC_KR)
    }

    @Test
    fun testEucKrPuaBis() {
        val v = mutableListOf<Byte>()
        v.add(0xFE.toByte())
        v.add(0xA1.toByte())
        v.add(0xB0.toByte())
        v.add(0xA1.toByte())
        v.add(' '.code.toByte())
        for (i in 0 until 40) {
            v.add(0xC5.toByte())
            v.add(0xD7.toByte())
            v.add(0xBD.toByte())
            v.add(0xBA.toByte())
            v.add(0xC6.toByte())
            v.add(0xAE.toByte())
            v.add('.'.code.toByte())
            v.add(' '.code.toByte())
        }
        checkBytes(v.toByteArray(), EUC_KR)
    }

    @Test
    fun testEucKrSingleByteFf() {
        val v = mutableListOf<Byte>()
        v.add(0xFF.toByte())
        v.add(' '.code.toByte())
        for (i in 0 until 40) {
            v.add(0xC5.toByte())
            v.add(0xD7.toByte())
            v.add(0xBD.toByte())
            v.add(0xBA.toByte())
            v.add(0xC6.toByte())
            v.add(0xAE.toByte())
            v.add('.'.code.toByte())
            v.add(' '.code.toByte())
        }
        checkBytes(v.toByteArray(), EUC_KR)
    }

    @Test
    fun testEucKrSingleByte81() {
        val v = mutableListOf<Byte>()
        v.add(0x81.toByte())
        v.add(' '.code.toByte())
        for (i in 0 until 40) {
            v.add(0xC5.toByte())
            v.add(0xD7.toByte())
            v.add(0xBD.toByte())
            v.add(0xBA.toByte())
            v.add(0xC6.toByte())
            v.add(0xAE.toByte())
            v.add('.'.code.toByte())
            v.add(' '.code.toByte())
        }
        checkBytes(v.toByteArray(), EUC_KR)
    }

    @Test
    fun testEucKrSingleByte84() {
        val v = mutableListOf<Byte>()
        v.add(0x84.toByte())
        v.add(' '.code.toByte())
        for (i in 0 until 40) {
            v.add(0xC5.toByte())
            v.add(0xD7.toByte())
            v.add(0xBD.toByte())
            v.add(0xBA.toByte())
            v.add(0xC6.toByte())
            v.add(0xAE.toByte())
            v.add('.'.code.toByte())
            v.add(' '.code.toByte())
        }
        checkBytes(v.toByteArray(), EUC_KR)
    }

    @Test
    fun testNotEucKr() {
        val v = mutableListOf<Byte>()
        v.add(0xC9.toByte())
        v.add(0xA0.toByte())
        v.add(0xB0.toByte())
        v.add(0xA1.toByte())
        v.add(' '.code.toByte())
        for (i in 0 until 40) {
            v.add(0xC5.toByte())
            v.add(0xD7.toByte())
            v.add(0xBD.toByte())
            v.add(0xBA.toByte())
            v.add(0xC6.toByte())
            v.add(0xAE.toByte())
            v.add('.'.code.toByte())
            v.add(' '.code.toByte())
        }
        checkBytes(v.toByteArray(), GBK)
    }

    @Test
    fun testShiftJisX0213() {
        val v = mutableListOf<Byte>()
        v.add(0x87.toByte())
        v.add(0xE5.toByte())
        for (i in 0 until 40) {
            v.add(0x82.toByte())
            v.add(0xC9.toByte())
            v.add(0x82.toByte())
            v.add(0xD9.toByte())
            v.add(0x82.toByte())
            v.add(0xF1.toByte())
            v.add(0x82.toByte())
            v.add(0xB2.toByte())
        }
        checkBytes(v.toByteArray(), SHIFT_JIS)
    }

    @Test
    fun testShiftJisSingleByteFd() {
        val v = mutableListOf<Byte>()
        v.add(0xFD.toByte())
        for (i in 0 until 40) {
            v.add(0x82.toByte())
            v.add(0xC9.toByte())
            v.add(0x82.toByte())
            v.add(0xD9.toByte())
            v.add(0x82.toByte())
            v.add(0xF1.toByte())
            v.add(0x82.toByte())
            v.add(0xB2.toByte())
        }
        checkBytes(v.toByteArray(), SHIFT_JIS)
    }

    @Test
    fun testNotShiftJis() {
        val v = mutableListOf<Byte>()
        v.add(0x84.toByte())
        v.add(0xE0.toByte())
        for (i in 0 until 40) {
            v.add(0x82.toByte())
            v.add(0xC9.toByte())
            v.add(0x82.toByte())
            v.add(0xD9.toByte())
            v.add(0x82.toByte())
            v.add(0xF1.toByte())
            v.add(0x82.toByte())
            v.add(0xB2.toByte())
        }
        checkBytes(v.toByteArray(), GBK)
    }

    @Test
    fun testNotShiftJisBis() {
        val v = mutableListOf<Byte>()
        v.add(0x87.toByte())
        v.add(0x7D.toByte())
        for (i in 0 until 40) {
            v.add(0x82.toByte())
            v.add(0xC9.toByte())
            v.add(0x82.toByte())
            v.add(0xD9.toByte())
            v.add(0x82.toByte())
            v.add(0xF1.toByte())
            v.add(0x82.toByte())
            v.add(0xB2.toByte())
        }
        checkBytes(v.toByteArray(), GBK)
    }

    @Test
    fun testEucJpX0213() {
        val v = mutableListOf<Byte>()
        v.add(0xAD.toByte())
        v.add(0xBF.toByte())
        for (i in 0 until 80) {
            v.add(0xA4.toByte())
            v.add(0xCB.toByte())
            v.add(0xA4.toByte())
            v.add(0xDB.toByte())
            v.add(0xA4.toByte())
            v.add(0xF3.toByte())
            v.add(0xA4.toByte())
            v.add(0xB4.toByte())
        }
        checkBytes(v.toByteArray(), EUC_JP)
    }

    @Test
    fun testEucJpX0213OtherPlane() {
        val v = mutableListOf<Byte>()
        v.add(0x8F.toByte())
        v.add(0xFE.toByte())
        v.add(0xF6.toByte())
        for (i in 0 until 80) {
            v.add(0xA4.toByte())
            v.add(0xCB.toByte())
            v.add(0xA4.toByte())
            v.add(0xDB.toByte())
            v.add(0xA4.toByte())
            v.add(0xF3.toByte())
            v.add(0xA4.toByte())
            v.add(0xB4.toByte())
        }
        checkBytes(v.toByteArray(), EUC_JP)
    }

    @Test
    fun testNotEucJp() {
        val v = mutableListOf<Byte>()
        v.add(0x8F.toByte())
        v.add(0xFE.toByte())
        v.add(0xF7.toByte())
        for (i in 0 until 80) {
            v.add(0xA4.toByte())
            v.add(0xCB.toByte())
            v.add(0xA4.toByte())
            v.add(0xDB.toByte())
            v.add(0xA4.toByte())
            v.add(0xF3.toByte())
            v.add(0xA4.toByte())
            v.add(0xB4.toByte())
        }
        checkBytes(v.toByteArray(), WINDOWS_1252)
    }

    @Test
    fun testNotEucJpBis() {
        val v = mutableListOf<Byte>()
        v.add(0xA8.toByte())
        v.add(0xDF.toByte())
        for (i in 0 until 80) {
            v.add(0xA4.toByte())
            v.add(0xCB.toByte())
            v.add(0xA4.toByte())
            v.add(0xDB.toByte())
            v.add(0xA4.toByte())
            v.add(0xF3.toByte())
            v.add(0xA4.toByte())
            v.add(0xB4.toByte())
        }
        checkBytes(v.toByteArray(), BIG5)
    }

    @Test
    fun testGbkSingleByteFf() {
        val v = mutableListOf<Byte>()
        v.add(0xFF.toByte())
        for (i in 0 until 80) {
            v.add(0xB5.toByte())
            v.add(0xC4.toByte())
        }
        checkBytes(v.toByteArray(), GBK)
    }

    @Test
    fun testGbkSingleByteA0() {
        val v = mutableListOf<Byte>()
        v.add(0xA0.toByte())
        v.add(' '.code.toByte())
        for (i in 0 until 80) {
            v.add(0xB5.toByte())
            v.add(0xC4.toByte())
        }
        checkBytes(v.toByteArray(), GBK)
    }

    @Test
    fun testGbkSingleByteFe() {
        val v = mutableListOf<Byte>()
        v.add(0xFE.toByte())
        v.add(' '.code.toByte())
        for (i in 0 until 80) {
            v.add(0xB5.toByte())
            v.add(0xC4.toByte())
        }
        checkBytes(v.toByteArray(), GBK)
    }

    @Test
    fun testNotGbkSingleByteFc() {
        val v = mutableListOf<Byte>()
        v.add(0xFC.toByte())
        v.add(' '.code.toByte())
        for (i in 0 until 80) {
            v.add(0xB5.toByte())
            v.add(0xC4.toByte())
        }
        checkBytes(v.toByteArray(), ISO_8859_5)
    }
}
