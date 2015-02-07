package io.scrapeyard

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.scrapeyard.Models.{SearchRequest, SearchResult}
import io.scrapeyard.ScrapeController.ControllerReq

import scala.language.postfixOps

class Dispatcher(scraperProps: Set[Props], mailerProps: Props) extends Actor
with ActorLogging {

  def this() {
    this(
      Set(
        Props(new ScraperActor(AirHrScraper)),
        Props(new ScraperActor(MomondoScraper)),
        Props(new ScraperActor(QatarScraper))
      ),
      Props[MailerActor]
    )
  }

  val scrapers = scraperProps map {
    prop => context.actorOf(prop)
  }

  var controllers = Set[ActorRef]()
  var responseMap = Map[ActorRef, Set[SearchResult]]()
  var reqEmail: String = _

  def receive: Receive = expectReq

  def expectReq: Receive = {
    case SearchRequest(email, criteria) =>
      reqEmail = email
      controllers = scrapers.map(_ => context.actorOf(Props[ScrapeControllerActor]))
      controllers zip scrapers foreach {
        case (c, s) => c ! ControllerReq(criteria, s)
      }
      context.become(expectResp)
  }

  def expectResp: Receive = {
    case results: Set[SearchResult] =>
      responseMap += (sender -> results)
      if (responseMap.keys == controllers) {
        val results = responseMap.flatMap {
          case (_, results1) => results1
        }.toSet
        val mailer = context.actorOf(mailerProps)
        mailer ! SendResults(reqEmail, "Search results", results)
        context.stop(self)
      }
  }
}
