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

