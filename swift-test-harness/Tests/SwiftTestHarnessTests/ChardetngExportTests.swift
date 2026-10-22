#if canImport(Testing)
import Testing
import Chardetng

@Suite("Chardetng Swift Export Suite")
struct ChardetngExportTests {
    @Test("Swift module loads cleanly and initializes EncodingDetector")
    func swiftModuleLoads() {
        let detector = EncodingDetector()
        let guessed = detector.guess(tld: nil, allowUtf8: true)
        #expect(guessed.name == "UTF-8")
    }

    @Test("EncodingDetector guess with allowUtf8 false falls back to windows-1252")
    func detectorGuessNonUtf8Fallback() {
        let detector = EncodingDetector()
        let guessed = detector.guess(tld: nil, allowUtf8: false)
        #expect(guessed.name == "windows-1252")
    }

    @Test("EncodingDetector companion factory creates functional instance")
    func companionFactoryInstantiates() {
        let detector = EncodingDetector.Companion.shared.new()
        let guessed = detector.guess(tld: nil, allowUtf8: true)
        #expect(guessed.name == "UTF-8")
    }

    @Test("Tld enum cases and rawValue roundtrip")
    func tldEnumParity() {
        #expect(Tld.allCases.count == 23)
        #expect(Tld.CentralWindows.rawValue == 0)
        #expect(Tld.Western.rawValue == 2)
        #expect(Tld.Generic.rawValue == 21)
        #expect(Tld.Eu.rawValue == 22)
        #expect(Tld.Generic.description == "Generic")
        #expect(Tld.Eu.description == "Eu")
        #expect(Tld(rawValue: 21) == .Generic)
        #expect(Tld(rawValue: 22) == .Eu)
    }

    @Test("Encoding index constants match expected values")
    func encodingIndexConstants() {
        #expect(ASCII_DIGIT == 100)
        #expect(WINDOWS_1258_INDEX == 0)
        #expect(WINDOWS_1250_INDEX == 1)
        #expect(ISO_8859_2_INDEX == 2)
        #expect(WINDOWS_1252_INDEX == 7)
        #expect(WINDOWS_1255_INDEX == 12)
        #expect(ISO_8859_8_INDEX == 13)
    }
}
#elseif canImport(XCTest)
import XCTest
import Chardetng

final class ChardetngExportTests: XCTestCase {
    func testSwiftModuleLoads() {
        let detector = EncodingDetector()
        let guessed = detector.guess(tld: nil, allowUtf8: true)
        XCTAssertEqual(guessed.name, "UTF-8")
    }

    func testDetectorGuessNonUtf8Fallback() {
        let detector = EncodingDetector()
        let guessed = detector.guess(tld: nil, allowUtf8: false)
        XCTAssertEqual(guessed.name, "windows-1252")
    }

    func testCompanionFactoryInstantiates() {
        let detector = EncodingDetector.Companion.shared.new()
        let guessed = detector.guess(tld: nil, allowUtf8: true)
        XCTAssertEqual(guessed.name, "UTF-8")
    }

    func testTldEnumParity() {
        XCTAssertEqual(Tld.allCases.count, 23)
        XCTAssertEqual(Tld.CentralWindows.rawValue, 0)
        XCTAssertEqual(Tld.Western.rawValue, 2)
        XCTAssertEqual(Tld.Generic.rawValue, 21)
        XCTAssertEqual(Tld.Eu.rawValue, 22)
        XCTAssertEqual(Tld.Generic.description, "Generic")
        XCTAssertEqual(Tld.Eu.description, "Eu")
        XCTAssertEqual(Tld(rawValue: 21), .Generic)
        XCTAssertEqual(Tld(rawValue: 22), .Eu)
    }

    func testEncodingIndexConstants() {
        XCTAssertEqual(ASCII_DIGIT, 100)
        XCTAssertEqual(WINDOWS_1258_INDEX, 0)
        XCTAssertEqual(WINDOWS_1250_INDEX, 1)
        XCTAssertEqual(ISO_8859_2_INDEX, 2)
        XCTAssertEqual(WINDOWS_1252_INDEX, 7)
        XCTAssertEqual(WINDOWS_1255_INDEX, 12)
        XCTAssertEqual(ISO_8859_8_INDEX, 13)
    }
}
#endif
