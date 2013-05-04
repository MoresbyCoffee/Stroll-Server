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

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import events._
import org.specs2.mutable._
import scala.collection.immutable.List
import com.typesafe.config.ConfigFactory

/** 
 * A tiny class that can be used as a Specs2 'context'.
 * From: [[http://blog.xebia.com/2012/10/01/testing-akka-with-specs2/]]
 */
abstract class AkkaTestkitSpecs2Support extends TestKit({
  val config = ConfigFactory.load()
  ActorSystem("testsystem", config)
})
  with After
  with ImplicitSender {
  // make sure we shut down the actor system after all tests have run
  def after = {
		  println("++++++++ Shuting down actorSystem")
		  system.shutdown()
    
  }
    
  
}

//import de.flapdoodle.embed.mongo._
//import config.MongodConfig
//import distribution.Version
//import org.specs2.specification.BeforeAfterExample
//
//trait EmbedConnection extends BeforeAfterExample {
//
//  //Override this method to personalize testing port
//  def embedConnectionPort(): Int = { 12345 }
//
//  //Override this method to personalize MongoDB version
//  def embedMongoDBVersion(): Version = { Version.V2_2_1 }
//
//  lazy val runtime: MongodStarter = MongodStarter.getDefaultInstance
//  lazy val mongodExe: MongodExecutable = runtime.prepare(new MongodConfig(embedMongoDBVersion(), embedConnectionPort(), true))
//  lazy val mongod: MongodProcess = mongodExe.start()
//
//  def before() {
//    mongod
//  }
//
//  def after() {
//    mongod.stop()
//    mongodExe.cleanup()
//  }
//}

/**
 * Special [[akka.actor.Actor]] what records all the incoming messages in the messageList.
 */
class MemoryActor extends Actor {
  var messageList: List[Any] = Nil;

  def receive = {
    case message => messageList = message :: messageList
  }
}

