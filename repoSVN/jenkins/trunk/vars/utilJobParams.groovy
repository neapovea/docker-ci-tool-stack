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


// parameterized build settings (with parameter type of "list subversion tags"): 
// Name: branch 
// Repository URL: svn://vcs.local/main/projectA 
// Tags filter: trunk|(tags|branches)/.* 
// Default value: trunk 

    echo "Cargando configuracion del JOB especifica para proyectos " + config.type

    def parametersList = [
      [$class: 'ListSubversionTagsParameterDefinition',
            name: 'SVN_TAG_TO_BUILD', 
            tagsDir: config.urlSVN, 
            tagsFilter: config.tagSVN,
            defaultValue: config.tagSVN,
            credentialsId: defConfig.get("jenkins.SVNLoginID"), 
            maxTags: '50', 
            reverseByDate: true, 
            reverseByName: false]   
    ]

    
    //parametros especificos de los tipos MAVEN 
    parametersList.add(choice(choices: 'SI\nNO', description: 'Especifica si se ejecutara Sonar o no', name: 'SONAR'))
    parametersList.add(choice(choices: 'NO\nSI', description: 'Especifica si se eliminara el WS completamente antes de empezar la tarea', name: 'CLEAN_WS'))
    parametersList.add(string(defaultValue: '', description: 'Fecha que establecera Sonar como fecha de Analisis. FORMATO yyyy-MM-dd', name: 'PROJECT_DATE'))
    parametersList.add(string(defaultValue: '', description: 'Version que establecera Sonar como version con la que comparar.', name: 'LEAK_PERIOD'))
    if (config?.stage?.deploy?.artifactory == 'SI') {
        parametersList.add(choice(choices: 'NO\nSI', description: '', name: 'DEPLOY_LIBRARY'))
    }
    if (config?.stage?.deploy?.pre) {
        parametersList.add(choice(choices: 'NO\nSI', description: '', name: 'DEPLOY_PRE'))
    }
    if (config?.stage?.deploy?.pro) {
        parametersList.add(choice(choices: 'NO\nSI', description: '', name: 'DEPLOY_PRO'))
    }
    if (config.type == "MAVEN") {
        parametersList.add(choice(choices: 'NO\nSI', description: 'Especifica si se ejecutara maven en modo debug o no', name: 'DEBUG'))
    }

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

    // //Comprobacion de parametros comunes
    // if (!config.DEPLOY_BINARIES ||
    //         !config.REF) {
    //     error "Faltan parametros, puede ser debido a una actualizacion del Job, por favor ejecute de nuevo el Job."
    // }
    //Comprobacion de parametros especificos
    if (!config.SONAR ) {
                error "Faltan parametros, puede ser debido a una actualizacion del Job, por favor ejecute de nuevo el Job."
            }
    config.each { k, v -> utilHelper.setParam(k, v) 
      }
}
