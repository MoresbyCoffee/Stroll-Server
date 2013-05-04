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

import common.Coordinate

/** Actor messages used to access [[actors.dal.DataAccessActor]] and receive
  * result from it.
  */
sealed trait DataAccessMessage

/** Trait for the Data Request Messages used for requesting data. */
trait DataAccessRequestMessage extends DataAccessMessage
/** Trait for Data Response Message used by [[actors.dal.DataAccessActor]]
  * to send back result to the requester.
  */
trait DataAccessResponseMessage extends DataAccessMessage

case class PlaceRequest(loc:Coordinate, distance:Double) extends DataAccessRequestMessage
case class PlaceDetails(id:String, name:String, loc:Coordinate) extends DataAccessResponseMessage



