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
package actors.dal

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import actors.AkkaTestkitSpecs2Support
import com.github.athieriot.EmbedConnection
import scala.concurrent.ExecutionContext.Implicits.global
import common.Coordinate
import reactivemongo.api._
import reactivemongo.bson._
import play.api.Play.current
import play.api.Play
import scala.collection.JavaConversions._
import akka.actor.Props
import events._
import scala.concurrent._
import scala.concurrent.duration._

/**
 * Unit tests for [[actors.dal.DataAccessActor]].
 */
@RunWith(classOf[JUnitRunner])
class DataAccessActorTest extends Specification with EmbedConnection {
  
  System.setProperty("MONGODB_URL", "localhost:12345")
  System.setProperty("MONGODB_USERNAME", "")
  System.setProperty("MONGODB_PASSWORD", "")
  System.setProperty("MONGODB_DB", "testDatabase")
    
  
  "DataAccessActor" should {
    "find places if a user actor requests it" in new AkkaTestkitSpecs2Support {
      import actors.dal.converters.PlaceConverter._
      
      val driver = new MongoDriver
      val connection = driver.connection("localhost:12345" :: Nil) 
      val db = connection("testDatabase")
      val collection = db("places")
      
      val result = collection.insert(Place(BSONObjectID.generate.stringify, "name", Coordinate(1.1, 1.1)))
      val res = Await.result(result, scala.concurrent.duration.DurationInt(15).seconds)
      println("~~~~~~~~~~~~~~~~~~~~~~~~~~~insert result: " + res)
      
//      val cursor = collection.find(BSONDocument()).cursor[Place]
//      val test = Await.result(cursor.toList, scala.concurrent.duration.DurationInt(15).seconds)
//      println("~~~~~~~~~~~~~~~~~~~~~~~~~~~test query test: " + test)

      
      val dalActor = system.actorOf(Props[DataAccessActor])
      
      dalActor ! UserLocationEvent("userId", Coordinate(1.1, 1.1))
      
      println("------------- expectMsg")
      expectMsgClass(scala.concurrent.duration.DurationInt(30).seconds, classOf[Place]) //(PlaceLocation("12", "name", Coordinate(1.1, 1.1)))
      println("+++++++++++++++++++++++ end expect")
      
      println("closing initial connection")
      driver.close()
      connection.close()
    }
  }

}