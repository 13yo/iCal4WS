package eu.kaatz.iCal

import akka.actor.Actor
import eu.kaatz.iCal.model.Event
import java.security.MessageDigest
import java.util.Date
import scala.io.Source
import net.fortuna.ical4j.data.UnfoldingReader
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import java.net.URL
import scala.io.Codec

class ImporterActor extends Actor {
  var es: List[Event] = List.empty
  var eventsMD5 = ""
  var updateDate = new Date(0)

  def receive = {
    case AllEvents(now)       => sender ! getAllEvents(now)
    case TaggedEvents(t, now) => sender ! getTagFilteredEvents(t, now)
  }

  private def getAllEvents(now: Date) = {
    events(now)
  }

  private def getTagFilteredEvents(tag: String, now: Date) = {
    events(now).collect({ case x: Event if x.tags.contains(tag) => x })
  }

  private def calcMD5(s: String): String = {
    val md5 = MessageDigest.getInstance("MD5")
    md5.reset()
    md5.update(s.getBytes())
    md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft("") { _ + _ }
  }

  private def events(now: Date) = {
    val md5 = calcMD5(eventsSource.getLines().foldLeft("") { _ + _ })
    println(now.getTime - updateDate.getTime)
    println(eventsMD5)
    println(md5)
    println((now.getTime - updateDate.getTime) < 30000 && eventsMD5.equals(md5))
    if ((now.getTime - updateDate.getTime) < 30000 && eventsMD5.equals(md5))
      es
    else {
      val reader: UnfoldingReader = new UnfoldingReader(eventsSource.reader(), 3000);
      val builder: CalendarBuilder = new CalendarBuilder()
      val calendar: Calendar = builder.build(reader)
      es = scala.collection.JavaConversions.asBuffer(calendar.getComponents()).collect({ case e: VEvent => Event.fromVEvent(e) }).toList
      updateDate = new Date()
      eventsMD5 = md5
      es
    }
  }

  private def eventsSource() = {
    val url = new URL("http://webmail.kaatz-media.de/kronolith/ics.php?c=egroupware%40kaatz-media.de")
    val conn = url.openConnection()
    val login = new StringBuffer("egroupware@kaatz-media.de").append(":").append("aZ3Ug!TLZ^iG").toString();
    val base = login.getBytes();
    val authorizationString = "Basic " + new String(new sun.misc.BASE64Encoder().encode(base));
    conn.setRequestProperty("Authorization", authorizationString);
    Source.fromInputStream(conn.getInputStream())(Codec.UTF8)
  }
}

case class AllEvents(now: Date)
case class TaggedEvents(tag: String, now: Date)