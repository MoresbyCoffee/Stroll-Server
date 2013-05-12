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
package actors.websocket

import play.api.libs.iteratee.Concurrent.Channel
import akka.actor._
import play.api.libs.json._
import events._
import common.Coordinate
import events.RegisterActor
import events.ErrorMessage
import events.Location
import events.MapInfo
import events.UserLocation
import scala.Some
import events.PlaceLocation
import events.Disconnect
import play.api.Logger

/**
 * This actor is responsible to handle the JSON request
 * arrived from the client and to send messages to the client.
 */
class WebSocketActor(val userActor : ActorRef, val sessionId : String, val sessionToken : String) extends Actor {

  var channel   : Option[Channel[JsValue]] = None

  def receive = {
    /* Messages from the WebSocket (from the client) */
    case wse : WebSocketEvent => processWebSocketEvent(wse)
    /* Messages from the Business Logic */
    case out : OutputMessage =>
      channel match {
        case Some(ch) =>
          ch.push(toMessage(out))
        case None =>
          Logger.warn(s"Lost message: $out") //TODO build queue and unregister actor (?) - carefully, because it's can be dangerous
      }

    //TODO case outputmessage: convert to Json
    case a => Logger.warn(s"unprocessed event $a")
  }

  /** Handles the WebSocket events.
    * All the messages arriving from the WebSocket - even the client messages -
    * wrapped into a [[actors.websocket.WebSocketEvent]].
    *
    * @param webSocketEvent The - service or client - message from the WebSocket
    */
  private def processWebSocketEvent(webSocketEvent : WebSocketEvent) {
    webSocketEvent match {
      /* Service Message handler */
      case AppendChannel(sessionTokenIn, ch) =>
        if (sessionToken == sessionTokenIn) {
          channel = Some(ch)
          userActor ! RegisterActor(self)
        } else {
          //TODO send error
          //TODO disconnect.
        }

      case cd : ClientDisconnect =>
        userActor ! Disconnect
        context.stop(self)
      case ChannelError(jsValue) =>
        //TODO put back to the queue.
        Logger.info(s"Channel error: Channel is set to None")
        channel = None
        //TODO start timer and close this in 3 mins if there is no new message from the client.

      /* Client Message handler */
      case ClientMessage(jsValue) =>
        Logger.trace("JavaScript message arrived")
        /* parsing json message */
        val parsedMessage = parseMessage(jsValue)
        Logger.trace(s"ParsedMessage: $parsedMessage")
        /* processing client message */
        processInputMessage(parsedMessage)
    }

  }

  private def processInputMessage(input : InputMessage) {
    userActor ! input
  }

  implicit val coordinateRead     = Json.reads[Coordinate]
  implicit val locationRead       = Json.reads[Location]
  implicit val mapInfoRead        = Json.reads[MapInfo]
  implicit val coordinateWrite    = Json.writes[Coordinate]
  implicit val userLocationWrite  = Json.writes[UserLocation]
  implicit val placeLocationWrite = new Writes[PlaceLocation] {
    def writes(place : PlaceLocation) : JsValue = {
      Json.obj(
        "name" -> place.name,
        "id"   -> place.id,
        "type" -> "place",
        "loc"  -> place.coord
      )
    }

  }
  implicit val errorMessageWrite  = Json.writes[ErrorMessage]

  private def toMessage(msg : OutputMessage) : JsValue = {
    msg match {
      case userLoc : UserLocation =>
        Json.toJson(userLoc)
      case placeLoc : PlaceLocation =>
        Json.toJson(placeLoc)
      case error : ErrorMessage =>
        Json.toJson(error)
    }
  }

  def parseMessage(jsValue : JsValue) : InputMessage = {
    (jsValue \ "type").as[String] match {
      case "loc" => jsValue.as[Location]
      case "map" => jsValue.as[MapInfo]
      case typeString => println(s"Type is not supported: $typeString"); throw new IllegalArgumentException
    }
  }

}
