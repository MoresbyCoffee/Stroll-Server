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


object WebSocketController extends Controller {

  val system = ActorSystem("WebSocketSystem")
  val dalActor = system.actorOf(Props[DataAccessActor])

//  system.eventStream.subscribe(dbActor, classOf[UserLocationEvent])

  private val userManager = new UserHandler(system, dalActor)


  def sync = WebSocket.using[JsValue] { request =>

    val socketActor = system.actorOf(Props(new WebSocketActor(userManager)))

  	val out = Concurrent.unicast[JsValue](
  	  onStart = {
    	channel => /* Send openChannel message */
    	  socketActor ! AppendChannel(channel)
    	  println("started")
      },
  	  onComplete = {
  	    println("Out channel completed")
  	  },
  	  onError = {
  	    (msg, input) => println(s"Error occured: ${msg}")
  	  }
    )

    val in  = Iteratee.foreach[JsValue] { msg =>
      println(s"Message arrived: ${msg}")
      socketActor ! ClientMessage(msg)
    }.mapDone { _ =>
      println("Disconnect")
      socketActor ! ClientDisconnect
    }

    (in, out)

  }



}