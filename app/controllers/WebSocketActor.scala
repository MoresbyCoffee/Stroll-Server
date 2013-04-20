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
          val parsedMessages = parseMessage(jsValue)
          println(s"ParsedMessage: ${parsedMessages}")
          val partitionedByAuth = parsedMessages.partition(classOf[Auth].isInstance(_))
          partitionedByAuth._1.foreach(processInputMessage(_))
          partitionedByAuth._2.foreach(processInputMessage(_))
          //ch.push(jsValue)

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
      case Auth(id, token, name) =>
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
  implicit val locationRead = Json.reads[Location]
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

  def parseMessage(jsValue : JsValue) : List[InputMessage] = {

    val auth = (jsValue \ "auth").asOpt[Auth]
    val location = (jsValue \ "location").asOpt[Location]
    var result : List[InputMessage] = Nil; //List[InputMessage]();
    auth.foreach{ a => result = a :: result }
    location.foreach{ a => result = a :: result }
    return result;
  }

}
