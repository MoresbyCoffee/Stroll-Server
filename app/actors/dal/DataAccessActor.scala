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

import akka.actor.{ActorPath, Actor}
import reactivemongo.api._
import reactivemongo.bson._
import play.api.libs.iteratee.Iteratee
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit._
import play.api.Play.current
import play.api.Play
import scala.collection.JavaConversions._
import com.typesafe.config.ConfigFactory

/**
 * Responsible to serve the data requests. This actor uses ReactiveMongo
 * to gain to MongoDB. It is enough to use only one DataAccessActor because
 * ReactiveMongo handles the requests asynchronously.
 */
class DataAccessActor extends Actor {

  implicit val duration = Duration.apply(30, SECONDS)

  /* Loading configuration */
  val config = ConfigFactory.load()
  
  var url      = config.getStringList("mongodb.url")
  var database = config.getString("mongodb.database")
  var username = config.getString("mongodb.username")
  var password = config.getString("mongodb.password")

  /* Database connection */
  val driver = new MongoDriver
  val connection = driver.connection( url )

  /* Authentication */
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

      import actors.dal.converters.PlaceConverter._

      val from = sender.path

      val x1 : Double = coord.lng - radius
      val y1 : Double = coord.lat - radius
      val x2 : Double = coord.lng + radius
      val y2 : Double = coord.lat + radius

      val cursor = collection.find(BSONDocument( "loc" -> BSONDocument(
        "$within" -> BSONDocument(      //GeoWithin doesn't work in tests.
          "$box" ->  BSONArray( BSONArray( x1, y1), BSONArray( x2, y2 ))
        )
      ))).cursor[Place]

      cursor.enumerate.apply(Iteratee.foreach(responseHandler(from)))

  }

  private[this] def responseHandler(fromPath : ActorPath) : (Place) => Unit = { place : Place => context.actorFor(fromPath) ! place }
  
  override def postStop() {
    println("Closing DAL actor")
    connection.close()
    driver.close()
    
  }

}
