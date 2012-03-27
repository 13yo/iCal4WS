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
import net.liftweb.http.S

object App extends RestHelper with EventImplicits {
  //Akka
  private val system = ActorSystem("fegCal")
  private lazy val eventActor = system.actorOf(Props[ImporterActor])
  implicit val timeout = Timeout(3000 milliseconds)

  serve {
    case "events" :: Nil JsonGet _ =>
      val baseURL = S.hostAndPath + "/events/"
      val tagFilter = S.params("tag")
      if (tagFilter.isEmpty)
        // serve the URL /events
        RestContinuation.async(
          satisfyRequest => {
            val events = (eventActor ? AllEvents(new Date())).mapTo[List[Event]]
            events onComplete {
              case Right(result) => satisfyRequest(EList2Json(result, baseURL))
              case Left(failure) => failure.printStackTrace()
            }
          })
      // serve the URL /events?tag=:tag
      else
        RestContinuation.async(
          satisfyRequest => {
            val events = (eventActor ? TaggedEvents(tagFilter, new Date())).mapTo[List[Event]]
            events onComplete {
              case Right(result) => satisfyRequest(EList2Json(result, baseURL))
              case Left(failure) => println(failure)
            }
          })
    // serve the URL /events/:id
    case "events" :: id :: _ JsonGet _ =>
      val baseURL = S.hostAndPath + "/events/"
      RestContinuation.async(
        satisfyRequest => {
          val event = (eventActor ? OneEvent(id)).mapTo[Event]
          event onComplete {
            case Right(result) => satisfyRequest(Event2Json(result, baseURL))
            case Left(failure) => println(failure)
          }
        })
  }
}
