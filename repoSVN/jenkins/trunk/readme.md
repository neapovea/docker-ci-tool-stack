# Sumario

- [Descripcion](#descripcion)
- [Estructura directorios repositorio](#estructura-direcotorios-repositorio)


# Descripcion
[⇑](#sumario)
Este repositorio contiene el pipeline y la configuracion de todos los proyectos de Jenkins
A continuacion una descripcion de cada una de las carpetas:

# Estructura directorios repositorio
[⇑](#sumario)

resources/config/
---
Contiene todas las configuraciones para el pipeline de cada uno de los proyectos
Los ficheros de configuracion tendran esta estructura:
```json
{
  "urlGit": "git@git.sas.junta-andalucia.es:internos/arquitectura/demoMacCD.git",
  //url del repositorio git
  "pipelineVersion": 1,
  //Version del pipeline
  "type": "MAVEN",
  //opcional,  MAVEN / ANT / NET
  "stage": {
    // configuraciones por stage
    "checkout": {
      "cleanWs": "SI"
    },
    "build": {
      //configuraciones para la etapa de construccion
      "mavenOpts": ""
      //opcional, aplica configuraciones especiales de maven
      "jdk": "1.8",
      //jdks, 1.8 1.7 1.6 ... Solo aplica a proyectos tipo MAVEN o ANT
      "filePath": "dispensaciones\\pom.xml",
      //opcional, aplica a todos los tipos, permite indicar la ruta del fichero que se usara en la compilacion
      "env": [
        "JAVA_TOOL_OPTIONS=\"-Dfile.encoding=ISO-8859-1\""
      ],
      //solo para proyectos ANT, inyecta variables de entorno en la construccion 
      "profiles": [
        //opcional, solo aplica a proyectos MAVEN, los diferentes profiles que tiene la aplicacion
        "all,pre",
        "all,prepil",
        "all,pro",
        "all,propil"
      ],
      "profileFilePath": "DYRAYA/RXXI/controlRecetas/controlrecetas.xml",
      //opcional, solo aplica a proyectos MAVEN, la ruta del fichero que contiene los profiles
      "target": "dist",
      //opcional, solo aplica a proyectos ANT, especifica el target para la compilacion ANT 
      "properties": {
        //opcional, solo aplica a proyectos ANT
        "-Dlibs.CopyLibs.classpath": "org-netbeans-modules-java-j2seproject-copylibstask.jar"
      }
      //Permite especificar parametros adicionales en la ejecucion de ANT
    },
    "testing": {
      //opcional, configuraciones para la etapa de pruebas
      "mavenOpts": ""
      //opcional, aplica configuraciones especiales de maven
      "target": "test",
      //Obligatorio, Solo aplica a proyectos ANT, especifica el target que ejecutara los test
      "codeCoverage": "JACOCO"
      //opcional, Solo aplica a proyectos MAVEN, especifica si la cobertura se realizara con JACOCO o con COBERTURA
    },
    "codeQuality": {
      //opcional, 
      "mavenOpts": ""
      //opcional, aplica configuraciones especiales de maven
      "additionalParams": {
        "-Dsonar.sources": "path a sources",
        "-Dsonar.java.binaries": "path a binarios",
        "-Dsonar.java.libraries": "path a librerias"
      }
    },
    "deploy": {
      "binaryList": [
        "binario1",
        "binario2"
      ],
      "artifactory": "SI",
      //desplegar librería en artifactory
      "bbdd": {
        "credentialsId": "MAC_OWN",
        "url": "bd-oearqdes.pre.sas.junta-andalucia.es:1521/OEARQDES"
      },
      "weblogic": {
        "credentialsId": "weblogic",
        "url": "http://10.235.72.170",
        "target": "arqAdmin",
        "pathWar": "mac.war"
      }
    }
  }
}
```


resources/libs/
---
Se ubicaran las librerias auxiliares que necesiten los proyectos para compilar en esta carpeta

resources/scripts/
---
Scripts sql para la gestion del paso entre versiones de las base de datos de los proyectos adaptados al despliegue continuo

vars/
---
contiene todos los metodos que usara el pipeline para su funcionamiento

Jenkinsfile 
---
Pipeline para la ejecucion por fases de los proyectos

