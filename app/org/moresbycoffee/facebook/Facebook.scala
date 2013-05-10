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
 * along with Moresby Stroll Server.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.moresbycoffee.facebook

import play.api.libs.json.Json
import scalaj.http.{HttpOptions, Http}

/**
 * This object provides facebook Graph API functionality to the application.
 */
object Facebook {

  implicit val facebookUserRead = Json.reads[FacebookUser]

  case class FBAppConfig(appId : String, appSecret : String)
  case class AccessToken(appId : String, token : String)
  case class FacebookUser(id : String, access_token: String, login_url : String)

  def getPredefinedTestUser(implicit accessToken : AccessToken) : Option[FacebookUser] = {
    val predefinedUsersResult = Http.get(s"https://graph.facebook.com/${accessToken.appId}/accounts/test-users").params(
      "access_token" -> accessToken.token)
      .options(HttpOptions.connTimeout(30000), HttpOptions.readTimeout(30000)).asString
    val predefinedUsersJson = Json.parse(predefinedUsersResult)
    val predefinedUsers = (predefinedUsersJson \ "data" ).as[List[FacebookUser]]
    if (predefinedUsers.isEmpty)
      None
    else
      Some(predefinedUsers.head)
  }

  def getTestUser(implicit accessToken : AccessToken) : Option[FacebookUser] = {
    getPredefinedTestUser.orElse {
      val facebookUserResult = Http.get(s"https://graph.facebook.com/${accessToken.appId}/accounts/test-users").params(
        "installed" -> "true",
        "name" -> "Test user name",
        "locale" -> "en_US",
        "permissions" -> "read_stream",
        "method" -> "post",
        "access_token" -> accessToken.token)
        .options(HttpOptions.connTimeout(30000), HttpOptions.readTimeout(30000)).asString
      val facebookUsersJson = Json.parse(facebookUserResult)

      println(s"^^^^^^^^^^^^^^^^^^^ Facebook: $facebookUserResult")

      facebookUsersJson.asOpt[FacebookUser]
    }

  }

  def getAccessToken(implicit appConfig : FBAppConfig) : AccessToken = {
    val accessTokenUrl = s"https://graph.facebook.com/oauth/access_token"

    println(s"^^^^^^^^^^^^^^^^^^^ Facebook Access Token: $accessTokenUrl")
    val facebookAccessTokenResult = Http.get(accessTokenUrl)
      .options(HttpOptions.connTimeout(10000), HttpOptions.readTimeout(10000))
      .params("client_id" -> appConfig.appId, "client_secret" -> appConfig.appSecret, "grant_type" -> "client_credentials").asString

    println(s"^^^^^^^^^^^^^^^^^^^ Facebook Access Token: $facebookAccessTokenResult")

    val accessToken = facebookAccessTokenResult.split("=")(1)

    AccessToken(appConfig.appId, accessToken)
  }

  def checkUser(userId : String, userToken : String)(implicit accessToken : AccessToken) : Boolean = {
    val result = Http.get("https://graph.facebook.com/debug_token").params(
      "input_token" -> userToken,
      "access_token" -> accessToken.token)
      .options(HttpOptions.connTimeout(30000), HttpOptions.readTimeout(30000)).asString
    val json = Json.parse(result)

    println(s"^^^^^^^^^^^^^^^^^^^ Facebook Check Result: $json")

    {
      val is_valid = (json \ "data" \ "is_valid").as[Boolean]
      is_valid == true
    } && {
      val user_id = (json \ "data" \ "user_id").as[Long]
      user_id.toString == userId
    }

  }

}
