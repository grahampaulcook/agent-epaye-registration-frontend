/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.agentepayeregistrationfrontend.models

import play.api.libs.json.Json

case class RegistrationRequest(agentName: String,
                               contactName: String,
                               telephoneNumber: Option[String],
                               faxNumber: Option[String],
                               emailAddress: Option[String],
                                address: Address)

object RegistrationRequest {
  implicit val registrationRequestFormat = Json.format[RegistrationRequest]

  def apply(agentName: String,
            contactName: String,
            telephoneNumber: Option[String],
            faxNumber: Option[String],
            emailAddress: Option[String],
            addressLine1: String,
            addressLine2: String,
            addressLine3: Option[String],
            addressLine4: Option[String],
            postCode: String): RegistrationRequest = RegistrationRequest(
    agentName, contactName, telephoneNumber, faxNumber, emailAddress,
    Address(addressLine1, addressLine2, addressLine3, addressLine4, postCode)
  )
}
