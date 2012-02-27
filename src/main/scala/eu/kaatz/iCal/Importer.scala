package eu.kaatz.iCal
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.data.UnfoldingReader
import java.io.InputStreamReader
import scala.io.Source
import java.net.URL
import scala.io.Codec
import net.fortuna.ical4j.model.Component
import scala.collection.mutable.ListBuffer
import play.api.libs.json.JsValue
import scala.collection.mutable.Buffer
import play.api.libs.json.JsString

object Importer {
   def go = {
      implicit val codec = Codec.UTF8
      val url = new URL("https://webmail.software-friends.de/horde/rpc.php/kronolith/tobias.kaatz@software-friends.de/8HqxAk7pT-BOX3bACfJ3PtA.ics")
      val conn = url.openConnection()
      val login = new StringBuffer("tobias.kaatz@software-friends.de").append(":").append("Z3t1O2-5j8").toString();
      val base = login.getBytes();
      val authorizationString = "Basic " + new String(new sun.misc.BASE64Encoder().encode(base));
      conn.setRequestProperty("Authorization", authorizationString);

      val s = Source.fromInputStream(conn.getInputStream())

      val reader : UnfoldingReader = new UnfoldingReader(s.reader(), 3000);

      val builder : CalendarBuilder = new CalendarBuilder()

      val calendar : Calendar = builder.build(reader)

         def toJson(cals : Buffer[Component]) = {
            val eList : Buffer[(String, JsValue)] = cals.map({ x => ("test" -> JsString("demo")) })
         }

      for (c <- scala.collection.JavaConversions.asBuffer(calendar.getComponents())) {
         println(c.asInstanceOf[Component].getProperties())

      }

   }

}