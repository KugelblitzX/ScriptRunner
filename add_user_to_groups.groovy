import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def userManager = ComponentAccessor.getUserManager()
def groupManager = ComponentAccessor.getGroupManager()
def userUtil = ComponentAccessor.getUserUtil()

// Получаем значение customfield_1 из текущего задания
def customField = customFieldManager.getCustomFieldObjectByName("customfield_1")
def userToAdd = issue.getCustomFieldValue(customField) as ApplicationUser

// Получаем текстовое поле, из которого нужно взять имена групп
def logField = customFieldManager.getCustomFieldObjectByName("LogFieldName") // Замените "LogFieldName" на имя вашего текстового поля
def logFieldValue = issue.getCustomFieldValue(logField) as String

log.warn("Attempting to find user from custom field 'customfield_1'")

if (userToAdd) {
    log.warn("User found: ${userToAdd.username}")

    if (logFieldValue) {
        // Разделяем имена групп из строки, разделенной запятыми
        def groupsToAdd = logFieldValue.split(",").collect { it.trim() }

        groupsToAdd.each { groupName ->
            def group = groupManager.getGroup(groupName)
            if (group) {
                userUtil.addUserToGroup(group, userToAdd)
                log.warn("User: ${userToAdd.username} added to group: ${groupName}")
            } else {
                log.warn("Group: ${groupName} does not exist")
            }
        }
    } else {
        log.warn("No groups found in log field")
    }
} else {
    log.warn("User not found in custom field 'customfield_1'")
}
