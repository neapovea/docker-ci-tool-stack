def call(config) {
    def projectDateString = ''
    projectDate = LocalDate.now().format("yyyy-MM-dd'T'HH:mm:ss", TimeZone.getTimeZone("Europe/Madrid")) + '+0100'
    projectDateString = ' -Dsonar.projectDate=' + projectDate

    def filePath = utilHelper.defaultIfNull(build?.filePath, 'pom.xml')
    def sonarParameters = utilHelper.defaultIfNull(addparameters, [:])
    def parameters = sonarParameters.collect { /$it.key="$it.value"/ } join " "
    withSonarQubeEnv(defConfig.get("sonar.version")) {
        withMaven(jdk: defConfig.get("sonar.jdk"),
                maven: defConfig.get("sonar.maven"),
                mavenOpts: utilHelper.defaultIfNull(codeQuality?.mavenOpts, utilHelper.defaultIfNull(build?.mavenOpts, defConfig.get("maven.options")))
        ) {
            bat('mvn -f ' + filePath + ' ' + defConfig.get("sonar.plugin") + ':sonar ' + projectDate +
                    ' -Dsonar.projectVersion=' + version +
                    ' ' + parameters)
        }
    }
}


def call(config) {
    //Establece el leakPeriod indicado en el job o lo calcula segun la version actual
    def version = config.PROJECT_VERSION
    try {
        version = utilHelper.defaultIfEmpty(config.PROJECT_VERSION_POM.split("-")[0], config.PROJECT_VERSION)
    } catch (ex) {
    }
    def prevVersion = utilHelper.previousVersion(version)
    echo("Comparacion " + prevVersion + " con " + version)
    def leakPeriodFinal = utilHelper.defaultIfEmpty(config.LEAK_PERIOD, prevVersion)
    def key = config.PROJECT_NAME
    env.SONARURL = "http://" + utilConfig.get("sonar.domain") + "/sonarqube/dashboard/index/" + config.PROJECT_NAME
    def projectDateString = ''
    def projectDate = config.PROJECT_DATE
    if (projectDate == '')
        projectDate = utilGitHelper.getDateLastCommit().format("yyyy-MM-dd'T'HH:mm:ss", TimeZone.getTimeZone("Europe/Madrid")) + '+0100'
    projectDateString = ' -Dsonar.projectDate=' + projectDate


    setLeakPeriod(leakPeriodFinal, key)
    sonarMaven(config?.stage?.codeQuality, config?.stage?.build, config.PROJECT_VERSION, projectDateString, config?.stage?.codeQuality?.additionalParams)
    //establece el leakperiod en sonar a la opcion por defecto
    //setLeakPeriod('previous_version', key)
}

/**
 * Establece a traves de la api de Sonar el leakperiod de un proyecto concreto
 * @param leakPeriod
 * @param key
 * @return
 */
def setLeakPeriod(leakPeriod, key) {
    withCredentials([string(credentialsId: defConfig.get("jenkins.SonarTokenID"), variable: 'token')]) {
        sh("curl -u " + token + ": -X POST \"http://" + utilConfig.get("sonar.domain") + "/sonarqube/api/properties?id=sonar.leak.period&value=" + leakPeriod + "&resource=" + key + "\"")
    }
}

def sonarMaven(codeQuality, build, version, projectDate, addparameters) {
    def filePath = utilHelper.defaultIfNull(build?.filePath, 'pom.xml')
    def sonarParameters = utilHelper.defaultIfNull(addparameters, [:])
    def parameters = sonarParameters.collect { /$it.key="$it.value"/ } join " "
    withSonarQubeEnv(utilConfig.get("sonar.version")) {
        withMaven(jdk: utilConfig.get("sonar.jdk"),
                maven: utilConfig.get("sonar.maven"),
                mavenOpts: utilHelper.defaultIfNull(codeQuality?.mavenOpts, utilHelper.defaultIfNull(build?.mavenOpts, utilConfig.get("maven.options")))
        ) {
            sh('mvn -f ' + filePath + ' ' + utilConfig.get("sonar.plugin") + ':sonar ' + projectDate +
                    ' -Dsonar.projectVersion=' + version +
                    ' ' + parameters)
        }
    }
}
