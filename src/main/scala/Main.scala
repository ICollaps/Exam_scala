import scopt.OParser
import scalaj.http.Http
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

case class Config(limit: Int = 10, keyword: String = "")

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

  def getPages(url: String): Either[Int, String] = {
    val result = Http(url).asString
    if (result.code == 200) Right(result.body)
    else Left(result.code)
  }

  def run(config: Config): Unit = {
    println(config)
    println(formatUrl(config.keyword, config.limit))
  }

  def formatUrl(keyword: String, limit: Int): String = {
    val encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString)
    "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=&sroffset=0&list=search&srsearch=%s&srlimit=%d".format(encodedKeyword, limit)
  }

}
