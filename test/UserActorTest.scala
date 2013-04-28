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
import akka.testkit.TestActorRef
import actors.UserActor
import akka.testkit.TestActor
import akka.actor.ActorSystem
import akka.testkit.TestKit
import events._
import org.specs2.mutable._
import org.specs2.mutable.SpecificationWithJUnit
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import akka.testkit.ImplicitSender
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import akka.testkit.EventFilter
import akka.actor.Actor
import scala.collection.immutable.List
import scala.collection.mutable.LinkedList



/** Unit test of UserActor. */
@RunWith(classOf[JUnitRunner])
class UserActorTest extends Specification {

  "UserActor" should {
    "should collect neighbouring information when a new session connects" in {
      todo
    }
    "process UserLocation and put a UserLocationEvent onto event bus" in new AkkaTestkitSpecs2Support {
      val eventActor = TestActorRef(new MemoryActor)
      system.eventStream.subscribe(eventActor, classOf[UserLocationEvent])

      val dalActor = system.actorOf(Props(new Actor() { def receive = { case msg => println(s"Message arrived: $msg") } }))

      val userActor = TestActorRef(new UserActor("userId", dalActor))
      userActor ! Location(Coordinate(1.1, 1.1))

      UserLocationEvent("userId", Coordinate(1.1, 1.1)) === eventActor.underlyingActor.messageList(0)

    }
    "process UserLocation and send a UserLocationEvent to the dal actor" in new AkkaTestkitSpecs2Support {
      system.eventStream.subscribe(system.actorOf(Props(new Actor() { def receive = { case msg => println(s"Message arrived: $msg") } })), classOf[UserLocationEvent])

      val dalActor = TestActorRef(new MemoryActor)

      val userActor = TestActorRef(new UserActor("userId", dalActor))
      userActor ! Location(Coordinate(1.1, 1.1))

      UserLocationEvent("userId", Coordinate(1.1, 1.1)) === dalActor.underlyingActor.messageList(0)

    }
    "process Place and send PlaceLocatoin to websocket" in {
      todo
    }
    "process Place and throw away if it is not in range" in {
      todo
    }
    "process Place and queue it up if the websocket is not available" in {
      //TODO do we want to queue messages in this level?
      todo
    }
  }

}