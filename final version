import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.bc.user.UserService
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.user.ApplicationUser
import org.apache.log4j.Level
import org.apache.log4j.Logger

// Логування
def log = Logger.getLogger("com.example.jira")
log.setLevel(Level.DEBUG)

// Отримимуємо поточну задачу
Issue issue = ComponentAccessor.getIssueManager().getIssueObject(issue.key)

// Встановлюємо поле звідки будемо тягнути користувача для блокування
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def userField = customFieldManager.getCustomFieldObjectByName("JIRA user")
def statusField = customFieldManager.getCustomFieldObjectByName("Component Lead Status")
def customField1 = customFieldManager.getCustomFieldObjectByName("Block user")
ApplicationUser userToDeactivate = issue.getCustomFieldValue(userField) as ApplicationUser
log.info "Перевірка юзера ${userToDeactivate?.username} на статус Component Lead."
if (userToDeactivate) {
    boolean success = activateDeactivateUser(userToDeactivate.username, false, log)
    if (!success) {
        log.info "Користувач ${userToDeactivate.username} є Component Lead та не може бути деактивованим."
        setStatusField(issue, statusField, "Component Lead", log)
        addComment(issue, "User ${userToDeactivate.displayName} since is component lead. Contact the Atlassian team", log)
    } else {
        log.info "Користувача ${userToDeactivate.username} деактивовано."
        setStatusField(issue, statusField, "Not Component Lead", log)
        setCustomField1(issue, customField1, "1", log)
        addComment(issue, "User deactivated: ${userToDeactivate.displayName}", log)
    }
} else {
    log.info "Не знайшов юзера в полі 'JIRA user'"
    setStatusField(issue, statusField, "No User", log)
}

static def setStatusField(Issue issue, def statusField, String status, def log) {
    def issueService = ComponentAccessor.getIssueService()
    def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
    def issueInputParameters = issueService.newIssueInputParameters()
    issueInputParameters.addCustomFieldValue(statusField.idAsLong, status)
    def updateValidationResult = issueService.validateUpdate(user, issue.id, issueInputParameters)
    if (updateValidationResult.isValid()) {
        def updateResult = issueService.update(user, updateValidationResult)
        if (updateResult.isValid()) {
            log.info "Поле статусу успішно оновлено на: ${status}"
        } else {
            log.error "Не вдалося оновити поле статусу: ${updateResult.errorCollection}"
        }
    } else {
        log.error "Не вдалося оновити поле статусу: ${updateValidationResult.errorCollection}"
    }
}

static def setCustomField1(Issue issue, def customField1, String value, def log) {
    def issueService = ComponentAccessor.getIssueService()
    def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
    def issueInputParameters = issueService.newIssueInputParameters()
    issueInputParameters.addCustomFieldValue(customField1.idAsLong, value)
    def updateValidationResult = issueService.validateUpdate(user, issue.id, issueInputParameters)
    if (updateValidationResult.isValid()) {
        def updateResult = issueService.update(user, updateValidationResult)
        if (updateResult.isValid()) {
            log.info "Поле CustomField1 успешно обновлено на: ${value}"
        } else {
            log.error "Не удалось обновить поле CustomField1: ${updateResult.errorCollection}"
        }
    } else {
        log.error "Не удалось обновить поле CustomField1: ${updateValidationResult.errorCollection}"
    }
}

static def addComment(Issue issue, String commentText, def log) {
    def commentManager = ComponentAccessor.getCommentManager()
    def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
    commentManager.create(issue, user, commentText, true)
    log.info "Комент додано: ${commentText}"
}

static def deactivateUser(String userName, def log) {
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
            // Перевіряємо, якщо помилка пов'язана з тим, що користувач є Component Lead
            def errors = updateUserValidationResult.errorCollection.errors
            if (errors && errors.active && errors.active.contains("Cannot deactivate user because they are currently a component lead")) {
                log.error "Оновлення ${userToUpdate.name} не вдалося. Errors: ${errors}"
                return false
            }
            log.error "Оновлення ${userToUpdate.name} не вдалося. ${updateUserValidationResult.errorCollection}"
            return false
        }

        // Деактивуємо користувача
        if (!userToUpdate.isActive()) {
            log.info("Користувач вже неактивний")
        } else {
            log.info("Спроба деактивації")
            userService.updateUser(updateUserValidationResult)
            log.info("${updateUser.name} деактивований")
        }
        return true
    } else {
        log.info "Користувача не знайдено"
        return false
    }
}
