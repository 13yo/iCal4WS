package eu.kaatz.iCal

import eu.kaatz.iCal.model.Event
import eu.kaatz.iCal.model.EventImplicits
import java.util.Date
import akka.actor.{ ActorSystem, Props, Actor }
import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.rest.RestContinuation
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonAST.JArray

/**
 * this application is registered via Global
 */
object App extends RestHelper with EventImplicits {
  private final val TagsPattern = """/tag/(\w+)""".r
  //Akka
  private val system = ActorSystem("fegCal")
  private lazy val eventActor = system.actorOf(Props[ImporterActor])
  implicit val timeout = Timeout(3000 milliseconds)

  // serve the URL /async/:id
  serve {
    case "events" :: Nil JsonGet _ =>
      RestContinuation.async(
        satisfyRequest => {
          val events = (eventActor ? AllEvents(new Date())).mapTo[List[Event]]
          events onComplete {
            case Right(result) => satisfyRequest(EList2Json(result))
            case Left(failure) => failure.printStackTrace()
          }
        })
    case "events" :: "tag" :: tag :: _ JsonGet _ =>
      RestContinuation.async(
        satisfyRequest => {
          val events = (eventActor ? TaggedEvents(tag, new Date())).mapTo[List[Event]].map { es =>
            es.asInstanceOf[JValue]
          }
          events onComplete {
            case Right(result) => satisfyRequest(result)
            case Left(failure) => println(failure)
          }
        })
  }
}
