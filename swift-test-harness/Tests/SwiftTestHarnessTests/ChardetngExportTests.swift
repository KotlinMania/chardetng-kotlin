import Testing
import Chardetng

@Suite
struct ChardetngExportTests {
    @Test
    func testSwiftModuleLoads() {
        let detector = EncodingDetector()
        _ = detector.guess(tld: nil, allowUtf8: true)
    }
}

