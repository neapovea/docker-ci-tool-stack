import static groovy.json.JsonOutput.*

/**
 * Comprueba que un Job contiene los parametros necesarios
 * @param params parametros del job
 * @param config configuracion del job
 * @return
 */
def call(params, config) {
    echo "Comprobando parametros del Job"
    def profiles = config?.stage?.build?.profiles
    config << params

    config.SONAR = "SI"
    config.DEBUG = "NO"
    //Parametros para todos los tipos
    def parametersList = [choice(choices: 'SI\nNO', description: 'Especifica si se ejecutara Sonar o no', name: 'SONAR')]
    echo "Cargando configuracion del JOB especifica para proyectos " + config.type
    //parametros especificos de los tipos MAVEN ANT NET
    parametersList.add(choice(choices: 'NO\nSI', description: 'Especifica si se eliminara el WS completamente antes de empezar la tarea', name: 'CLEAN_WS'))

    def propertiesList = [buildDiscarder(logRotator(artifactDaysToKeepStr: '20', artifactNumToKeepStr: '6', daysToKeepStr: '', numToKeepStr: '')),
                          disableConcurrentBuilds(),
                          parameters(parametersList),
                          gitLabConnection('gitlab'),
                          pipelineTriggers([
                                  [
                                          $class                        : 'GitLabPushTrigger',
                                          triggerOnPush                 : true,
                                          triggerOnMergeRequest         : false,
                                          triggerOpenMergeRequestOnPush : "never",
                                          triggerOnNoteRequest          : false,
                                          noteRegex                     : "Jenkins please retry a build",
                                          skipWorkInProgressMergeRequest: true,
                                          ciSkip                        : false,
                                          setBuildDescription           : true,
                                          addNoteOnMergeRequest         : true,
                                          addCiMessage                  : true,
                                          addVoteOnMergeRequest         : true,
                                          acceptMergeRequestOnSuccess   : false,
                                          branchFilterType              : "NameBasedFilter",
                                          excludeBranchesSpec           : "master"
                                  ]
                          ])]
    println " "
    println "CONFIGURACION DE LA EJECUCION: "
    println prettyPrint(toJson(config))
    println " "
    properties(propertiesList)

    //Comprobacion de parametros comunes
    if (!config.DEPLOY_BINARIES ||
            !config.REF) {
        error "Faltan parametros, puede ser debido a una actualizacion del Job, por favor ejecute de nuevo el Job."
    }
    //Comprobacion de parametros especificos
    if (!config.SONAR ) {
                error "Faltan parametros, puede ser debido a una actualizacion del Job, por favor ejecute de nuevo el Job."
            }
    config.each { k, v -> sasHelper.setParam(k, v) }
}
