/*
 *  Moresby Coffee Bean
 *
 * Copyright (c) 2013, Barnabas Sudy (barnabas.sudy@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package actors

import akka.actor.{ActorRef, Actor}
import events._
import scala.math._
import actors.dal.Place
import common._


class UserActor(val id : String, val dalActor : ActorRef) extends Actor {

  println(self.path)

  var name : String = "testUser"
  var lastLocation : Option[Coordinate] = None
  var websocketActor : Option[ActorRef] = None
  var radius : Double = 1.0

  def receive = {
    case inputMessage : InputMessage =>
      processInputEvent(inputMessage)
      //TODO handle serviceEvents rather than RegisterActor only
    case RegisterActor(actor) =>
      websocketActor = Some(actor)
      println("Web socket actor is registered")
    case Disconnect =>
      websocketActor = None
      lastLocation.foreach { ll =>
        context.system.eventStream.publish(new DisconnectEvent(id, ll))
      }
      //TODO start a timer to kill the actor if no request in 5 mins.
    case msg : UserLocationEvent =>
      ifInRange(msg.id, msg.coord, () => {
        sendMessage(new UserLocation(msg.id, Some(msg.coord)))
      })
    case msg : DisconnectEvent =>
      ifInRange(msg.id, msg.lastCoord, () => {
        sendMessage(new UserLocation(msg.id, disc = Some(true)))
      })
    case place : Place =>
      println("Arrived place:" + place)
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
      case None => println(s"Message lost $msg") //TODO append the message to a queue
    }
  }

  private def processInputEvent(event : InputMessage) {
    event match {
      case location : Location =>
        println("Location arrived")
        lastLocation = Some(location.coord)
        val userLocation = new UserLocationEvent(id, location.coord)
        context.system.eventStream.publish(userLocation)
        dalActor ! userLocation
      case _ => println("Unprocessed something")
        sender ! ErrorMessage(s"Unexpected event: $event")
        //TODO handle all the possibilities
    }
  }
}