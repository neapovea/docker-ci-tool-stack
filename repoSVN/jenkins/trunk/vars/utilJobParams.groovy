import static groovy.json.JsonOutput.*

/**
 * Comprueba que la tarea tiene los parámetros que necesita.
 * @param params parametros de la tarea
 * @param config configuracion de la tarea
 */

def call(params, config) {
    echo "Validar parámetros de la Tarea"
    def profiles = config?.stage?.build?.profiles
    config << params

    config.SONAR = "SI"
    config.DEBUG = "NO"

    echo "Cargando configuracion de la tarea "


    //carga parámetro de lista de versiones del respositorio SVN. la seleccioar se guarda en la variable env.SVN_TAG_TO_BUILD
    def parametersList = [
      [$class: 'ListSubversionTagsParameterDefinition',
            name: 'SVN_TAG_TO_BUILD', 
            tagsDir: config.urlSVN, 
            //carga filtro de ramas y rama por defecto indicado en la configuración especifica de la tarea si es nulo carga el que se indico por defecto
            tagsFilter: utilHelper.defaultIfNull(config.tagSVN, defConfig.get("jenkins.tagSVN")),
            defaultValue: utilHelper.defaultIfNull(config.tagSVN, defConfig.get("jenkins.tagDefaultSVN")),
            //cargar credencial indicado en el fichero de configuración global.
            credentialsId: defConfig.get("jenkins.SVNLoginID"), 
            maxTags: '50', 
            reverseByDate: true, 
            reverseByName: false],
        choice(choices: 'NO\nSI', description: '', name: 'DEPLOY_BINARIES')
    ]

    //carga de más parámetros
    parametersList.add(choice(choices: 'SI\nNO', description: 'Especifica si se ejecutara Sonar o no', name: 'SONAR'))
    parametersList.add(choice(choices: 'NO\nSI', description: 'Especifica si se eliminara el WS completamente antes de empezar la tarea', name: 'CLEAN_WS'))
    parametersList.add(string(defaultValue: '', description: 'Fecha que establecera Sonar como fecha de Analisis. FORMATO yyyy-MM-dd', name: 'PROJECT_DATE'))
    parametersList.add(string(defaultValue: '', description: 'Version que establecera Sonar como version con la que comparar.', name: 'LEAK_PERIOD'))
    //cargar parámetros de forma condicional solo si están indicado en la configuración de la tarea.
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

    //carga datos propiedades para la tarea
    def propertiesList = [buildDiscarder(logRotator(artifactDaysToKeepStr: '20', artifactNumToKeepStr: '6', daysToKeepStr: '', numToKeepStr: '')),
                          disableConcurrentBuilds(),
                          parameters(parametersList)]

    //pasa la información del tag seleccionado a la variable por defecto pasa la que este establecida pro defecto
    config.SVN_TAG = utilHelper.defaultIfEmpty(env.SVN_TAG_TO_BUILD, defConfig.get("jenkins.tagDefaultSVN"))
    // if (substring(config.urlSVN.length() - 1) == "/"){
      config.SVN_BUILD_URL = config.urlSVN  + config.SVN_TAG
    // }
    // else {
    //   config.SVN_BUILD_URL = config.urlSVN  + "/" + config.SVN_TAG
    // }
    


    //muestra por el log de consola la configuración de la tarea para su ejecución.
    println "Datos de la tarea para su ejecución: "
    println prettyPrint(toJson(config))
    println " "
    properties(propertiesList)

    //Comprobacion de parámetros
    if (!config.SONAR ) {
                error "Faltan parametros, puede ser debido a una actualizacion del Job, por favor ejecute de nuevo el Job."
            }
    config.each { k, v -> utilHelper.setParam(k, v) 
      }
    echo "Fin de la carga parametors"
}
