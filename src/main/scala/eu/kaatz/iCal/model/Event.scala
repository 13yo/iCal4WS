package eu.kaatz.iCal.model
import play.api.libs.json._
import java.util.Date
import java.text.SimpleDateFormat
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.DateProperty

case class Event(title: String, description: String, tags: Seq[String], start: Date, end: Date)

object Event {

  implicit object EventFormat extends Format[Event] {
    def reads(json: JsValue): Event = Event(
      (json \ "title").as[String],
      (json \ "description").as[String],
      (json \ "tags").asOpt[Seq[String]].getOrElse(List()),
      new Date((json \ "begin").as[Long]),
      new Date((json \ "end").as[Long]))

    def writes(e: Event): JsValue = JsObject(Seq(
      "title" -> JsString(e.title),
      "description" -> JsString(e.description),
      "tags" -> JsArray(e.tags.map(x => JsString(x.trim)).toList),
      "begin" -> JsString(e.start.getTime().toString),
      "end" -> JsString(e.end.getTime().toString)))
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

    val title = vs(event.getSummary())
    val description = vs(event.getDescription())
    val tags = vs(event.getProperty(Property.CATEGORIES)).replaceAll("\\\\,", ",").split(",")
    val begin = vd(event.getStartDate())
    val end = vd(event.getEndDate())
    Event(title, description, tags, begin, end)
  }
}