package events

import akka.actor.ActorRef


case class Coordinate(lng : Double, lat: Double)

sealed trait Message
trait InputMessage extends Message
// auth
// example: { "auth" : { "id" : "blah", "tocken" : "blah" }}
case class Auth(id: String, token : String) extends InputMessage

trait InputEvent extends InputMessage
// ??
// { "type" : "settings", "name" : "Balazs Balazs" , ... }
case class UserSettings(name : Option[String] = None,
                        radius : Option[Double] = None,
                        placeCacheSize : Option[Int] = None) extends InputEvent
// location => loc
// example: { "type" : "loc", "lng" : 1.1, "lat" : 1.1 }
case class Location(coord : Coordinate) extends InputEvent
// disc
// { "type" : "disc", "id" : "placeId", "payload" : "place secret" }
case class Discovery(id : String, payload : String) extends InputEvent
// bump
// { "type" : "bump", ... }
case class Bump(id : String, myPayload : String, herPayload : String)
// get user info
// { "type" : "getUser", "id" : "blahblah" }
case class GetUserInfo(id : String) extends InputEvent

trait OutputMessage extends Message
// { "type" : "userloc", "id": "blahblah", "coord" : { "lng" : 1.1, "lat" : 1.0 } }
case class UserLocation(id : String, coord : Option[Coordinate] = None, disc : Option[Boolean] = None) extends OutputMessage
// { "type" : "place", "id": "blahblah", "coord" : { "lng" : 1.1, "lat" : 1.0 } }
case class PlaceLocation(id : String, name: String, coord : Coordinate) extends OutputMessage
// { "type" : "user", "id" : "blahblah", "name" : "Balazs Balazs" }
case class UserInfo(id : String, name : String) extends OutputMessage
// { "type" : "error", "msg" : "Error message"}
case class ErrorMessage(msg : String) extends OutputMessage


sealed trait InternalEvent

trait UserEvent extends InternalEvent
case class UserLocationEvent(id: String, coord : Coordinate) extends UserEvent
case class DisconnectEvent(id: String, lastCoord: Coordinate) extends UserEvent

trait ServiceEvent extends InternalEvent
case class RegisterActor(actor : ActorRef) extends ServiceEvent
case class Disconnect() extends ServiceEvent