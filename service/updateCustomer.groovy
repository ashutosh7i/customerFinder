import org.moqui.context.ExecutionContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Initialize execution context and validate customer existence
ExecutionContext ec = context.ec
serviceResult = ec.service.sync()
    .name("PartyServices.find#FindCustomer")
    .parameters([emailAddress: emailAddress])
    .call()

if (!serviceResult.customerList) {
    return ec.message.addError("Customer Does Not Exist")
}
def partyId = serviceResult.customerList[0]

// Setup date formatting for record keeping
LocalDate currentDate = LocalDate.now()
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
String formattedDate = currentDate.format(formatter)

// Handle postal address updates
if (postalAddress) {
    // Find and expire existing postal address
    def contactMechPostal = ec.entity.find("ContactMech")
        .condition("infoString", emailAddress)
        .one()
    if (contactMechPostal) {
        def pcmOld = ec.entity.find("PartyContactMech")
            .condition("contactMechId", contactMechPostal.contactMechId)
            .condition("partyId", partyId)
            .one()
        if (pcmOld) {
            pcmOld.thruDate = formattedDate
            pcmOld.update()
        }
    }

    // Create new postal address records
    def newContactMechPostal = ec.entity.makeValue("ContactMech")
    newContactMechPostal.setSequencedIdPrimary()
    newContactMechPostal.contactMechTypeEnumId = "CmtPostalAddress"
    newContactMechPostal.infoString = postalAddress
    newContactMechPostal.create()

    // Create postal address details
    def postalAddressEntity = ec.entity.makeValue("PostalAddress")
    postalAddressEntity.contactMechId = newContactMechPostal.contactMechId
    postalAddressEntity.address1 = postalAddress
    postalAddressEntity.create()

    // Link new postal address to party
    def pcmNewPostal = ec.entity.makeValue("PartyContactMech")
    pcmNewPostal.partyId = partyId
    pcmNewPostal.contactMechId = newContactMechPostal.contactMechId
    pcmNewPostal.contactMechPurposeId = "EmailPrimary"
    pcmNewPostal.fromDate = formattedDate
    pcmNewPostal.create()
}

// Handle phone number updates
if (phoneNumber) {
    // Find and expire existing phone number
    def contactMechPhone = ec.entity.find("ContactMech")
        .condition("infoString", emailAddress)
        .one()
    if (contactMechPhone) {
        def pcmOldPhone = ec.entity.find("PartyContactMech")
            .condition("contactMechId", contactMechPhone.contactMechId)
            .condition("partyId", partyId)
            .one()
        if (pcmOldPhone) {
            pcmOldPhone.thruDate = formattedDate
            pcmOldPhone.update()
        }
    }

    // Create new phone number records
    def newContactMechPhone = ec.entity.makeValue("ContactMech")
    newContactMechPhone.setSequencedIdPrimary()
    newContactMechPhone.contactMechTypeEnumId = "CmtTelecomNumber"
    newContactMechPhone.create()

    // Create telecom number details
    def telecomNumberEntity = ec.entity.makeValue("TelecomNumber")
    telecomNumberEntity.contactMechId = newContactMechPhone.contactMechId
    telecomNumberEntity.contactNumber = phoneNumber
    telecomNumberEntity.create()

    // Link new phone number to party
    def pcmNewPhone = ec.entity.makeValue("PartyContactMech")
    pcmNewPhone.partyId = partyId
    pcmNewPhone.contactMechId = newContactMechPhone.contactMechId
    pcmNewPhone.contactMechPurposeId = "EmailPrimary"
    pcmNewPhone.fromDate = formattedDate
    pcmNewPhone.create()
}