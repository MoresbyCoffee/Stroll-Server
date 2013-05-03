package actors.dal

import reactivemongo.bson.BSONObjectID
import common.Coordinate

case class Place(id : String, name : String, loc : Coordinate)

