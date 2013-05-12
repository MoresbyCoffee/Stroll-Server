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

import akka.testkit.TestActorRef
import events._
import common._
import org.specs2.mutable._
import akka.actor.Props
import akka.actor.Actor
import events.UserLocationEvent
import actors.dal.PlaceRequest


/** Unit test of UserActor. */
class UserActorTest extends Specification {
  
  System.setProperty("MONGODB_URL", "localhost:12345")
  System.setProperty("MONGODB_USERNAME", "")
  System.setProperty("MONGODB_PASSWORD", "")
  System.setProperty("MONGODB_DB", "testDatabase")

  "UserActor" should {
    "should collect neighbouring information when a new session connects" in {
      todo
    }
    "process UserLocation and put a UserLocationEvent onto event bus" in new AkkaTestkitSpecs2Support {
      val eventActor = TestActorRef(new MemoryActor)
      system.eventStream.subscribe(eventActor, classOf[UserLocationEvent])

      val dalActor = system.actorOf(Props(new Actor() { def receive = { case msg => println(s"Message arrived: $msg") } }), "dalActor")

      val userActor = TestActorRef(new UserActor("userId"))
      userActor ! Location(Coordinate(1.1, 1.1))

      UserLocationEvent("userId", Coordinate(1.1, 1.1)) === eventActor.underlyingActor.messageList(0)

    }
    "process UserLocation and does not send a PlaceRequest to the dal actor" in new AkkaTestkitSpecs2Support {

      val dalActor = TestActorRef(new MemoryActor, name = "dalActor")

      val userActor = TestActorRef(new UserActor("userId"))
      userActor ! Location(Coordinate(1.1, 1.1))

      dalActor.underlyingActor.messageList.isEmpty must beTrue

    }

    "process MapInfo and send a PlaceRequest to the dal actor" in new AkkaTestkitSpecs2Support {

      val dalActor = TestActorRef(new MemoryActor, name = "dalActor")

      val userActor = TestActorRef(new UserActor("userId"))
      userActor ! MapInfo(Coordinate(1.21, 1.31), 4.23)

      PlaceRequest(Coordinate(1.21, 1.31), 4.23) === dalActor.underlyingActor.messageList(0)

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