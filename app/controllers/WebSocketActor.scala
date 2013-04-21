package controllers

import play.api.libs.iteratee.Concurrent.Channel
import akka.actor._
import play.api.libs.json._
import actors.UserHandler
import events._

/**
 * Author: Barnabas Sudy
 */
class WebSocketActor(val userManager : UserHandler) extends Actor {

  var channel : Option[Channel[JsValue]] = None;
  var userActor : Option[ActorRef] = None;

  def receive = {
    case AppendChannel(ch) => channel = Some(ch)  //TODO run everything from the queue
    case ClientDisconnect =>
      userActor.foreach { _ ! Disconnect }

      //TODO set a disconnected state, what must be tested before doing anything.
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
    case out:OutputMessage =>
      channel match {
        case Some(ch) =>
          ch.push(toMessage(out))
        case None =>
          println("Lost message: ${out}") //TODO build queue and unregister actor (?) - carefully, because it's can be dangerous
      }

    //TODO case outputmessage: convert to Json
    case a => println(s"unprocessed event ${a}")
  }

  private def processInputMessage(input : InputMessage) = {
    input match {
      /* Auth message */
      case Auth(id, token) =>
        val actor = userManager.getUser(id, token)
        userActor = Some(actor)
        actor ! RegisterActor(self)
      /* Everything else */
      case _ => {
        userActor match {
          case None => println("Not userActor, please login in first") //TODO send error message
          case Some(actorRef) => actorRef ! input
        }

      }
    }
  }

  implicit val authRead = Json.reads[Auth]
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
      case "auth" => jsValue.as[Auth]
      case "loc" => jsValue.as[Location]
      case typeString => println(s"Type is not supported: ${typeString}"); throw new IllegalArgumentException
    }
  }

}
