import org.scalatest.flatspec.AnyFlatSpec
import scalaj.http.Http

// object MockHttpUtils extends HttpUtils {
//   override def parse(url: String): HttpRequest = {
//     new HttpRequest {
//       override def getStatusCode(url: String): Int = 200
//     }
//   }
// }

class MainSpec extends AnyFlatSpec {
  "formatUrl" should "return the formatted URL" in {
    val keyword = "Scala"
    val limit = 10
    val expectedUrl =
      "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=&sroffset=0&list=search&srsearch=Scala&srlimit=10"

    val actualUrl = Main.formatUrl(keyword, limit)

    assert(actualUrl == expectedUrl)
  }

  "parseJson" should "return a list of WikiPage objects" in {
    val rawJson =
      "{\"query\": {\"search\": [{\"title\": \"Page 1\", \"wordcount\": 100}, {\"title\": \"Page 2\", \"wordcount\": 200}]}}"
    val expectedPages = Seq(WikiPage("Page 1", 100), WikiPage("Page 2", 200))

    val actualPages = Main.parseJson(rawJson)

    assert(actualPages == expectedPages)
  }

  "totalWords" should "return 0 for an empty list of pages" in {
    val pages = Seq.empty[WikiPage]

    val total = Main.totalWords(pages)

    assert(total == 0)
  }

  it should "return the total number of words for a non-empty list of pages" in {
    val pages = Seq(WikiPage("Page 1", 100), WikiPage("Page 2", 200))
    val expectedTotal = 300

    val total = Main.totalWords(pages)

    assert(total == expectedTotal)
  }

  "parseArguments" should "return Some(config) for valid arguments" in {
    val args = Array("--limit", "20", "Scala")
    val expectedConfig = Config(20, "Scala")

    val actualConfig = Main.parseArguments(args)

    assert(actualConfig.contains(expectedConfig))
  }

  it should "return None for invalid arguments" in {
    val args = Array("--limit", "abc", "Scala")

    val actualConfig = Main.parseArguments(args)

    assert(actualConfig.isEmpty)
  }

  // "getPages" should "return Right with the page content for a successful HTTP request" in {
  //   val mockHttpUtils = new MockHttpUtils
  //   val url = "https://example.com/"
  //   val responseBody = "Page content"
  //   mockHttpUtils.setResponse(url, 200, responseBody)

  //   val result = Main.getPages(url)(mockHttpUtils)

  //   assert(result == Right(responseBody))
  // }

  // it should "return Left with the error code for a failed HTTP request" in {
  //   val mockHttpUtils = new MockHttpUtils
  //   val url = "https://example.com/"
  //   val errorCode = 404
  //   mockHttpUtils.setResponse(url, errorCode, "")

  //   val result = Main.getPages(url)(mockHttpUtils)

  //   assert(result == Left(errorCode))
  // }
}
