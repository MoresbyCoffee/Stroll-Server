package events

import akka.actor.ActorRef


sealed trait Message
trait InputMessage extends Message
case class Auth(id: String, token : String, name : Option[String] = None) extends InputMessage

trait InputEvent extends InputMessage
case class Location(longitude : Double, latitude : Double, radius : Double) extends InputEvent
case class Discovery(payload : String) extends InputEvent

trait UserEvent extends Message
case class UserLocationEvent(id: String, longitude: Double, latitude: Double, name : String) extends UserEvent

trait ServiceMessage extends Message
case class RegisterActor(actor : ActorRef) extends ServiceMessage
case class Disconnect() extends ServiceMessage

trait OutputMessage extends Message
case class UserLocation(id : String, longitude: Double, latitude: Double, name : String) extends OutputMessage
case class PlaceLocation(id : String, name: String, longitude : Double, latitude : Double) extends OutputMessage
case class ErrorMessage(msg : String) extends OutputMessage