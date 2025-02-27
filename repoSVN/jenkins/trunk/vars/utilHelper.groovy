def retry(doIt, retryCondition) {
    def retrys = 3
    def initialWaitTime = 5
    def waitTime = 30
    def retr = true
    def ex
    for (int i = 0; i < retrys && retr; i++) {
        try {
            retr = retryCondition(doIt())
            ex = null
        } catch (Exception e) {
            retr = true
            ex = e
        }
        if (retr) sleep(initialWaitTime + (i * waitTime))
    }
    if (ex != null) throw ex
}

def retry(doIt) {
    retry(doIt, { it })
}
def getProfileString(build,profile){
    def profileStr = ''
    if (build?.profileFilePath) profileStr = ' -s settings.xml '
    if (profile) profileStr += ' -P ' + profile
    return profileStr
}

def getMavenVersion(jdk) {
    String mavenVersion = 'M3'
    if (jdk == "1.6") mavenVersion = 'M3.2.5'
    return mavenVersion
}

def getAntVersion(jdk) {
    String antVersion = "Ant1.8.4"
    if (jdk == "1.8") antVersion = "Ant"
    return antVersion
}

def getCmdOutput(String cmd) {
    if (isUnix()) {
        return sh(returnStdout: true, script: '#!/bin/sh -e\n' + cmd).trim()
    } else {
        def output = bat(returnStdout: true, script: cmd).trim().split("\n").toList()
        output.removeAt(0)
        return output.join("\n")
    }
}

def setParam(paramName, paramValue) {
    //asigna parámetros a la tarea.
    List<ParameterValue> newParams = new ArrayList<>()
    newParams.add(new StringParameterValue(paramName, paramValue.toString()))
    //descomentar para ver los valores de los parámetos que carga// println newParams
    $build().addOrReplaceAction($build().getAction(ParametersAction.class).createUpdated(newParams))
}

def defaultIfEmpty(obj, byDefault) {
    def res = defaultIfNull(obj, byDefault)
    if (res == '') return byDefault
    return res
}

def defaultIfNull(obj, byDefault) {
    if (obj == null) return byDefault
    return obj
}

def lastN(String input, int n){
  return n > input?.size() ? null : n ? input[-n..-1] : ''
}


/**
 * extra la lista de versiones de git y llama a {@previousVersion} con la lista
 * @param version
 * @return
 */
def previousVersion(version) {
    def versions = getCmdOutput('git tag').trim().split("\n").toList()
    return previousVersion(version, versions)
}
def zipWS(name, version) {
    zip archive: true, dir: '', glob: '', zipFile: name + '.' + version + '.zip'

}

/**
 * devuelve la version posterior a la indicada de la lista de versiones que se le ha pasado
 * @param version version actual
 * @param versions versiones donde se encuentra la version posterior
 * @return version posterior a la actual
 */
def previousVersion(version, versions) {
    def versionRed = version
    def splitVersion = version.split("\\.").toList()
    if (splitVersion.size() > 3) versionRed = splitVersion.subList(0, 3).join(".")
    def previousVersions = versions.findAll {
        return compareVersions(it, versionRed) < 0 && it.split("\\.").toList().size() > 3 && it.matches("[.0-9]+")
    }
    if (previousVersions.size() == 0) return ''
    def previousVersion = mostRecentVersion(previousVersions)
    return previousVersion
}

@NonCPS
def compareVersions(a, b) {
    List verA = a.tokenize('.')
    List verB = b.tokenize('.')

    def commonIndices = Math.min(verA.size(), verB.size())

    for (int i = 0; i < commonIndices; ++i) {
        if (!verA[i].isInteger() || !verB[i].isInteger()) return verA[i] <=> verB[i]
        def numA = verA[i].toInteger()
        def numB = verB[i].toInteger()

        if (numA != numB) {
            return numA <=> numB
        }
    }

    // If we got this far then all the common indices are identical, so whichever version is longer must be more recent
    return verA.size() <=> verB.size()
}

@NonCPS
def mostRecentVersion(versions) {
    versions.sort { a, b -> compareVersions(a, b) }
    return versions[-1]
}

@NonCPS
def readXml(String text) {
    return new XmlParser().parseText(text)
}
