def call(config) {
    BuildMaven(config?.stage?.build, config.PROFILE, config.DEBUG == "SI")
}

def BuildMaven(build, profile, debug) {
    def mavenVersion = utilHelper.getMavenVersion(build.jdk)
    def jdkBits = ""
    if (build.jdkBits) jdkBits = "_" + build.jdkBits
    withMaven(
            jdk: build.jdk + jdkBits,
            maven: mavenVersion,
            mavenOpts: utilHelper.defaultIfNull(build?.mavenOpts, defConfig.get("maven.options")),
            options: [
                    artifactsPublisher(disabled: build.skipArtifacts),
                    findbugsPublisher(disabled: true),
                    openTasksPublisher(disabled: true)]) {
        def filePath = utilHelper.defaultIfNull(build?.filePath, 'pom.xml')
        def profileStr = utilHelper.getProfileString(build, profile)
        def debugStr = ''
        if (debug) debugStr = ' -X '
        sh('mvn -f ' + filePath + ' ' + profileStr + ' -DskipTests -Dcobertura.skip -Dmaven.javadoc.skip=true clean install' + debugStr)
    }
}