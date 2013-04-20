package actors

import akka.actor.{ActorRef, Actor}
import events._
import scala.math._
import actors.dal.Place


class UserActor(val id : String, val dalActor : ActorRef) extends Actor {

  println(self.path)

  var name : String = "testUser"
  var lastLocation : Option[Location] = None;
  var websocketActor : Option[ActorRef] = None;

  def receive = {
    case event : InputEvent =>
      processInputEvent(event)
      //TODO handle serviceEvents rather than RegisterActor only
    case RegisterActor(actor) =>
      websocketActor = Some(actor)
      println("Web socket actor is registered")
    case Disconnect =>
      websocketActor = None
      //TODO start a timer to kill the actor if no request in 5 mins.
    case msg : UserLocationEvent =>
      if (id != msg.id) {
        lastLocation.foreach { loc =>
          if (((abs(msg.longitude - loc.longitude)) < loc.radius)
          && ((abs(msg.latitude - loc.latitude)) < loc.radius)) {
            websocketActor match {
              case Some(a) => a ! new UserLocation(msg.id, msg.longitude, msg.latitude, msg.name)
              case None => println(s"Message lost ${msg}") //TODO append the message to a queue
            }
          }
        }

      }
    case place : Place =>
      println("Arrived place:" + place)
      websocketActor match {
        case Some(a) => a ! new PlaceLocation(place.id, place.name, place.longitude, place.latitude)
        case None => println(s"Message lost ${place}") //TODO append the message to a queue
      }
    case _ =>
        println("Unexpected message arrived")
        sender ! ErrorMessage(s"Unexpected message")
  }

  private def processInputEvent(event : InputEvent) = {
    event match {
      case location : Location =>
        println("Location arrived")
        lastLocation = Some(location)
        val userLocation = new UserLocationEvent(id, location.longitude, location.latitude, name);
        context.system.eventStream.publish(userLocation)
        dalActor ! userLocation
      case _ => println("Unprocessed something")
        sender ! ErrorMessage(s"Unexpected event: ${event}")
    }
  }
}