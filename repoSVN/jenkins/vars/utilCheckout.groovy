def call(config) {
    //Descarga del repositorio
    if (config?.stage?.checkout?.cleanWS=="SI") {
        cleanWs()
    }
    checkout([$class: 'SubversionSCM', locations: [[credentialsId: '11bed131-793e-485d-8bea-886ab3c52c0e', local: '.', remote: 'http://localhost:18080/svn/pruebamaven/']]])

    //Carga informacion adicional desde el fichero descriptor del proyecto
    pom = readMavenPom(file: utilHelper.defaultIfNull(config?.stage?.build?.filePath, 'pom.xml'))
    config.PROJECT_NAME = pom.groupId+":"+pom.artifactId
    config.PROJECT_VERSION_POM = pom.version
}
