/*
 *  Moresby Coffee Bean
 *
 * Copyright (c) 2013, Barnabas Sudy (barnabas.sudy@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package actors

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import events._
import org.specs2.mutable._
import scala.collection.immutable.List
import com.typesafe.config.ConfigFactory

/** 
 * A tiny class that can be used as a Specs2 'context'.
 * From: [[http://blog.xebia.com/2012/10/01/testing-akka-with-specs2/]]
 */
abstract class AkkaTestkitSpecs2Support extends TestKit({
  val config = ConfigFactory.empty()
  ActorSystem("testsystem", config)
})
  with After
  with ImplicitSender {
  // make sure we shut down the actor system after all tests have run
  def after = system.shutdown()
}

/**
 * Special [[Actor]] what records all the incoming messages in the messageList. 
 */
class MemoryActor extends Actor {
  var messageList: List[Any] = Nil;

  def receive = {
    case message => messageList = message :: messageList
  }
}
