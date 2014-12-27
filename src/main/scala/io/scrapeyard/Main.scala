package io.scrapeyard

import org.joda.time.DateTime

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {
  println("hello world")

  val origs = Set("ZAG", "BUD")
  val dests = Set("DPS")
  val depFrom = DateTime.parse("2015-05-20T00:00:00.000Z")
  val depUntil = DateTime.parse("2015-05-22T00:00:00.000Z")
  val retFrom = DateTime.parse("2015-07-20T00:00:00.000Z")
  val retUntil = DateTime.parse("2015-07-31T00:00:00.000Z")
  val criteria = BatchSearchCriteria(origs, dests, depFrom, depUntil, retFrom, retUntil)
  val paramList = Dispatcher.toSearchParams(criteria)

//  private val qatarFuture = Future {
//    paramList.foreach { ps =>
//      Try(QatarScraper.doIt(ps)) match {
//        case Success(r) => println(r)
//        case Failure(t) => println((ps, t))
//      }
//    }
//  }

  private val airHrFuture = Future {
    paramList.foreach { ps =>
      Try(AirHrScraper.doIt(ps)) match {
        case Success(r) => println(r)
        case Failure(t) => println((ps, t))
      }
    }
  }

//  private val momondoFuture = Future {
//    paramList.foreach { ps =>
//      Try(MomondoScraper.doIt(ps)) match {
//        case Success(r) => println(r)
//        case Failure(t) => println((ps, t))
//      }
//    }
//  }
//  momondoFuture.onComplete(println(_))

//  private val airHrFuture = Future {
//    paramList.foreach { ps =>
//      val res = Try(AirHrScraper.doIt(ps))
//      println(res)
//    }
//  }
//  airHrFuture.onComplete(println(_))

//  Await.ready(qatarFuture, 1 hour)
//  Await.ready(momondoFuture, 1 hour)
  Await.ready(airHrFuture, 1 hour)


//
//  val arrivals = Seq(
//    "25-Jul-2015",
//    "26-Jul-2015",
//    "27-Jul-2015",
//    "28-Jul-2015",
//    "29-Jul-2015",
//    "30-Jul-2015",
//    "31-Jul-2015",
//    "01-Aug-2015",
//    "02-Aug-2015",
//    "03-Aug-2015",
//    "04-Aug-2015",
//    "05-Aug-2015",
//    "06-Aug-2015",
//    "07-Aug-2015",
//    "08-Aug-2015",
//    "09-Aug-2015",
//    "10-Aug-2015"
//  )
//
//  val arrivalsToPrices = arrivals.map { a =>
//    val pair = (a, Scraper.doIt(a))
//    println(pair)
//    pair
//  }.toMap
//
//  println(arrivalsToPrices.mkString("\n"))

//  println(MomondoScraper.doIt(params))
}