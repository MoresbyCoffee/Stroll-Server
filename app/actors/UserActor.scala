package actors

import akka.actor.{ActorRef, Actor}
import events._
import scala.math._
import actors.dal.Place


class UserActor(val id : String, val dalActor : ActorRef) extends Actor {

  println(self.path)

  var name : String = "testUser"
  var lastLocation : Option[Coordinate] = None
  var websocketActor : Option[ActorRef] = None
  var radius : Double = 1.0

  def receive = {
    case inputMessage : InputMessage =>
      processInputEvent(inputMessage)
      //TODO handle serviceEvents rather than RegisterActor only
    case RegisterActor(actor) =>
      websocketActor = Some(actor)
      println("Web socket actor is registered")
    case Disconnect =>
      websocketActor = None
      lastLocation.foreach { ll =>
        context.system.eventStream.publish(new DisconnectEvent(id, ll))
      }
      //TODO start a timer to kill the actor if no request in 5 mins.
    case msg : UserLocationEvent =>
      ifInRange(msg.id, msg.coord, () => {
        sendMessage(new UserLocation(msg.id, Some(msg.coord)))
      })
    case msg : DisconnectEvent =>
      ifInRange(msg.id, msg.lastCoord, () => {
        sendMessage(new UserLocation(msg.id, disc = Some(true)))
      })
    case place : Place =>
      println("Arrived place:" + place)
      sendMessage(new PlaceLocation(place.id, place.name, Coordinate(place.longitude, place.latitude)))
  }

  private def ifInRange(id : String, coord : Coordinate, f : () => Unit) {
    if (this.id != id) {
      lastLocation.foreach { loc =>
        if (((abs(coord.lng - loc.lng)) < radius)
          && ((abs(coord.lat - loc.lat)) < radius)) {
            f()
        }
      }
    }
  }

  private def sendMessage(msg : OutputMessage) {
    websocketActor match {
      case Some(a) => a ! msg
      case None => println(s"Message lost $msg") //TODO append the message to a queue
    }
  }

  private def processInputEvent(event : InputMessage) {
    event match {
      case location : Location =>
        println("Location arrived")
        lastLocation = Some(location.coord)
        val userLocation = new UserLocationEvent(id, location.coord)
        context.system.eventStream.publish(userLocation)
        dalActor ! userLocation
      case _ => println("Unprocessed something")
        sender ! ErrorMessage(s"Unexpected event: $event")
        //TODO handle all the possibilities
    }
  }
}