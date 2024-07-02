import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.bc.user.UserService
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.user.ApplicationUser

// Отримуємо поточну задачу
Issue issue = ComponentAccessor.getIssueManager().getIssueObject(issue.key)

// Встановлюємо поле звідки будемо тягнути користувача для блокування
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def userField = customFieldManager.getCustomFieldObjectByName("JIRA user")
ApplicationUser userToDeactivate = issue.getCustomFieldValue(userField) as ApplicationUser

if (userToDeactivate) {
    deactivateUser(userToDeactivate.username)
}

static def deactivateUser(String userName) {
    // Отримуємо користувача за ім'ям
    def userToUpdate = ComponentAccessor.getUserManager().getUserByName(userName)
    if (userToUpdate) {
        // Отримуємо сервіс користувачів
        def userService = ComponentAccessor.getComponent(UserService)
        
        // Створюємо оновленого користувача з новим статусом активності (false для деактивації)
        def updateUser = userService.newUserBuilder(userToUpdate).active(false).build()
        
        // Валідовуємо оновлення користувача
        def updateUserValidationResult = userService.validateUpdateUser(updateUser)
        
        // Перевіряємо, чи валідна валідація
        if (!updateUserValidationResult.valid) {
            return false
        }

        // Деактивуємо користувача
        if (!userToUpdate.isActive()) {
            return true
        } else {
            userService.updateUser(updateUserValidationResult)
            return true
        }
    }
    return false
}
