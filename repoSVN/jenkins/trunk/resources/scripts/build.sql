WHENEVER SQLERROR EXIT SQL.SQLCODE
SET ECHO OFF
SET SERVEROUTPUT ON
--INICIO SCRIPT FULL
PROMPT Comprobando tabla de versiones
@@create_table.sql
PROMPT Comprobando paquete de utiles
@@gob_package.sql
PROMPT Cargando versiones del proveedor
@@versions.sql
PROMPT Seleccionando que scripts se deberan de ejecutar para llegar a la version objetivo
-- Ejecutar scripts del proveedor
SET VERIFY OFF
SET feedback off
SET pagesize 0
SPOOL nivelacion.sql
--SELECT 'WHENEVER OSERROR exit -1' FROM dual;
SELECT Gob_package.gob_generate_sql('&1')
FROM dual;
SPOOL off
SET feedback on
SET VERIFY ON
PROMPT Ejecutando Scripts
@nivelacion.sql
PROMPT Activando version &1
EXECUTE Gob_package.gob_active_version('&1')
PROMPT Fin de ejecucion
EXIT

