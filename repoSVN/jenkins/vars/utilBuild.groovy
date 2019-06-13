def call(config) {
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
                    
                    bat('mvn -f ' + filePath + ' -DskipTests -Dcobertura.skip -Dmaven.javadoc.skip=true clean install' )
                }


}
