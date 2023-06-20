import scopt.OParser
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import play.api.libs.json.{Json, JsArray, JsValue}
import scalaj.http.{HttpRequest, HttpOptions, HttpConstants}
import scalaj.http.Http



case class Config(limit: Int = 10, keyword: String = "")

case class WikiPage(title: String, words: Int)

trait HttpUtils {
  def parse(url: String): HttpRequest
}

object HttpUtilsImpl extends HttpUtils {
  def parse(url: String): HttpRequest = {
    Http(url)
  }
}

object Main extends App {
  parseArguments(args) match {
    case Some(config) => run(config)
    case _            => println("Unable to parse arguments")
  }

  def parseArguments(args: Array[String]): Option[Config] = {
    val builder = OParser.builder[Config]
    val parser = {
      import builder._
      OParser.sequence(
        programName("WikiStats"),
        opt[Int]('l', "limit")
          .action((value, config) => config.copy(limit = value))
          .text("Limit of the number of pages to retrieve"),
        arg[String]("<keyword>")
          .required()
          .action((value, config) => config.copy(keyword = value))
          .text("Keyword to search for")
      )
    }

    OParser.parse(parser, args, Config())
  }

  def getPages(url: String)(implicit httpUtils: HttpUtils): Either[Int, String] = {
    val result = httpUtils.parse(url).asString
    if (result.code == 200) Right(result.body)
    else Left(result.code)
  }

  def run(config: Config): Unit = {
    val url = formatUrl(config.keyword, config.limit)
    implicit val httpUtils: HttpUtils = HttpUtilsImpl
    getPages(url) match {
      case Right(body: String) =>
        val pages = parseJson(body)
        println(s"Number of pages found: ${pages.length}")
        pages.foreach(page => println(page))

        val total = totalWords(pages)
        println(s"Total number of words across all pages: $total")

        val average = if (pages.nonEmpty) total.toDouble / pages.length else 0
        println(s"Average number of words per page: $average")
      case Left(errorCode) => println(s"An error occurred: $errorCode")
    }
  }

  def formatUrl(keyword: String, limit: Int): String = {
    val encodedKeyword = s"https://en.wikipedia.org/w/api.php?action=query&format=json&prop=&sroffset=0&list=search&srsearch=$keyword&srlimit=$limit"
    encodedKeyword
  }

  def parseJson(rawJson: String): Seq[WikiPage] = {
    val json = Json.parse(rawJson)
    val pagesJson = (json \ "query" \ "search").as[JsArray].value

    pagesJson.map { pageJson =>
      WikiPage(
        title = (pageJson \ "title").as[String],
        words = (pageJson \ "wordcount").as[Int]
      )
    }
  }

  def totalWords(pages: Seq[WikiPage]): Int = {
    pages.foldLeft(0)((total, page) => total + page.words)
  }
}
