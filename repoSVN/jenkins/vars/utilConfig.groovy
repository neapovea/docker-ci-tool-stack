def call(jobName) {
    echo "Cargando fichero de configuracion del job: " + jobName
    //Lectura del fichero de configuracion, asociado por nombre de la tarea
    def config = readJSON text: libraryResource("config/" + jobName + ".json")
    echo "Configuracion leida"
    //asignacion de parametros por defecto
    config.type = sasHelper.defaultIfNull(config.type, 'MAVEN')
    config.mailNotification = sasHelper.defaultIfNull(config.mailNotification, '')
    config.pipelineVersion = sasHelper.defaultIfNull(config.pipelineVersion, 99999)

    return config
}