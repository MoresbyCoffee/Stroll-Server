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



