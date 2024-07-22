import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.util.UserManager
import com.atlassian.jira.bc.group.GroupService
import com.atlassian.jira.bc.group.GroupService.RemoveUserFromGroupValidationResult

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def userManager = ComponentAccessor.getUserManager()
def groupService = ComponentAccessor.getComponent(GroupService)

// Получаем значение customfield_1 из текущего задания
def customField = customFieldManager.getCustomFieldObjectByName("customfield_1")
def userKey = issue.getCustomFieldValue(customField) as String
def user = userManager.getUserByKey(userKey)

if (user) {
    // Получаем все группы, в которых состоит пользователь
    def groups = ComponentAccessor.getGroupManager().getGroupsForUser(user.name)
    
    groups.each { group ->
        // Валидация удаления пользователя из группы
        RemoveUserFromGroupValidationResult validationResult = groupService.validateRemoveUserFromGroup(user.directoryUser, group)
        
        if (validationResult.isValid()) {
            // Удаление пользователя из группы
            groupService.removeUserFromGroup(validationResult)
            log.info("User ${user.name} removed from group ${group.name}")
        } else {
            log.warn("Failed to remove user ${user.name} from group ${group.name}: ${validationResult.errorCollection}")
        }
    }
} else {
    log.warn("User with key ${userKey} not found")
}
