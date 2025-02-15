import org.moqui.context.ExecutionContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Get execution context and check for existing customer
ExecutionContext ec = context.ec
serviceResult = ec.service.sync()
                 .name("PartyServices.find#FindCustomer")
                 .parameters([emailAddress: emailAddress])
                 .call()

// Prevent duplicate customer creation
if (serviceResult.customerList) {
    return ec.message.addError("Customer Already Exists")
}

// Setup current date formatting
LocalDate currentDate = LocalDate.now()
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
String formattedDate = currentDate.format(formatter)

// Create base Party record
newparty = ec.entity.makeValue("Party")
newparty.setFields(context, true, null, null)
newparty.partyTypeEnumId = "Person"
newparty.setSequencedIdPrimary()
newparty.create()

// Create Person record linked to Party
newperson = ec.entity.makeValue("Person")
newperson.setFields(context, true, null, null)
newperson.partyId = newparty.partyId
newperson.create()

// Create ContactMech for email
newcontactmech = ec.entity.makeValue("ContactMech")
newcontactmech.setFields(context, true, null, null)
newcontactmech.setSequencedIdPrimary()
newcontactmech.contactMechTypeEnumId = "CmtEmailAddress"
newcontactmech.infoString = emailAddress
newcontactmech.create()

// Link email to Party via PartyContactMech
pcm = ec.entity.makeValue("PartyContactMech")
pcm.setFields(context, true, null, null)
pcm.partyId = newparty.partyId
pcm.contactMechId = newcontactmech.contactMechId
pcm.contactMechPurposeId = "EmailPrimary"
pcm.fromDate = formattedDate
pcm.create()
