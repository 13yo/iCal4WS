package eu.kaatz.iCal.model
import play.api.libs.json._
import java.util.Date
import java.text.SimpleDateFormat
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.DateProperty

case class Event(id: String, title: String, description: String, tags: Seq[String], start: Date, end: Date)

object Event {

  implicit object EventFormat extends Format[Event] {
    def reads(json: JsValue): Event = Event(
      (json \ "id").as[String],
      (json \ "title").as[String],
      (json \ "description").as[String],
      (json \ "tags").asOpt[Seq[String]].getOrElse(List()),
      new Date((json \ "start").as[Long] * 1000),
      new Date((json \ "end").as[Long] * 1000))

    def writes(e: Event): JsValue = JsObject(Seq(
      "id" -> JsString(e.id),
      "title" -> JsString(e.title),
      "description" -> JsString(e.description),
      "tags" -> JsArray(e.tags.map(x => JsString(x.trim)).toList),
      "start" -> JsString((e.start.getTime() / 1000).toString),
      "end" -> JsString((e.end.getTime() / 1000).toString)))
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

    val id = vs(event.getUid())
    val title = vs(event.getSummary())
    val description = vs(event.getDescription())
    val tags = vs(event.getProperty(Property.CATEGORIES)).replaceAll("\\\\,", ",").split(",")
    val begin = vd(event.getStartDate())
    val end = vd(event.getEndDate())
    Event(id, title, description, tags, begin, end)
  }
}