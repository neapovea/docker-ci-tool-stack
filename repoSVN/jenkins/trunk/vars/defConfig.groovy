def get(key) {

    if (!Globals.GlobalConfig) {
        Globals.GlobalConfig = readJSON text: libraryResource("configuraciones/config.json")
        echo("Carganda configuración por defecto del entorno.")
    }

    value = getStringKey(key)
    try {
        return value[env.ENTORNO]
    } catch (MissingPropertyException e) {
        return value
    }
}

def getStringKey(key) {
    def obj = Globals.GlobalConfig
    for (k in key.split('\\.')) {
        obj = obj[k]
    }
    return obj
}

class Globals {
    static GlobalConfig
}
