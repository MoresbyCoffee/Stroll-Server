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
package actors.dal.converters

import reactivemongo.bson._
import actors.dal.Place
import reactivemongo.bson.BSONDouble
import reactivemongo.bson.BSONString
import actors.dal.Place
import common.Coordinate


object PlaceConverter {
  
	implicit object PlaceReader extends BSONDocumentReader[Place] {
	  def read(doc: BSONDocument): Place = {
	    Place(
	      doc.getAs[BSONObjectID]("_id").get.stringify,
	      doc.getAs[BSONString]("name").get.value,
	      Coordinate(
	        doc.getAs[BSONDocument]("loc").get.getAs[Double]("lng").get,
	        doc.getAs[BSONDocument]("loc").get.getAs[Double]("lat").get
	      )
	    )
	  }
	}
	
	implicit object PlaceWriter extends BSONDocumentWriter[Place] {
	  def write(place : Place) : BSONDocument = {
	    val bson = BSONDocument("_id" -> BSONObjectID(place.id),
	    						"name" -> BSONString(place.name),
	    						"loc" -> BSONDocument(
	    							"lng" -> BSONDouble(place.loc.lng),
	    							"lat" -> BSONDouble(place.loc.lat)
	    						))
	    bson
	  }
	}
}