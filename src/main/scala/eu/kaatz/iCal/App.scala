package eu.kaatz.iCal

import com.typesafe.play.mini._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json.JsObject

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
      case GET(Path("/import")) => Action { request =>

         val res = Importer.go.toString
         Ok(JsObject(res)).as("text/html")
      }
   }
}
