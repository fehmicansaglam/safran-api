package safran

import akka.util.Timeout
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.actor.{ ActorRef, Actor, Props, ActorSystem }
import akka.actor.ActorDSL._
import akka.pattern.ask
import akka.io.IO
import spray.can.Http
import spray.routing._
import spray.http.MediaTypes._
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import scala.xml.XML

case class Item(title: String, description: String, pubDate: String, link: String)

class SafranServiceActor(val fetcher: ActorRef) extends Actor with SafranService {

  implicit def executionContext = actorRefFactory.dispatcher

  def actorRefFactory = context

  def receive = runRoute(myRoute)
}

trait SafranService extends HttpService with SprayJsonSupport {

  object ItemJsonProtocol extends DefaultJsonProtocol {
    implicit val ItemFormat = jsonFormat4(Item)
  }

  import ItemJsonProtocol._

  def fetcher: ActorRef

  implicit def executionContext: ExecutionContext

  val myRoute =
    path("items") {
      get {
        respondWithMediaType(`application/json`) {
          complete {
            implicit val timeout = Timeout(5 seconds)
            (fetcher ? "list").mapTo[Seq[Item]]
          }
        }
      }
    }
}

object Safran extends App {

  implicit val system = ActorSystem("safran-system")

  val fetcher = actor(new Act {
    var items: Seq[Item] = Nil
    become {
      case "fetch" =>
        items = XML.load("http://www.safran.io/feed.rss") \\ "item" map { item =>
          Item((item \ "title").text, (item \ "description").text, (item \ "pubDate").text, (item \ "link").text)
        } take (10)
        println(s"Fetched new items: $items")

      case "list" => sender() ! items
    }
  })

  import system.dispatcher

  system.scheduler.schedule(0 milliseconds,
    60 seconds,
    fetcher,
    "fetch")

  val service = system.actorOf(Props(new SafranServiceActor(fetcher)), "safran-service")

  // start a new HTTP server on port 80 with our service actor as the handler
  IO(Http) ! Http.Bind(service, interface = "0.0.0.0", port = 8000)

}
