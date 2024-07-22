import com.atlassian.jira.component.ComponentAccessor

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def userManager = ComponentAccessor.getUserManager()
def groupManager = ComponentAccessor.getGroupManager()
def userUtil = ComponentAccessor.getUserUtil()

// Получаем значение customfield_1 из текущего задания
def customField = customFieldManager.getCustomFieldObjectByName("customfield_1")
def userToRemove = issue.getCustomFieldValue(customField) as ApplicationUser

if (userToRemove) {
    log.warn("User found: ${userToRemove.username}")

    // Получаем все группы, в которых состоит пользователь
    def groups = groupManager.getGroupsForUser(userToRemove.name)
    
    groups.each { group ->
        if (group) {
            userUtil.removeUserFromGroup(group, userToRemove)
            log.warn("User: ${userToRemove.username} removed from group: ${group.name}")
        } else {
            log.warn("Group does not exist")
        }
    }
} else {
    log.warn("User not found in custom field 'customfield_1'")
}
