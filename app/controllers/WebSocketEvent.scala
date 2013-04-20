package controllers

import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json.JsValue

sealed trait WebSocketEvent

case class AppendChannel(channel : Channel[JsValue]) extends WebSocketEvent
case class ClientDisconnect() extends WebSocketEvent
case class ClientMessage(jsValue : JsValue) extends WebSocketEvent
