import com.atlassian.jira.component.ComponentAccessor

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def userManager = ComponentAccessor.getUserManager()
def groupManager = ComponentAccessor.getGroupManager()
def userUtil = ComponentAccessor.getUserUtil()

// Получаем значение customfield_1 из текущего задания
def customField = customFieldManager.getCustomFieldObjectByName("customfield_1")
def userName = issue.getCustomFieldValue(customField) as String
def user = userManager.getUserByName(userName)

if (user) {
    // Получаем все группы, в которых состоит пользователь
    def groups = groupManager.getGroupsForUser(user.name)
    
    groups.each { group ->
        if (group) {
            userUtil.removeUserFromGroup(group, user)
            log.warn("User: ${user.name} removed from group: ${group.name}")
        } else {
            log.warn("Group does not exist")
        }
    }
} else {
    log.warn("User: ${userName} doesn't exist")
}
