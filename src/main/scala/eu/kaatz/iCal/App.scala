package eu.kaatz.iCal

import play.api.libs.json.Json._
import com.typesafe.play.mini._
import play.api.mvc._
import play.api.mvc.Results._
import eu.kaatz.iCal.model.Event
import java.util.Date

/**
 * this application is registered via Global
 */
object App extends Application {
  def route = {
    case GET(Path("/coco")) & QueryString(qs) => Action { request =>
      println(request.body)
      println(play.api.Play.current)
      val result = QueryString(qs, "foo").getOrElse("noh")
      Ok(<h1>It works!, query String { result }</h1>).as("text/html")
    }
    case GET(Path("/import")) => Action {
      val res = Importer.go
      Ok(toJson(res))
    }
  }
}
