package uk.gov.hmrc.agentepayeregistrationfrontend.controllers

import scala.concurrent.duration._

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlEqualTo}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.redirectLocation
import uk.gov.hmrc.play.http.BadGatewayException

class AgentEpayeRegistrationControllerISpec extends BaseControllerISpec {
  private lazy val controller: AgentEpayeRegistrationController = app.injector.instanceOf[AgentEpayeRegistrationController]

  "root context redirects to start page" in {
    val result = controller.root(FakeRequest())

    status(result) shouldBe 303

    val timeout = 2.seconds
    redirectLocation(result)(timeout).get should include("/start")
  }

  "start displays start page" in {
    val result = controller.start(FakeRequest())

    checkHtmlResultWithBodyText(result, htmlEscapedMessage("start.title"))
  }

  "showAgentDetailsForm displays the agent details form page" in {
    val result = await(controller.showAgentDetailsForm(FakeRequest()))

    checkHtmlResultWithBodyText(result, htmlEscapedMessage("details.agent.title"))
  }

  "agentDetails page" should {
    "show the agentDetails page with the validation errors" when {
      "the agent name is missing" in {
        val request = FakeRequest().withFormUrlEncodedBody(
          validFormRegestrationDetails.map {
            case ("agentName", _) => ("agentName", "")
            case x => x
          }: _*
        )

        val result = await(controller.agentDetails(request))

        checkHtmlResultWithBodyText(result, htmlEscapedMessage("details.agent.title"))
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("error.required"))
      }
    }
    "show the contactDetails page if validation passes" in {
      val request = FakeRequest().withFormUrlEncodedBody(validFormRegestrationDetails: _*)
      val result = await(controller.agentDetails(request))

      checkHtmlResultWithBodyText(result, htmlEscapedMessage("details.contact.title"))
    }
    "retain all the form state and pass it to the next page" in {
      val request = FakeRequest().withFormUrlEncodedBody(validFormRegestrationDetails: _*)

      val result = await(controller.agentDetails(request))
      checkAllFormValuesArePresent(result, validFormRegestrationDetails)
    }
  }

  "contactDetails page" should {
    "show the contactDetails page with the validation errors" when {
      "the contact name is missing" in {
        val request = FakeRequest().withFormUrlEncodedBody(
          validFormRegestrationDetails.map {
            case ("contactName", _) => ("contactName", "")
            case x => x
          }: _*
        )

        val result = await(controller.contactDetails(request))

        checkHtmlResultWithBodyText(result, htmlEscapedMessage("details.contact.title"))
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("error.required"))
      }

      "the contact name is invalid and the telephone number is invalid" in {
        val request = FakeRequest().withFormUrlEncodedBody(
          validFormRegestrationDetails.map {
            case ("contactName", _) => ("contactName", "7^$£")
            case ("telephoneNumber", _) => ("telephoneNumber", "7^$£")
            case x => x
          }: _*
        )

        val result = await(controller.contactDetails(request))

        checkHtmlResultWithBodyText(result, htmlEscapedMessage("details.contact.title"))
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("error.string.invalid"))
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("error.telephone.invalid"))
      }
    }

    "show the addressDetails page if validation passes" in {
      val request = FakeRequest().withFormUrlEncodedBody(validFormRegestrationDetails: _*)
      val result = await(controller.contactDetails(request))

      checkHtmlResultWithBodyText(result, htmlEscapedMessage("details.address.title"))
    }

    "retain all the form state and pass it to the next page" in {
      val request = FakeRequest().withFormUrlEncodedBody(validFormRegestrationDetails: _*)

      val result = await(controller.contactDetails(request))
      checkAllFormValuesArePresent(result, validFormRegestrationDetails)
    }
  }

  "addressDetails page" should {
    "show the addressDetails page again with the validation errors" when {
      "the address line 1 is missing" in {
        val request = FakeRequest().withFormUrlEncodedBody(
          validFormRegestrationDetails.map {
            case ("address.addressLine1", _) => ("address.addressLine1", "")
            case x => x
          }: _*
        )

        val result = await(controller.addressDetails(request))

        checkHtmlResultWithBodyText(result, htmlEscapedMessage("details.address.title"))
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("error.required"))
      }
    }

    "show the summary page if validation passes" in {
      val request = FakeRequest().withFormUrlEncodedBody(validFormRegestrationDetails: _*)
      val result = await(controller.addressDetails(request))

      checkHtmlResultWithBodyText(result, htmlEscapedMessage("summary.title"))
    }

    "retain all the form state and pass it to the next page" in {
      val request = FakeRequest().withFormUrlEncodedBody(validFormRegestrationDetails: _*)

      val result = await(controller.addressDetails(request))
      checkAllFormValuesArePresent(result, validFormRegestrationDetails)
    }
  }

  "summary" should {
    "show the registration confirmation page if validation passes" in {
      val agentRef = "HX2000"

      stubFor(post(urlEqualTo(s"/agent-epaye-registration/registrations"))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(Json.obj("payeAgentReference" -> agentRef).toString())))

      val request = FakeRequest().withFormUrlEncodedBody(validFormRegestrationDetails: _*)
      val result = await(controller.summary(request))

      checkHtmlResultWithBodyText(result, htmlEscapedMessage("registrationConfirmation.title"))

      checkHtmlResultWithBodyText(result, htmlEscapedMessage(agentRef))
    }

    "throw an exception upon registration if there is a problem with the backend service" in {
      val request = FakeRequest().withFormUrlEncodedBody(validFormRegestrationDetails: _*)

      stopServer()

      intercept[BadGatewayException] {
        await(controller.summary(request))
      }
    }

    "retain all the form state and pass it on to the appropriate page" when {
      "amending the agent details" in {
        val request = FakeRequest().withFormUrlEncodedBody(
          validFormRegestrationDetails ++ Seq(("amend", "agentDetails")) : _*)

        val result = await(controller.summary(request))

        checkHtmlResultWithBodyText(result, htmlEscapedMessage("details.agent.title"))
        checkAllFormValuesArePresent(result, validFormRegestrationDetails)
      }
      "amending the contact details" in {
        val request = FakeRequest().withFormUrlEncodedBody(
          validFormRegestrationDetails ++ Seq(("amend", "contactDetails")) : _*)

        val result = await(controller.summary(request))

        checkHtmlResultWithBodyText(result, htmlEscapedMessage("details.contact.title"))
        checkAllFormValuesArePresent(result, validFormRegestrationDetails)
      }
      "amending the address details" in {
        val request = FakeRequest().withFormUrlEncodedBody(
          validFormRegestrationDetails ++ Seq(("amend", "addressDetails")) : _*)

        val result = await(controller.summary(request))

        checkHtmlResultWithBodyText(result, htmlEscapedMessage("details.address.title"))
        checkAllFormValuesArePresent(result, validFormRegestrationDetails)
      }
    }
  }

  val validFormRegestrationDetails = Seq(
    "agentName" -> "Angela Agent",
    "contactName" -> "Charlie Contact",
    "telephoneNumber" -> "01234567890",
    "emailAddress" -> "foo@bar.com",
    "address.addressLine1" -> "1 Streety Street",
    "address.addressLine2" -> "Towny town",
    "address.addressLine3" -> "",
    "address.addressLine4" -> "",
    "address.postcode" -> "AA111AA"
  )

  private def checkAllFormValuesArePresent(result: Result, formKeyValues: Seq[(String, String)]) = {
    formKeyValues.map(t => htmlEscapedMessage(t._2)).foreach(checkHtmlResultWithBodyText(result, _))
  }
}
