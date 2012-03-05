package eu.kaatz.iCal.model
import play.api.libs.json._
import java.util.Date
import java.text.SimpleDateFormat
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.DateProperty
import net.fortuna.ical4j.model.ComponentFactory
import net.fortuna.ical4j.model.property.RRule
import org.joda.time.DateTime
import org.scala_tools.time.Imports._
import net.fortuna.ical4j.model.parameter.Value
import scala.collection.JavaConversions

case class Event(id: String, title: String, description: String, tags: Seq[String], start: Date, end: Date)

object Event {

  implicit object EventFormat extends Format[Event] {
    def reads(json: JsValue): Event = Event(
      (json \ "id").as[String],
      (json \ "title").as[String],
      (json \ "description").as[String],
      (json \ "tags").asOpt[Seq[String]].getOrElse(List()),
      new Date((json \ "start").as[Int] * 1000),
      new Date((json \ "end").as[Int] * 1000))

    def writes(e: Event): JsValue = JsObject(Seq(
      "id" -> JsString(e.id),
      "title" -> JsString(e.title),
      "url" -> JsString(new StringBuffer().append("javascript:openDialog('").
        append(e.title).
        append("','").
        append(e.description).
        append("')").toString),
      "tags" -> JsArray(e.tags.map(x => JsString(x.trim)).toList),
      "start" -> JsNumber((e.start.getTime() / 1000)),
      "end" -> JsNumber((e.end.getTime() / 1000))))
  }

  implicit def str2date(dString: String): Date = {
    new SimpleDateFormat("yyyyMMdd-HHmmss-").parse(dString.replace('T', '-').replace('Z', '-'))
  }

  def fromVEvent(event: VEvent): Event = {
    def vd(p: DateProperty) = p match {
      case null => null
      case _    => p.getDate()
    }
    def vs(p: Property) = p match {
      case null => ""
      case _    => p.getValue()
    }
    val rrule = event.getProperty(Property.RRULE).asInstanceOf[RRule]
    if (rrule != null) {
      val begin = new net.fortuna.ical4j.model.Date((DateTime.now - 1.month).toDate)
      val end = new net.fortuna.ical4j.model.Date((DateTime.now + 1.year).toDate)
      //      JavaConversions.asBuffer(rrule.getRecur().getDates(begin, end, Value.DATE_TIME)).collect({ case d: net.fortuna.ical4j.model.DateTime => 
      //        val copy = event.copy()
      //        val pList = copy.getProperties()
      //        copy.getProperty(Property.)
      //        copy.calculateRecurrenceSet()
      //        pList.remove(Property.RRULE)
      //        pList.getProperty(Property.DTSTART).setValue(d.)
      //        pList.getProperty(Property.DTEND).setValue()
      //      })

    } else {

    }

    val id = vs(event.getUid())
    val title = vs(event.getSummary())
    val description = vs(event.getDescription())
    val tags = vs(event.getProperty(Property.CATEGORIES)).replaceAll("\\\\,", ",").split(",")
    val begin = vd(event.getStartDate())
    val end = vd(event.getEndDate())
    Event(id, title, description, tags, begin, end)
  }
}