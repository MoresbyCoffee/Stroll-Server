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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package actors.websocket

import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json.JsValue

/**
 * Actor messages for the WebSocket to indicate some events to [[actors.websocket.WebSocketActor]].
 */
sealed trait WebSocketEvent

/** Service message - a channel is ready to use. */
case class AppendChannel(channel : Channel[JsValue]) extends WebSocketEvent
/** Service message - the client has disconnected, do not send messages. */
case class ClientDisconnect() extends WebSocketEvent
/** Service message - Error happend in the channel, do not send messages. */
case class ChannelError(jsValue: JsValue) extends WebSocketEvent
/** Wrapper for messages from the client. */
case class ClientMessage(jsValue : JsValue) extends WebSocketEvent

