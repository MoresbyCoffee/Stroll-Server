package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AuthSpec extends Specification {

  System.setProperty("MONGODB_URL", "localhost:12345")
  System.setProperty("MONGODB_USERNAME", "")
  System.setProperty("MONGODB_PASSWORD", "")
  System.setProperty("MONGODB_DB", "testDatabase")

  "Application" should {

      "should authenticate" in {
        running(TestServer(3333)) {

          import scalaj.http._

          val result = Http.postData("http://localhost:3333/auth", """{"id":"12","token":"data"}""")
            .header("Content-Type", "application/json")
            .header("Charset", "UTF-8")
            .option(HttpOptions.readTimeout(10000))
            .asString

          import play.api.libs.json._

          val jsonResult: JsValue = Json.parse(result)
          val sessionId    = (jsonResult \ "id").asOpt[String]
          val sessionToken = (jsonResult \ "token").asOpt[String]

          println("^^^^^^^^^^^^^^^^^^^ " + result);

          sessionId.isDefined must beTrue
          sessionToken.isDefined must beTrue

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