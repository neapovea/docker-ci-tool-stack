def call(config) {
	config.PROJECT_VERSION = config.SVN_TAG
    //Descarga del repositorio
    if (config?.stage?.checkout?.cleanWS=="SI") {
        cleanWs()
    }
    checkout([$class: 'SubversionSCM', locations: [[credentialsId: defConfig.get("jenkins.SVNLoginID"), local: '.', remote: config.SVN_BUILD_URL]]])

    //Carga informacion adicional desde el fichero descriptor del proyecto
    pom = readMavenPom(file: utilHelper.defaultIfNull(config?.stage?.build?.filePath, 'pom.xml'))
    config.PROJECT_NAME = pom.groupId+":"+pom.artifactId
    config.PROJECT_VERSION_POM = pom.version
}
