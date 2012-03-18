package eu.kaatz.iCal

import akka.actor.Actor
import eu.kaatz.iCal.model.EventImplicits
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
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.TreeMap

class ImporterActor extends Actor with EventImplicits {
   var es = ListBuffer[Event]()
   var eMap = TreeMap[String, Event]().empty
   var eventsMD5 = ""
   var updateDate = new Date(0)

   def receive = {
      case AllEvents(now)       => sender ! getAllEvents(now)
      case TaggedEvents(t, now) => sender ! getTagFilteredEvents(t, now)
      case OneEvent(id)         => sender ! getEvent(id)
   }

   private def getAllEvents(now : Date) = {
      //    println(events(now))
      events(now)
   }

   private def getEvent(id : String) = {
      //    println(events(now))
      eMap(id)
   }

   private def getTagFilteredEvents(tag : String, now : Date) = {
      events(now).collect({ case x : Event if x.tags.contains(tag) => x })
   }

   private def calcMD5(s : String) : String = {
      val md5 = MessageDigest.getInstance("MD5")
      md5.reset()
      md5.update(s.getBytes())
      md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft("") { _ + _ }
   }

   private def events(now : Date) = {
      val md5 = calcMD5(eventsSource.getLines().foldLeft("") { _ + _ })
      //eventsSource.getLines().foreach(println)
      //    println(now.getTime - updateDate.getTime)
      //    println(eventsMD5)
      //    println(md5)
      //    println((now.getTime - updateDate.getTime) < 30000 && eventsMD5.equals(md5))
      if ((now.getTime - updateDate.getTime) < 30000 && eventsMD5.equals(md5))
         //es.toList
         eMap.values.toList
      else {
         val reader : UnfoldingReader = new UnfoldingReader(eventsSource.reader(), 3000);
         val builder : CalendarBuilder = new CalendarBuilder()
         val calendar : Calendar = builder.build(reader)

         //es.clear
         eMap = eMap.empty
         val componentIt = calendar.getComponents().iterator()
         while (componentIt.hasNext())
            componentIt.next() match {
               case e : VEvent =>
                  //es ++= e
                  eMap = eMap ++ VEvent2Events(e).map { event => ((event.id + event.start) -> event) }
               case _ =>
            }

         updateDate = new Date()
         eventsMD5 = md5
         //      es.toList
         eMap.values.toList
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

case class AllEvents(now : Date)
case class OneEvent(id : String)
case class TaggedEvents(tag : String, now : Date)