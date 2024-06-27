import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.user.ApplicationUser
import org.apache.log4j.Level
import org.apache.log4j.Logger

// Логування
def log = Logger.getLogger("com.example.jira")
log.setLevel(Level.DEBUG)

// Отримуємо поточну задачу
Issue issue = ComponentAccessor.getIssueManager().getIssueObject(issue.key)

// Встановлюємо поле звідки будемо тягнути користувача
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def userField = customFieldManager.getCustomFieldObjectByName("JIRA user")
def statusField = customFieldManager.getCustomFieldObjectByName("Component Lead Status")
ApplicationUser userToCheck = issue.getCustomFieldValue(userField) as ApplicationUser

if (userToCheck) {
    boolean isLead = checkComponentLead(userToCheck, log)
    if (isLead) {
        setStatusField(issue, statusField, "Component Lead", log)
    } else {
        setStatusField(issue, statusField, "Not Component Lead", log)
    }
} else {
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
    }
}
