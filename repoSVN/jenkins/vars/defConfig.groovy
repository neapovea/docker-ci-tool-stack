def get(key) {
    if (!Globals.GlobalConfig) {
        echo("Cargando configuración por defecto")
        Globals.GlobalConfig = readJSON text: libraryResource("config/config.json")
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
