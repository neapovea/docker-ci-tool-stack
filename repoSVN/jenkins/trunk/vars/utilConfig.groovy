def call(jobName) {
    echo "Cargando fichero de configuracion del job: " + jobName
    //Lectura del fichero de configuracion, asociado por nombre de la tarea
    def config = readJSON text: libraryResource("configuraciones/" + jobName + ".json")
    echo "Configuracion leida"
    // //asignacion de parametros por defecto
    // config.type = utilHelper.defaultIfNull(config.type, 'MAVEN')
    // config.mailNotification = utilHelper.defaultIfNull(config.mailNotification, '')
    // config.pipelineVersion = utilHelper.defaultIfNull(config.pipelineVersion, 99999)

    return config
}