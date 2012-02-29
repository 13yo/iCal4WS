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
import play.api.libs.json._
import scala.collection.mutable.Buffer
import eu.kaatz.iCal.model.Event
import net.fortuna.ical4j.model.component.VEvent
import java.util.Date
import java.security.MessageDigest
object Importer {
  def go = {
    def calcMD5(l: List[String], s: String): String = l match {
      case Nil =>
        val md5 = MessageDigest.getInstance("MD5")
        md5.reset()
        md5.update(s.getBytes())
        md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft("") { _ + _ }
      case line :: tail => calcMD5(tail, line + s)
    }

    implicit val codec = Codec.UTF8
    //      val url = new URL("http://webmail.kaatz-media.de/kronolith/ics.php?c=egroupware%40kaatz-media.de")
    //      val conn = url.openConnection()
    //      val login = new StringBuffer("tobias.kaatz@software-friends.de").append(":").append("Z3t1O2-5j8").toString();
    //      val base = login.getBytes();
    //      val authorizationString = "Basic " + new String(new sun.misc.BASE64Encoder().encode(base));
    //      conn.setRequestProperty("Authorization", authorizationString);
    //
    //      val s = Source.fromInputStream(conn.getInputStream())

    val s = Source.fromFile("src/main/resources/test.ics")
    println(calcMD5(s.getLines.toList, ""))

    val reader: UnfoldingReader = new UnfoldingReader(s.reader(), 3000);

    val builder: CalendarBuilder = new CalendarBuilder()

    val calendar: Calendar = builder.build(reader)

    scala.collection.JavaConversions.asBuffer(calendar.getComponents()).collect({ case e: VEvent => Event.fromVEvent(e) }).toList
  }

}