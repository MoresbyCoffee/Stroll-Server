package actors.dal

import events.Coordinate
import reactivemongo.bson.BSONObjectID

case class Place(id : String, name : String, loc : Coordinate)

