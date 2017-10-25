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

package uk.gov.hmrc.agentepayeregistrationfrontend

import play.api.data.Forms._
import play.api.data.Mapping
import play.api.data.validation.{Constraint, Constraints, _}

package object controllers {

  object FieldMappings {
    private val postcodeWithoutSpacesRegex = "^[A-Z]{1,2}[0-9][0-9A-Z]?[0-9][A-Z]{2}$|BFPO[0-9]{1,5}$".r
    private val telephoneNumberRegex = "^[0-9 ()]*$"
    private val validStringRegex = "[a-zA-Z0-9,.()\\-\\!@\\s]+"
    private val emailRegex = """^[a-zA-Z0-9-.]+?@[a-zA-Z0-9-.]+$""".r
    private val nonEmptyPostcode: Constraint[String] = Constraint[String] { fieldValue: String =>
      Constraints.nonEmpty(fieldValue) match {
        case i: Invalid =>
          i
        case Valid =>
          val error = "error.postcode.invalid"
          val fieldValueWithoutSpaces = fieldValue.replace(" ", "")
          postcodeWithoutSpacesRegex.unapplySeq(fieldValueWithoutSpaces)
            .map(_ => Valid)
            .getOrElse(Invalid(ValidationError(error)))
      }
    }

    private val telephoneNumber: Constraint[String] = Constraint[String] { fieldValue: String =>
      Constraints.nonEmpty(fieldValue) match {
        case i: Invalid => i
        case Valid => fieldValue match {
          case value if !value.matches(telephoneNumberRegex) =>
            Invalid(ValidationError("error.telephone.invalid"))
          case _ => Valid
        }
      }
    }

    private def emailAddress: Constraint[String] = Constraint[String]("constraint.email") { e =>
      if (e == null) Invalid(ValidationError("error.email"))
      else if (e.trim.isEmpty) Invalid(ValidationError("error.email"))
      else emailRegex.findFirstMatchIn(e.trim)
        .map(_ => Valid)
        .getOrElse(Invalid("error.email"))
    }

    private val validName: Constraint[String] = Constraint[String] { fieldValue: String =>
      Constraints.nonEmpty(fieldValue) match {
        case i @ Invalid(_) =>
          i
        case Valid =>
          if (fieldValue.matches(validStringRegex))
            Valid
          else
            Invalid(ValidationError("error.string.invalid"))
      }
    }

    def postcode: Mapping[String] = text(maxLength = 8) verifying nonEmptyPostcode
    def telephone: Mapping[Option[String]] = optional(text(maxLength = 24) verifying telephoneNumber)
    def name: Mapping[String] = text(maxLength = 56) verifying(validName)
    def emailAddr: Mapping[Option[String]] = optional(text(maxLength = 129) verifying emailAddress)
    def addressLine12: Mapping[String] = text(maxLength = 35) verifying(validName)
    def addressLine34: Mapping[Option[String]] = optional(text(maxLength = 35) verifying(validName))
  }
}
