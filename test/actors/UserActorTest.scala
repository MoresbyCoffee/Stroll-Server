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
import org.junit.runner.RunWith
import akka.actor.Props
import akka.actor.Actor
import events.UserLocationEvent
import org.specs2.runner.JUnitRunner



/** Unit test of UserActor. */
@RunWith(classOf[JUnitRunner])
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