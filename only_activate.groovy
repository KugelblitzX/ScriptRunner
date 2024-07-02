import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.bc.user.UserService
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.user.ApplicationUser

// Отримуємо поточну задачу
Issue issue = ComponentAccessor.getIssueManager().getIssueObject(issue.key)

// Назва поля, з якого потрібно отримати користувача
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def userField = customFieldManager.getCustomFieldObjectByName("User Field") 
ApplicationUser userToActivate = issue.getCustomFieldValue(userField) as ApplicationUser

if (userToActivate) {
    activateUser(userToActivate.username)
}

static def activateUser(String userName) {
    // Отримуємо користувача
    def userToUpdate = ComponentAccessor.getUserManager().getUserByName(userName)
    if (userToUpdate) {
        // Отримуємо сервіс користувачів
        def userService = ComponentAccessor.getComponent(UserService)
        
        def updateUser = userService.newUserBuilder(userToUpdate).active(true).build()
        
        def updateUserValidationResult = userService.validateUpdateUser(updateUser)
        
        if (!updateUserValidationResult.valid) {
            return false
        }

        // Активуємо користувача
        if (!userToUpdate.isActive()) {
            userService.updateUser(updateUserValidationResult)
        }
        return true
    }
    return false
}
