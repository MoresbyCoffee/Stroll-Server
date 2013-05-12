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
package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import com.typesafe.config.ConfigFactory
import scalaj.http._
import play.api.libs.json._
import org.moresbycoffee.facebook.Facebook._

/** Tests the authentication. */
class AuthSpec extends Specification {

  System.setProperty("MONGODB_URL", "localhost:12345")
  System.setProperty("MONGODB_USERNAME", "")
  System.setProperty("MONGODB_PASSWORD", "")
  System.setProperty("MONGODB_DB", "testDatabase")


  /* Loading configuration */
  val config = ConfigFactory.load()

  val APP_ID = config.getString("facebook.app_id")
  val APP_SECRET = config.getString("facebook.app_secret")

  implicit val appConfig = FBAppConfig(APP_ID, APP_SECRET)
  implicit val accessToken = getAccessToken

  "Application" should {

    "should authenticate" in {
      running(TestServer(3333)) {

        val facebookUser = getPredefinedTestUser.get


        val result = Http.postData("http://localhost:3333/auth", s"""{"id":"${facebookUser.id}","token":"${facebookUser.access_token}"}""")
          .header("Content-Type", "application/json")
          .header("Charset", "UTF-8")
          .option(HttpOptions.readTimeout(10000))
          .asString


        val jsonResult: JsValue = Json.parse(result)

        val sessionId    = (jsonResult \ "id").asOpt[String]
        val sessionToken = (jsonResult \ "token").asOpt[String]

        println("^^^^^^^^^^^^^^^^^^^ " + result);

        sessionId.isDefined must beTrue
        sessionToken.isDefined must beTrue

      }
    }

    "should fail if the id and token not valid" in {
      running(TestServer(3333)) {
        val result = Http.postData("http://localhost:3333/auth", """{"id":"12","token":"data"}""")
          .header("Content-Type", "application/json")
          .header("Charset", "UTF-8")
          .option(HttpOptions.readTimeout(10000))
          .responseCode

        result must be_==(403)
      }

    }
    
/*    "work from within a browser" in {
        
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")
        
        browser.$("h1").first.getText must equalTo("Configure your 'Hello world':")
        
        browser.$("#name").text("Bob")
        browser.$("#submit").click()
        
        browser.$("dl.error").size must equalTo(1)
        browser.$("dl#repeat_field dd.error").first.getText must equalTo("Numeric value expected")
        browser.$("#name").first.getValue must equalTo("Bob")
        
        browser.$("#repeat").text("xxx")
        browser.$("#submit").click()
        
        browser.$("dl.error").size must equalTo(1)
        browser.$("dl#repeat_field dd.error").first.getText must equalTo("Numeric value expected")
        browser.$("#name").first.getValue must equalTo("Bob")
        browser.$("#repeat").first.getValue must equalTo("xxx")
        
        browser.$("#name").text("")
        browser.$("#submit").click()
        
        browser.$("dl.error").size must equalTo(2)
        browser.$("dl#name_field dd.error").first.getText must equalTo("This field is required")
        browser.$("dl#repeat_field dd.error").first.getText must equalTo("Numeric value expected")
        browser.$("#name").first.getValue must equalTo("")
        browser.$("#repeat").first.getValue must equalTo("xxx")
        
        browser.$("#name").text("Bob")
        browser.$("#repeat").text("10")
        browser.$("#submit").click()
        
        browser.$("header a").first.getText must equalTo("Here is the result:")
        
        val items = browser.$("section ul li")
        
        items.size must equalTo(10)
        items.get(0).getText must equalTo("Hello Bob!")
        
        browser.$("p.buttons a").click()
        
        browser.$("h1").first.getText must equalTo("Configure your 'Hello world':")

         
      }
    }
*/
    
  }

  
}