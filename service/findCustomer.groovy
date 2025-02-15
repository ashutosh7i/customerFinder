import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityFind
import org.moqui.entity.EntityList
import org.moqui.entity.EntityValue

// Get execution context and initialize entity finder
ExecutionContext ec = context.ec
EntityFind ef = ec.entity.find("party.FindCustomerView").distinct(true)
ef.selectField("partyId")

// Dynamic condition building based on provided search criteria
if (emailAddress) { ef.condition("email", emailAddress) }
if (firstName) { ef.condition("firstName", firstName) }
if (lastName) { ef.condition("lastName", lastName) }
if (contactNumber) { ef.condition("contactNumber", contactNumber) }
if (postalAddress) { ef.condition("postalAddress", postalAddress) }

// Handle sorting logic
if (orderByField && orderByField.contains("combinedName")) {
    ef.orderBy("firstName, lastName")
} else if (orderByField) {
    ef.orderBy(orderByField)
}

// Pagination handling
if (!pageNoLimit) { 
    ef.offset(pageIndex as int)
    ef.limit(pageSize as int) 
}

// Execute query and collect results
partyIdList = []
EntityList el = ef.list()
for (EntityValue ev in el) partyIdList.add(ev.partyId)

// Set result counts
partyIdListCount = ef.count()
customerList = partyIdList
totalCount = partyIdListCount
