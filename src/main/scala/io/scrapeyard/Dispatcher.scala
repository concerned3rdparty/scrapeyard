package io.scrapeyard

import akka.actor.{Actor, ActorLogging, Props}
import io.scrapeyard.ScrapeMailer.SendEmail
import io.scrapeyard.Models.{BatchSearchCriteria, SearchParams, SearchRequest}
import spray.json._
import ModelsJsonSupport._

import scala.language.postfixOps
import scala.util.{Success, Failure, Try}

class Dispatcher extends Actor with ActorLogging {

  import io.scrapeyard.Dispatcher._


  val mailer = context.actorOf(Props[MailerActor], "mailer")

  def receive: Receive = {
    case req: SearchRequest => dispatch(req)
  }

  def dispatch(req: SearchRequest) = {
    val paramList = toSearchParams(req.criteria)

    val searchTries = paramList flatMap { ps => Seq(
      Try(AirHrScraper.doIt(ps)),
      Try(MomondoScraper.doIt(ps)),
      Try(QatarScraper.doIt(ps))
    )}

    val (succs, fails) = searchTries.partition(_.isSuccess)

    val results = succs map (_.get)
    mailer ! SendEmail(req.email, "Search results", results.toJson.prettyPrint)

    if (fails.nonEmpty) {
      log.warning("Failed searches: ")
      fails.foreach {
        case Failure(t) => log.error(t, t.getMessage)
        case Success(s) => log.error(
          s"Unexpected successful search found in filtered failed searches: $s")
      }
    }
  }
}

object Dispatcher {
  def toSearchParams(criteria: BatchSearchCriteria): Seq[SearchParams] = {
    var depDates = Vector(criteria.depFrom)
    while(depDates.last.compareTo(criteria.depUntil) < 0) {
      depDates = depDates :+ depDates.last.plusDays(1)
    }

    var retDates = Vector(criteria.retFrom)
    while(retDates.last.compareTo(criteria.retUntil) < 0) {
      retDates = retDates :+ retDates.last.plusDays(1)
    }


    val searches = for {
      orig <- criteria.origs
      dest <- criteria.dests
      dep <- depDates
      ret <- retDates
    } yield SearchParams(orig, dest, dep, ret)

    searches.toVector
  }
}
