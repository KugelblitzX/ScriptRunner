import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.user.ApplicationUser
import org.apache.log4j.Level
import org.apache.log4j.Logger

// Логування
def log = Logger.getLogger("com.example.jira")
log.setLevel(Level.DEBUG)

// Отримуємо поточну задачу
def issueManager = ComponentAccessor.getIssueManager()
def issue = issueManager.getIssueObject(transientVars["issue"].id)

// Встановлюємо поле звідки будемо тягнути користувача
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def userField = customFieldManager.getCustomFieldObjectByName("JIRA user")
def statusField = customFieldManager.getCustomFieldObjectByName("Component Lead Status")
ApplicationUser userToCheck = issue.getCustomFieldValue(userField) as ApplicationUser

log.info "Перевірка юзера ${userToCheck?.username} на статус Component Lead."

if (userToCheck) {
    boolean isLead = checkComponentLead(userToCheck, log)
    if (isLead) {
        log.info "Користувач ${userToCheck.username} є Component Lead."
        setStatusField(issue, statusField, "Component Lead", log)
    } else {
        log.info "Користувач ${userToCheck.username} не є Component Lead."
        setStatusField(issue, statusField, "Not Component Lead", log)
    }
} else {
    log.info "Не знайшов юзера в полі 'JIRA user'"
    setStatusField(issue, statusField, "No User", log)
}

static def checkComponentLead(ApplicationUser user, def log) {
    def projectComponentManager = ComponentAccessor.getProjectComponentManager()
    def projectManager = ComponentAccessor.getProjectManager()
    def leadStatus = false
    
    // Проходимо по всіх проектах і компонентах, щоб перевірити статус Component Lead
    def allProjects = projectManager.getProjectObjects()
    for (project in allProjects) {
        def components = projectComponentManager.findAllForProject(project.id)
        for (component in components) {
            log.info "Перевірка компонента ${component.name} у проекті ${project.name} для користувача ${user.username}"
            if (component.lead == user.key) {
                log.info "Користувач ${user.username} є лідером компонента ${component.name} у проекті ${project.name}"
                leadStatus = true
            }
        }
    }
    return leadStatus
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
