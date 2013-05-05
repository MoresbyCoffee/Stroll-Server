/*
* Moresby Coffee Bean
*
* Copyright (c) 2013, Barnabas Sudy (barnabas.sudy@gmail.com)
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the <organization> nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
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
package actors.dal

import org.specs2.mutable.Specification
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
import reactivemongo.api.indexes._
import reactivemongo.api.indexes.IndexType.Geo2D

/**
 * Unit tests for [[actors.dal.DataAccessActor]].
 */
class DataAccessActorTest extends Specification with EmbedConnection {
  
  System.setProperty("MONGODB_URL", "localhost:12345")
  System.setProperty("MONGODB_USERNAME", "")
  System.setProperty("MONGODB_PASSWORD", "")
  System.setProperty("MONGODB_DB", "testDatabase")
    
  
  "DataAccessActor" should {
    "find places if a user actor requests it" in new AkkaTestkitSpecs2Support {
      import actors.dal.converters.PlaceConverter._

      /* Preparation */
      val driver     = new MongoDriver
      val connection = driver.connection("localhost:12345" :: Nil)
      val db         = connection("testDatabase")
      val collection = db("places")
      
      val insertResult = collection.insert(Place(BSONObjectID.generate.stringify, "name", Coordinate(1.1, 1.1)))
      Await.result(insertResult, scala.concurrent.duration.DurationInt(15).seconds)

      val idx : Index = Index( ("loc", Geo2D) :: Nil)
      Await.result(collection.indexesManager.ensure(idx) , scala.concurrent.duration.DurationInt(15).seconds)

      /* Creating subject */
      val dalActor = system.actorOf(Props[DataAccessActor])

      /* Test */
      dalActor ! PlaceRequest(Coordinate(1.1, 1.1), 1.0)

      /* Assertion */
      val result = expectMsgClass(scala.concurrent.duration.DurationInt(30).seconds, classOf[Place])
      result.loc === Coordinate(1.1, 1.1)
      result.name === "name"

      /* Closing connection */
      println("closing initial connection")
      driver.close()
      connection.close()
    }
  }

}