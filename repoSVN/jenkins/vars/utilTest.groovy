def call(config) {
 
    def jdk = utilHelper.defaultIfNull(testing?.jdk, utilHelper.defaultIfNull(build?.jdk, '1.8'))
    def mavenVersion = utilHelper.getMavenVersion(jdk)
    def filePath = utilHelper.defaultIfNull(testing?.filePath, utilHelper.defaultIfNull(build?.filePath, 'pom.xml'))

    try {
        withMaven(
                jdk: jdk,
                maven: mavenVersion,
                mavenOpts: utilHelper.defaultIfNull(testing?.mavenOpts, utilHelper.defaultIfNull(build?.mavenOpts, defConfig.get("maven.options"))),
                options: [
                        artifactsPublisher(disabled: true),
                        findbugsPublisher(disabled: true),
                        openTasksPublisher(disabled: true)]) {

            bat('mvn -f ' + filePath + ' ' + ' org.jacoco:jacoco-maven-plugin:prepare-agent test -Dmaven.javadoc.skip=true -fae -Dmaven.test.failure.ignore=false')
        }
    } catch (err) {
        echo 'Testing failed!'
        def sw = new StringWriter()
        def pw = new PrintWriter(sw)
        err.printStackTrace(pw)
        echo sw.toString()
        currentBuild.result = 'UNSTABLE'
    }

}

def jdkTesting(testing, build) {
    return utilHelper.defaultIfNull(testing?.jdk, utilHelper.defaultIfNull(build?.jdk, '1.8'))
}
