package actors.dal

import akka.actor.Actor
import events.UserLocationEvent

import reactivemongo.api._
import reactivemongo.bson._
import actors.dal.converters.PlaceReader

import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler


import play.api.libs.iteratee.Iteratee
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.core.commands.SuccessfulAuthentication
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit._
import reactivemongo.core.actors.Authenticate
import play.api.Play.current
import play.api.Play
import scala.collection.JavaConversions._

class DataAccessActor extends Actor {

//  val connection = MongoConnection( List( "localhost:27017" ) )
//  val db = connection("facebook")
  implicit val duration = Duration.apply(30, SECONDS)


  var url = Play.configuration.getStringList("mongodb.url").get.toList
  var database = Play.configuration.getString("mongodb.database").get
  var username = Play.configuration.getString("mongodb.username")
  var password = Play.configuration.getString("mongodb.password")

  println("Conf: " + url)

  val connection = MongoConnection.apply( url )

  if (username.isDefined && password.isDefined && username.get != "" && password.get != "") {
    connection.authenticate(database, username.get, password.get).onSuccess {
      case auth => println("authenticated")
    }
  }

  val db = connection(database)
  val collection = db("places")

  def receive = {
    case UserLocationEvent(_, longitude, latitude, _) =>
      implicit val placeReader = PlaceReader
      println("database request sender:" + sender)

      val from = sender.path

      val cursor = collection.find(BSONDocument())

      cursor.enumerate.apply(Iteratee.foreach { place =>
        println("found document: " + place + " sender:" + from)
        context.actorFor(from) ! place
      })

  }

}
