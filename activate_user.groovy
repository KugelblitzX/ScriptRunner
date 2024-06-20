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

// Отримуємо поточне завдання
Issue issue = ComponentAccessor.getIssueManager().getIssueObject(issue.key)

// Вказуємо ім'я користувацького поля, з якого потрібно отримати користувача
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def userField = customFieldManager.getCustomFieldObjectByName("User Field") // Замініть "User Field" на назву вашого користувацького поля
def statusField = customFieldManager.getCustomFieldObjectByName("Component Lead Status") // Додайте користувацьке поле для статусу
ApplicationUser userToActivate = issue.getCustomFieldValue(userField) as ApplicationUser

log.info "Перевірка користувача ${userToActivate?.username} для активації."

if (userToActivate) {
    boolean success = activateUser(userToActivate.username, log)
    if (success) {
        log.info "Користувач ${userToActivate.username} активований."
        setStatusField(issue, statusField, "Active", log)
        addComment(issue, "User activated: ${userToActivate.displayName}", log)
    } else {
        log.info "Не вдалося активувати користувача ${userToActivate.username}."
    }
} else {
    log.info "Користувача не знайдено в користувацькому полі"
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

static def addComment(Issue issue, String commentText, def log) {
    def commentManager = ComponentAccessor.getCommentManager()
    def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
    commentManager.create(issue, user, commentText, true)
    log.info "Коментар додано: ${commentText}"
}

static def activateUser(String userName, def log) {
    // Отримуємо користувача за ім'ям
    def userToUpdate = ComponentAccessor.getUserManager().getUserByName(userName)
    if (userToUpdate) {
        // Отримуємо сервіс користувачів
        def userService = ComponentAccessor.getComponent(UserService)
        
        // Створюємо оновленого користувача з новим статусом активності (true для активації)
        def updateUser = userService.newUserBuilder(userToUpdate).active(true).build()
        
        // Валідовуємо оновлення користувача
        def updateUserValidationResult = userService.validateUpdateUser(updateUser)
        
        // Перевіряємо, чи валідна валідація
        if (!updateUserValidationResult.valid) {
            log.error "Оновлення ${userToUpdate.name} не вдалося. ${updateUserValidationResult.errorCollection}"
            return false
        }

        // Активуємо користувача
        if (userToUpdate.isActive()) {
            log.info("Користувач вже активний")
        } else {
            log.info("Спроба активації")
            userService.updateUser(updateUserValidationResult)
            log.info("${updateUser.name} активований")
        }
        return true
    } else {
        log.info "Користувача не знайдено"
        return false
    }
}
