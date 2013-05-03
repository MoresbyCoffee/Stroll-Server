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

import akka.actor.Actor
import events.UserLocationEvent
import reactivemongo.api._
import reactivemongo.bson._
import play.api.libs.iteratee.Iteratee
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.core.commands.SuccessfulAuthentication
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit._
import reactivemongo.core.actors.Authenticate
import play.api.Play.current
import play.api.Play
import scala.collection.JavaConversions._
import com.typesafe.config.ConfigFactory

class DataAccessActor extends Actor {

  implicit val duration = Duration.apply(30, SECONDS)

  val config = ConfigFactory.load()
  
  var url = config.getStringList("mongodb.url")
  var database = config.getString("mongodb.database")
  var username = config.getString("mongodb.username")
  var password = config.getString("mongodb.password")

  println("Conf: " + url)

  val driver = new MongoDriver
  val connection = driver.connection( url )

  if (username != "" && password != "") {
    connection.authenticate(database, username, password).onSuccess {
      case auth => println("authenticated")
    }
  }

  val db = connection(database)
  val collection = db("places")

  def receive = {
    case PlaceRequest(coord, radius) =>
      println(s"Requesting places for $coord with $radius radius")

    case UserLocationEvent(_, coord) =>
      
      import actors.dal.converters.PlaceConverter._ 
      
      println("database request sender:" + sender)

      val from = sender.path

      val cursor = collection.find(BSONDocument()).cursor[Place]

      cursor.enumerate.apply(Iteratee.foreach { place =>
        println("%%%%%%%%%%%%%%%55 found document: " + place + " sender:" + from)
        context.actorFor(from) ! place
      })

  }
  
  override def postStop() {
    println("Closing DAL actor")
    connection.close()
    driver.close()
    
  }

}
