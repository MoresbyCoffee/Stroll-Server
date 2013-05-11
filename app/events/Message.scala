/* This file is part of Moresby Stroll Server.
 *
 * Moresby Stroll Server is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Moresby Stroll Server is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Moresby Stroll Server.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package events

import akka.actor.ActorRef
import common.Coordinate


sealed trait Message
trait InputMessage extends Message
// ??
// { "type" : "settings", "name" : "Balazs Balazs" , ... }
case class UserSettings(name : Option[String] = None,
                        radius : Option[Double] = None,
                        placeCacheSize : Option[Int] = None) extends InputMessage
// location => loc
// example: { "type" : "loc", "lng" : 1.1, "lat" : 1.1 }
case class Location(coord : Coordinate) extends InputMessage
// example: { "type" : "map", "coord" : { "lng" : 1.1, "lat": 1.1 }, "radius" : 0.01 }
case class MapInfo(coord: Coordinate, radius: Double) extends InputMessage
// disc
// { "type" : "disc", "id" : "placeId", "payload" : "place secret" }
case class Discovery(id : String, payload : String) extends InputMessage
// bump
// { "type" : "bump", ... }
case class Bump(id : String, myPayload : String, herPayload : String) extends InputMessage
// get user info
// { "type" : "getUser", "id" : "blahblah" }
case class GetUserInfo(id : String) extends InputMessage

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