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
package actors.websocket

import org.specs2.mutable.Specification
import actors.{MemoryActor, AkkaTestkitSpecs2Support}
import akka.testkit.TestActorRef
import events.PlaceLocation
import common.Coordinate
import play.api.libs.json._
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.{Enumerator, Enumeratee, Iteratee, Concurrent}
import scala.concurrent._
import scala.concurrent.duration._

/**
 * Unit tests for [[actors.websocket.WebSocketActor]]
 */
class WebSocketActorTest extends Specification {

  "WebSocketActor" should {
    "forward PlaceLocation messages to the client" in new AkkaTestkitSpecs2Support {

      //creatig actors
      val userActor      = TestActorRef(new MemoryActor)
      val webSocketActor = TestActorRef(new WebSocketActor(userActor, "sessionId", "sessionToken"))

      val channelPromise = Promise[Channel[JsValue]]
      val outputPromise  = Promise[JsValue]
      val enumerator = Concurrent.unicast[JsValue]( onStart = { channelPromise success _ } )
      enumerator(Iteratee.foreach[JsValue] { outputPromise success _ })

      //Assigning channel
      val channel : Channel[JsValue] = Await.result(channelPromise.future, DurationInt(15).seconds)
      webSocketActor ! AppendChannel(channel)


      //Test
      webSocketActor ! PlaceLocation("placeId", "Name of the place", Coordinate(12.33, 45.75))
      val output = Await.result(outputPromise.future, DurationInt(15).seconds)

      //Assertion
      (output \ "name").as[String] === "Name of the place"
      (output \ "type").as[String] === "place"
      (output \ "id").as[String] === "placeId"
      (output \ "loc" \ "lng").as[Double] === 12.33
      (output \ "loc" \ "lat").as[Double] === 45.75

    }
    "forward UserLocation message to the client" in {
      todo
    }
  }


}
