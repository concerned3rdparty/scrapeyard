package io.scrapeyard

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import spray.httpx.SprayJsonSupport
import spray.json._

object Models {
  case class BatchSearchCriteria(
                                origs: Set[String],
                                dests: Set[String],
                                depFrom: DateTime,
                                depUntil: DateTime,
                                retFrom: DateTime,
                                retUntil: DateTime
                                )
  
  case class SearchRequest(email: String, criteria: BatchSearchCriteria)

  case class SearchParams(
                           origin: String,
                           destination: String,
                           departure: DateTime,
                           returning: DateTime
                           )

  case class SearchYield(price: String, url: String)

  case class SearchResult(params: SearchParams, yld: SearchYield)
  
  case class SearchResponse(email: String, results: Seq[SearchResult])
}

object ModelsJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  import Models._
  
  implicit object DateTimeJsonFormat extends RootJsonFormat[DateTime] {
    val Formatter = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC

    def write(dt: DateTime) = JsString(Formatter.print(dt))

    def read(value: JsValue) = value match {
      case JsString(v) => Formatter.parseDateTime(v)
      case v => deserializationError(s"Unable to interpret value '$v' as date")
    }
  }

  implicit val BscFormat = jsonFormat6(BatchSearchCriteria)
  implicit val SearchReqFormat = jsonFormat2(SearchRequest)
  implicit val SearchParamsFormat = jsonFormat4(SearchParams)
  implicit val SearchYldFormat = jsonFormat2(SearchYield)
  implicit val SearchResFormat = jsonFormat2(SearchResult)
}


