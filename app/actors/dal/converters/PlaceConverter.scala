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