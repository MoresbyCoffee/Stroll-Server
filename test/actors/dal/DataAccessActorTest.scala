/*
 *  Moresby Coffee Bean
 *
 * Copyright (c) 20132013, Barnabas Sudy (barnabas.sudy@gmail.com)
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