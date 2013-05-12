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

import akka.actor.ActorSystem
import events._
import akka.actor.Props
import akka.actor.ActorRef
import com.typesafe.config.ConfigFactory
import org.moresbycoffee.facebook.Facebook._
import actors.dal.Place
import scala.Some
import org.codehaus.jackson.map.ser.PropertyBuilder.EmptyArrayChecker

class UserHandler(val actorSystem : ActorSystem) {

  /* Loading configuration */
  val config = ConfigFactory.load()

  val APP_ID = config.getString("facebook.app_id")
  val APP_SECRET = config.getString("facebook.app_secret")

  implicit val appConfig = FBAppConfig(APP_ID, APP_SECRET)
  implicit val accessToken = getAccessToken


  def getUser(id : String, token : String) : Option[ActorRef] = {
    if (checkUser(id, token)) {
      val actor = actorSystem.actorOf(Props(new UserActor(id)))
      actorSystem.eventStream.subscribe(actor, classOf[UserEvent])
      actorSystem.eventStream.subscribe(actor, classOf[Place])
      Some(actor)
    } else {
      None
    }
  }
  
  private def checkFacebook(id : String, token : String) : Boolean = {
    return checkUser(id, token);
  }
  
}