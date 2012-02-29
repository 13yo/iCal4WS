package eu.kaatz.iCal

import play.api.libs.json.Json._
import com.typesafe.play.mini._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.concurrent._
import eu.kaatz.iCal.model.Event
import java.util.Date
import akka.actor.{ ActorSystem, Props, Actor }
import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask

/**
 * this application is registered via Global
 */
object App extends Application {
  private final val TagsPattern = """/tag/(\w+)""".r
  //Akka
  private val system = ActorSystem("fegCal")
  private lazy val eventActor = system.actorOf(Props[ImporterActor])
  implicit val timeout = Timeout(1000 milliseconds)

  def route = {
    case GET(Path("/")) => Action {
      AsyncResult {
        (eventActor ask AllEvents(new Date())).mapTo[List[Event]].asPromise.map { es =>
          Ok(toJson(es))
        }
      }
    }
    case GET(Path(TagsPattern(tag))) => Action {
      AsyncResult {
        (eventActor ask TaggedEvents(tag, new Date())).mapTo[List[Event]].asPromise.map { es =>
          Ok(toJson(es))
        }
      }
    }
  }
}
