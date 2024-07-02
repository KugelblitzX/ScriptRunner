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
} else {
    // Логування можна залишити на випадок, якщо користувача не знайдено
    log.info "Не знайшов юзера в полі 'JIRA user'"
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
            log.info("Користувач вже неактивний")
        } else {
            userService.updateUser(updateUserValidationResult)
            log.info("${updateUser.name} деактивований")
        }
        return true
    } else {
        log.info "Користувача не знайдено"
        return false
    }
}
