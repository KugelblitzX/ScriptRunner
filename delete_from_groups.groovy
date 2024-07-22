import com.atlassian.jira.component.ComponentAccessor

def userUtil = ComponentAccessor.userUtil
def userManager = ComponentAccessor.userManager
def customFieldManager = ComponentAccessor.getCustomFieldManager()

// Получаем значение customfield_1 из текущего задания
def customField = customFieldManager.getCustomFieldObjectByName("customfield_1")
def userKey = issue.getCustomFieldValue(customField) as String
def userToRemove = userManager.getUserByName(userKey)

if (!userToRemove) {
    log.warn("User: $userKey doesn't exist")
    return
}

// Получаем все группы, в которых состоит пользователь
def userGroups = ComponentAccessor.getGroupManager().getGroupsForUser(userToRemove.name)

if (!userGroups) {
    log.warn("User: $userToRemove.username is not in any group")
    return
}

userGroups.each { group ->
    if (!group) {
        log.warn("Group doesn't exist")
        return
    }

    userUtil.removeUserFromGroup(group, userToRemove)
    log.warn("User: $userToRemove.username removed from the group: ${group.name}")
}
