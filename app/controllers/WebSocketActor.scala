package controllers

import play.api.libs.iteratee.Concurrent.Channel
import akka.actor._
import play.api.libs.json._
import actors.UserHandler
import events._

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
      case AppendChannel(ch) => channel = Some(ch)  //TODO run everything from the queue
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
