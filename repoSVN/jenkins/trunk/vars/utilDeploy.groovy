def call(config) {
    if (config?.stage?.deploy?.includeArtifacts) {
        archiveArtifacts artifacts: config.stage.deploy.includeArtifacts, onlyIfSuccessful: true
    }
    if (config.DEPLOY_BINARIES == 'SI' && (config?.stage?.deploy?.uploadBinaries == 'SI' || config?.stage?.deploy?.uploadBinaries == null)) {
        deployBinaries(config)
    }
    if (config?.stage?.deploy?.artifactory == 'SI' && config.DEPLOY_LIBRARY == 'SI') {
        deployArtifactory(config?.stage?.build)
    }
    if (config?.stage?.deploy?.pre && config.DEPLOY_PRE == 'SI') {
        DeployApp(config?.stage?.deploy?.pre, pom.artifactId, pom.version)
    }
    if (config?.stage?.deploy?.pro && config.DEPLOY_PRO == 'SI') {
        DeployApp(config?.stage?.deploy?.pro, pom.artifactId, pom.version)
    }
}



def deployArtifactory(build) {
    script {
        def filePath = utilHelper.defaultIfNull(build?.filePath, 'pom.xml')
        def server = Artifactory.server "utilArtefactory"
        def buildInfo = Artifactory.newBuildInfo()
        buildInfo.env.capture = true
        def rtMaven = Artifactory.newMavenBuild()
        rtMaven.tool = 'M3' // Tool name from Jenkins configuration
        //rtMaven.opts = "-Denv=dev"
        rtMaven.deployer releaseRepo: 'util-artifacts-deploy', snapshotRepo: 'util-artifacts-deploy', server: server
        rtMaven.resolver releaseRepo: 'util-internal', snapshotRepo: 'util-internal', server: server
        rtMaven.deployer.deployArtifacts = true
        rtMaven.deployer.artifactDeploymentPatterns.addExclude("*.war").addExclude("*.ear")
        def profile = ''
        if (build?.profileFilePath) profile = ' -s settings.xml '
        if (params.PROFILE) profile += ' -P ' + params.PROFILE
        rtMaven.run pom: filePath, goals: 'install ' + profile + ' --global-settings "D:\\Program Files (x86)\\Jenkins\\tools\\hudson.tasks.Maven_MavenInstallation\\M3\\conf\\settings.xml" -DskipTests -Dmaven.test.skip -Dcobertura.skip -Dmaven.javadoc.skip=true -Dmaven.compiler.source=' + build.jdk + ' -Dmaven.compiler.target=' + build.jdk, buildInfo: buildInfo

        server.publishBuildInfo buildInfo
    }
}

def putToBinariRepo(source, optional, projectVersion) {
    putToBinariRepo(source, '', optional, projectVersion)
}

def putToBinariRepo(source, profileStr, optional, projectVersion) {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "repo-binarios", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        //subir scripts
        command = "\"C:\\Program Files (x86)\\WinSCP\\WinSCP.com\" " +
                "/log=\"WinSCP.log\" " +
                "/ini=nul /command " +
                "\"open ftpes://" + env.USERNAME + ":" + env.PASSWORD + "@repo-binarios.util.junta-andalucia.es/ " +
                "-certificate=\"\"5c:b1:a2:c8:70:f5:02:e7:b7:91:3d:d0:d2:fc:d2:5c:55:68:fe:59\"\"\" " +
                "\"put " + source + " DESA/apps/" + env.JOB_NAME + "/" + projectVersion + "/" + profileStr + "\" " +
                "\"exit\""
        if (optional) command = command + "|| ver>nul"
        utilHelper.retry({
            bat command
        })
    }
}

def deployBinaries(config) {
    echo "Deploy binaries to repo"
    putToBinariRepo("SCRIPTS", true, config.PROJECT_VERSION)
    def binaries = ''
    binaries = utilHelper.defaultIfEmpty(config?.stage?.deploy?.binaryList, "*.war *.ear")
    def listBinaries = utilHelper.getCmdOutput("dir /s /b " + binaries).trim().split("\n").toList()
    String profileStr = (config.PROFILE == null) ? "" : config.PROFILE + "/"
    for (String path : listBinaries) {
        echo path
        putToBinariRepo(path, profileStr, false, config.PROJECT_VERSION)
    }
}

def Undeploy(idcredential, url_wl, app, version) {
    script {
        println 'Suprimiendo la aplicacion ' + app + ':' + version + ' en ' + url_wl
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: idcredential, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
            sh "curl --user " + env.USERNAME + ":" + env.PASSWORD + " -X DELETE " + url_wl + "/management/wls/latest/deployments/application/id/" + app + "%23" + version + " > deployStatus"
            println readFile('deployStatus')
        }
    }
}
def DeployApp(deploy, app, version) {
    println 'Actualizando Base de datos a la version ' + app + ':' + version + ' en ' + url_WL
    writeFile file: 'scripts/build.sql', text: libraryResource("scripts/build.sql")
    writeFile file: 'scripts/create_table.sql', text: libraryResource("scripts/create_table.sql")
    writeFile file: 'scripts/gob_package.sql', text: libraryResource("scripts/gob_package.sql")
    dir('scripts') {
        UpdateDatabase(deploy.bbdd.credentialsId, "build.sql", deploy.bbdd.url, version)
    }
    println 'Desplegando la version ' + app + ':' + version + ' en ' + deploy.weblogic.url
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: deploy.weblogic.credentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        sh "curl --user " + env.USERNAME + ":" + env.PASSWORD + " -F \"model={name: '" + app + "#" + version + "', targets: [ '" + deploy.weblogic.target + "' ]}\" -F \"deployment=@target/" + deploy.weblogic.pathWar + "\" -X POST " + deploy.weblogic.url + "/management/wls/latest/deployments/application > deployStatus"
        println readFile('deployStatus')
    }
}

def UpdateDatabase(idcredential, script, oracle_db, version) {
    println 'Ejecutando script ' + script + ' con version: ' + version
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: idcredential, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        bat "D:\\app\\Administrator\\product\\12.1.0\\client_1\\sqlplus.exe " + env.USERNAME + "/" + env.PASSWORD + "@" + oracle_db + " @" + script + " " + version + " > sqlplus.log"
        println readFile('sqlplus.log')
    }
}

def DeployNuget() {
    powershell('D:\\Nuget\\nuget.exe pack @(gci */*.csproj)[0].FullName  -Prop Configuration=Release')
    bat('xcopy /Y "*.nupkg" "D:\\Klondike\\App_Data\\Packages"')
}
