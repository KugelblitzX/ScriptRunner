import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def userManager = ComponentAccessor.getUserManager()
def groupManager = ComponentAccessor.getGroupManager()
def userUtil = ComponentAccessor.getUserUtil()

// Получаем значение customfield_1 из текущего задания
def customField = customFieldManager.getCustomFieldObjectByName("customfield_1")
def userToRemove = issue.getCustomFieldValue(customField) as ApplicationUser

// Получаем текстовое поле для записи информации о группах
def logField = customFieldManager.getCustomFieldObjectByName("LogFieldName") // Замените "LogFieldName" на имя вашего текстового поля

log.warn("Attempting to find user from custom field 'customfield_1'")

if (userToRemove) {
    log.warn("User found: ${userToRemove.username}")

    // Получаем все группы, в которых состоит пользователь
    def groups = groupManager.getGroupsForUser(userToRemove.name)
    
    // Список для хранения имен групп
    def removedGroups = []

    groups.each { group ->
        if (group) {
            userUtil.removeUserFromGroup(group, userToRemove)
            log.warn("User: ${userToRemove.username} removed from group: ${group.name}")
            removedGroups.add(group.name) // Добавляем имя группы в список
        } else {
            log.warn("Group does not exist")
        }
    }

    // Обновляем текстовое поле с именами групп, разделенными запятыми
    def removedGroupsString = removedGroups.join(", ")
    issue.setCustomFieldValue(logField, removedGroupsString)
    log.warn("Updated log field with removed groups: ${removedGroupsString}")

} else {
    log.warn("User not found in custom field 'customfield_1'")
}
