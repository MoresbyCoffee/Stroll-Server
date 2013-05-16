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
package actors

import akka.actor.{ActorRef, Actor}
import events._
import scala.math._
import actors.dal.{PlaceRequest, Place}
import common._
import play.api.Logger


class UserActor(val id : String) extends Actor {

  val dalActor : ActorRef = context.actorFor("../dalActor")
  println("Found dalActor:" + dalActor.getClass)

  //TODO send "initUser" to dalActor.

  var name : String = "testUser"
  var lastLocation : Option[Coordinate] = None
  var websocketActor : Option[ActorRef] = None
  var radius : Double = 1.0

  def receive = {
    case inputMessage : InputMessage =>
      Logger.debug("Receive inputMessage: " + inputMessage)
      processInputEvent(inputMessage)
      //TODO handle serviceEvents rather than RegisterActor only
    case RegisterActor(actor) =>
      websocketActor = Some(actor)
      Logger.debug("Web socket actor is registered")
    case Disconnect =>
      websocketActor = None
      lastLocation.foreach { ll =>
        context.system.eventStream.publish(new DisconnectEvent(id, ll))
      }
      context.stop(self)
    case msg : UserLocationEvent =>
      ifInRange(msg.id, msg.coord, () => {
        sendMessage(new UserLocation(msg.id, Some(msg.coord)))
      })
    case msg : DisconnectEvent =>
      ifInRange(msg.id, msg.lastCoord, () => {
        sendMessage(new UserLocation(msg.id, disc = Some(true)))
      })
    case place : Place =>
      Logger.debug("Arrived place:" + place)
      sendMessage(new PlaceLocation(place.id, place.name, place.loc))
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
      case None => Logger.warn(s"Message lost $msg") //TODO append the message to a queue
    }
  }

  private def processInputEvent(event : InputMessage) {
    event match {
      case location : Location =>
        lastLocation = Some(location.coord)
        val userLocation = new UserLocationEvent(id, location.coord)
        //val placeRequest = new PlaceRequest(location.coord, radius)
        context.system.eventStream.publish(userLocation)
      case mapInfo : MapInfo =>
        val placeRequest = new PlaceRequest(mapInfo.coord, mapInfo.radius)
        dalActor ! placeRequest
      case _ => Logger.warn("Unprocessed something")
        sender ! ErrorMessage(s"Unexpected event: $event")
        //TODO handle all the possibilities
    }
  }
}