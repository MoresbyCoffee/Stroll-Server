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

/**
 * Created with IntelliJ IDEA.
 * User: envagyok
 * Date: 2013.04.30.
 * Time: 21:52
 * To change this template use File | Settings | File Templates.
 */
sealed trait DataAccessMessage

trait DataAccessRequestMessage extends DataAccessMessage
trait DataAccessResponseMessage extends DataAccessMessage

case class PlaceRequest(loc:Coordinate, distance:Double) extends DataAccessRequestMessage
case class PlaceDetails(id:String, name:String, loc:Coordinate) extends DataAccessResponseMessage



