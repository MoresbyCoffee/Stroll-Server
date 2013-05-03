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
package controllers

import play.api.libs.iteratee.Concurrent.Channel
import akka.actor._
import play.api.libs.json._
import actors.UserHandler
import events._
import common.Coordinate
import common.Coordinate
import controllers.ClientDisconnect
import events.ErrorMessage
import controllers.ClientMessage
import events.Location
import events.PlaceLocation
import controllers.AppendChannel
import events.UserLocation
import scala.Some
import events.Disconnect
import controllers.ChannelError

/**
 * Author: Barnabas Sudy
 */
class WebSocketActor(val userActor : ActorRef, val sessionId : String, val sessionToken : String) extends Actor {

  var channel : Option[Channel[JsValue]] = None;

  def receive = {
    case wse : WebSocketEvent => processWebSocketEvent(wse)
    case out : OutputMessage =>
      channel match {
        case Some(ch) =>
          ch.push(toMessage(out))
        case None =>
          println("Lost message: ${out}") //TODO build queue and unregister actor (?) - carefully, because it's can be dangerous
      }

    //TODO case outputmessage: convert to Json
    case a => println(s"unprocessed event ${a}")
  }

  private def processWebSocketEvent(webSocketEvent : WebSocketEvent) = {
    webSocketEvent match {
      case AppendChannel(ch) =>
        channel = Some(ch)  //TODO run everything from the queue
        userActor ! RegisterActor(self)
      case cd : ClientDisconnect =>
        userActor ! Disconnect
        context.stop(self)
      case ClientMessage(jsValue) =>
        println("JavaScript message arrived")
        channel match {
          /* No channel assigned yes */
          case None => println("The channel is not ready yet") //TODO queue the messages
          /* Channel has been assigned */
          case Some(ch) =>
            /* process messages */
            val parsedMessage = parseMessage(jsValue)
            println(s"ParsedMessage: ${parsedMessage}")
            processInputMessage(parsedMessage)
        }
      case ChannelError(jsValue) =>
        //TODO put back to the queue.
        println(s"Channel error: Channel is set to None")
        channel = None;
        //TODO start timer and close this in 3 mins if there is no new message from the client.
    }

  }

  private def processInputMessage(input : InputMessage) = {
    userActor ! input
  }

  implicit val coordinateRead = Json.reads[Coordinate]
  implicit val locationRead = Json.reads[Location]
  implicit val coordinateWrite = Json.writes[Coordinate]
  implicit val userLocationWrite  = Json.writes[UserLocation]
  implicit val placeLocationWrite = Json.writes[PlaceLocation]
  implicit val errorMessageWrite  = Json.writes[ErrorMessage]

  private def toMessage(msg : OutputMessage) : JsValue = {
    msg match {
      case userLoc : UserLocation => Json.toJson(userLoc)
      case placeLoc : PlaceLocation => Json.toJson(placeLoc)
      case error : ErrorMessage => Json.toJson(error)
    }
  }

  def parseMessage(jsValue : JsValue) : InputMessage = {
    (jsValue \ "type").as[String] match {
      case "loc" => jsValue.as[Location]
      case typeString => println(s"Type is not supported: ${typeString}"); throw new IllegalArgumentException
    }
  }

}
