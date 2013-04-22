package actors

import akka.actor.Actor
import akka.actor.ActorSystem
import events._
import scala.collection.mutable.{HashMap, SynchronizedMap}
import akka.actor.Props
import akka.actor.ActorRef
import actors.dal.Place

class UserHandler(val actorSystem : ActorSystem, val dalActor : ActorRef) {

  val userActors = new HashMap[String, ActorRef] with SynchronizedMap[String, ActorRef]


  def getUser(id : String, token : String) : Option[ActorRef] = {
    
    return Some(userActors.getOrElse(id, {
      val actor = actorSystem.actorOf(Props(new UserActor(id, dalActor)))
      actorSystem.eventStream.subscribe(actor, classOf[UserEvent])
      actorSystem.eventStream.subscribe(actor, classOf[Place])
      actor
    })
    )
	  
  }
  
  private def checkFacebook(id : String, token : String) : Boolean = {
    return true;
  }
  
}