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

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee._
import scala.concurrent._
import akka.actor._
import play.api.libs.json._
import actors.UserHandler
import events._

import play.api.libs.functional.syntax._
import actors.dal.DataAccessActor
import scala.util.Random
import scala.collection.mutable.{SynchronizedMap, HashMap}
import org.apache.commons.codec.binary.Base64.decodeBase64
import actors.websocket._


object WebSocketController extends Controller {

  val system = ActorSystem("WebSocketSystem")
  val dalActor = system.actorOf(Props[DataAccessActor])

//  system.eventStream.subscribe(dbActor, classOf[UserLocationEvent])

  private val userManager = new UserHandler(system, dalActor)

  def getSessionToken(request: RequestHeader): Option[String] = {
    request.headers.get("Authorization").flatMap { authorization =>
      authorization.split(" ").drop(1).headOption.flatMap { encoded =>
        new String(decodeBase64(encoded.getBytes)).split(":").toList match {
          case c :: s :: Nil => Some(c)
          case _ => None
        }
      }
    }
  }



  val websocketActors = new HashMap[String, Tuple2[String, ActorRef]] with SynchronizedMap[String, Tuple2[String, ActorRef]]

  def auth = Action { request =>
      val json: Option[JsValue] = request.body.asJson
      val id    : Option[String] = if (json.isDefined) { Some((json.get \ "id").as[String]) } else { None }
      val token : Option[String] = if (json.isDefined) { Some((json.get \ "token").as[String]) } else { None }

      val userActor = if (id.isDefined && token.isDefined) {
        userManager.getUser(id.get, token.get)
      } else {
        None
      }

      if (userActor.isDefined) {

        //TODO make it safe
        val sessionId    = Random.nextLong.toString
        val sessionToken = Random.nextLong.toString


        websocketActors += sessionId -> (sessionToken, system.actorOf(Props(new WebSocketActor(userActor.get, sessionId, sessionToken))))
        Ok(Json.toJson(
          Map(
            "id" -> Json.toJson(sessionId),
            "token" -> Json.toJson(sessionToken)
          )
        ))
      } else {
        Forbidden("Please log in first")
      }

  }


  def sync(sessionId : String, sessionToken : String) = WebSocket.using[JsValue] { request =>

    val sessionToken2 : Option[String] = if (!sessionToken.isEmpty) { Some(sessionToken) } else {
      getSessionToken(request)
    }
    val webSocketActorCandidate = websocketActors.get(sessionId)
    println(s"SessoinId: $sessionId SessionToken: $sessionToken SessionToken2: $sessionToken2 ActorCand: $webSocketActorCandidate")
    if (webSocketActorCandidate.isEmpty || sessionToken2.isEmpty || webSocketActorCandidate.get._1 != sessionToken2.get) {
      throw new IllegalArgumentException
    }

    val socketActor = webSocketActorCandidate.get._2


  	val out = Concurrent.unicast[JsValue](
  	  onStart = {
    	channel => /* Send openChannel message */
    	  socketActor ! AppendChannel(channel)
    	  println("started")
      },
  	  onComplete = {
  	    println("Out channel completed")
  	  },
  	  onError = { (msg, input) =>
        println(s"Error occured: $msg")
        input.map( jsValue => socketActor ! ChannelError(jsValue) )
  	  }
    )

    val in  = Iteratee.foreach[JsValue] { msg =>
      println(s"Message arrived: $msg")
      socketActor ! ClientMessage(msg)
    }.mapDone { _ =>
      println("Disconnect")
      socketActor ! ClientDisconnect
    }

    (in, out)

  }



}