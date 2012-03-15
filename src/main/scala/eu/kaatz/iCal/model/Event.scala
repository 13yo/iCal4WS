package eu.kaatz.iCal.model
import java.util.Date
import java.text.SimpleDateFormat
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.DateProperty
import net.fortuna.ical4j.model.ComponentFactory
import net.fortuna.ical4j.model.property.RRule
import org.scala_tools.time.Imports._
import net.fortuna.ical4j.model.parameter.Value
import scala.collection.JavaConversions
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.fortuna.ical4j.model.Recur
import scala.collection.mutable.ListBuffer
import java.net.URLEncoder

case class Event(id: String, title: String, description: String, tags: Seq[String], start: Date, end: Date)

object Event {
  implicit def str2date(dString: String): Date = {
    new SimpleDateFormat("yyyyMMdd-HHmmss-").parse(dString.replace('T', '-').replace('Z', '-'))
  }

  def fromVEvent(event: VEvent): Seq[Event] = {
    def vd(p: DateProperty) = p match {
      case null => null
      case _    => p.getDate()
    }
    def vs(p: Property) = p match {
      case null => ""
      case _    => p.getValue()
    }
    def toEvent(e: VEvent) = {
      val id = vs(e.getUid())
      val title = vs(e.getSummary())
      val description = vs(e.getDescription())
      val tags = vs(e.getProperty(Property.CATEGORIES)).replaceAll("\\\\,", ",").split(",")
      val begin = vd(e.getStartDate())
      val end = vd(e.getEndDate())
      Event(id, title, description, tags, begin, end)
    }

    val eList = ListBuffer[Event]()
    val p = event.getProperty(Property.RRULE)
    if (p != null) {
      println(p.asInstanceOf[RRule].getRecur())
      val r: Recur = p.asInstanceOf[RRule].getRecur()
      val begin = new net.fortuna.ical4j.model.Date((DateTime.now - 1.month).toDate)
      val end = new net.fortuna.ical4j.model.Date((DateTime.now + 1.year).toDate)
      val period = new net.fortuna.ical4j.model.Period(new net.fortuna.ical4j.model.DateTime(event.getProperty(Property.DTSTART).getValue()), new net.fortuna.ical4j.model.DateTime(event.getProperty(Property.DTEND).getValue()))
      val dateIt = r.getDates(begin, end, Value.DATE_TIME).iterator
      while (dateIt.hasNext()) {
        val d = dateIt.next().asInstanceOf[net.fortuna.ical4j.model.DateTime]
        val per = new net.fortuna.ical4j.model.Period(d, period.getDuration())
        val eProps = event.getProperties()
        eProps.getProperty(Property.DTSTART).setValue(per.getStart().toString())
        eProps.getProperty(Property.DTEND).setValue(per.getEnd().toString())
        eProps.remove(eProps.getProperty(Property.RRULE))
        eList += toEvent(new VEvent(eProps))
      }
    } else {
      eList += toEvent(event)
    }
    eList.toSeq
  }
}

object EventImplicits extends EventImplicits
trait EventImplicits {
  import net.liftweb.json._
  //  import net.liftweb.json.JsonParser._
  implicit val f = DefaultFormats

  implicit def VEvent2Events(e: VEvent): TraversableOnce[Event] = Event.fromVEvent(e)
  implicit def Json2Event(s: String) = parse(s).extract[Event]
  implicit def Event2Json(e: Event): JObject = ("id", e.id) ~ ("title", e.title) ~ ("description", e.description) ~
    ("tags", JArray(e.tags.map(x => JString(x.trim)).toList)) ~
    ("start", (e.start.getTime() / 1000)) ~
    ("end", (e.end.getTime() / 1000)) ~ ("url", URLEncoder.encode(e.id + e.start))
  implicit def EList2Json(l: List[Event]) = JArray(l map { Event2Json(_) })
}