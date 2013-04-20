package actors.dal.converters

import reactivemongo.bson._
import actors.dal.Place
import reactivemongo.bson.BSONDouble
import reactivemongo.bson.BSONString
import actors.dal.Place


import reactivemongo.bson.handlers._

object PlaceReader extends BSONReader[Place] {
  def fromBSON(document: BSONDocument): Place = {
    println(s"found document: ${BSONDocument.pretty(document)}")

    val doc = document.toTraversable
    //      println(s"doc: ${BSONDocument.pretty(doc)} id: ${doc.getAs[BSONObjectID]("_id")} name: ${doc.getAs[BSONString]("name")} ");
    Place(
      doc.getAs[BSONObjectID]("_id").get.stringify,
      doc.getAs[BSONString]("name").get.value,
      doc.getAs[BSONDouble]("long").get.value,
      doc.getAs[BSONDouble]("lat").get.value
    )
  }
}
