CREATE OR REPLACE PACKAGE Gob_package AS
  FUNCTION gob_compare_version(p_ver_inicial VARCHAR2, p_ver_final VARCHAR2)
    RETURN NUMBER;
  FUNCTION gob_is_intermediate_version(p_ver_inicial VARCHAR2, p_ver_comprobacion VARCHAR2, p_ver_final VARCHAR2)
    RETURN NUMBER;
  FUNCTION gob_actual_version
    RETURN VARCHAR;
  FUNCTION gob_previus_version(p_ver VARCHAR2)
    RETURN VARCHAR;
  FUNCTION gob_intermediate_versions(p_ver_actual VARCHAR2, p_ver_final VARCHAR2)
    RETURN VARCHAR;
  PROCEDURE gob_active_version(p_ver VARCHAR2);
  FUNCTION gob_generate_sql(p_ver VARCHAR2)
    RETURN VARCHAR;
  PROCEDURE gob_add_version(p_ver VARCHAR2, p_last_version VARCHAR2);
  PROCEDURE gob_add_version(p_ver VARCHAR2);
END;
  /


 CREATE OR REPLACE PACKAGE BODY Gob_package AS
  PROCEDURE gob_add_version(p_ver VARCHAR2) IS
    previus_version VARCHAR2(255);
    BEGIN
      previus_version := gob_previus_version(p_ver);
      gob_add_version(p_ver, previus_version);
    END;
  PROCEDURE gob_add_version(p_ver VARCHAR2, p_last_version VARCHAR2) IS BEGIN
    INSERT INTO DATABASE_VERSION (VERSION, REFDATE, ACTIVE, PREVIOUS) SELECT
                                                                        p_ver,
                                                                        SYSDATE,
                                                                        0,
                                                                        p_last_version
                                                                      FROM DUAL
                                                                      WHERE NOT EXISTS(SELECT *
                                                                                       FROM DATABASE_VERSION
                                                                                       WHERE VERSION = p_ver);
    COMMIT;
  END;
  PROCEDURE gob_active_version(p_ver VARCHAR2) IS
    BEGIN
      -- Desactivar versiones anteriores
      UPDATE DATABASE_VERSION dv
      SET ACTIVE = 0;
      UPDATE DATABASE_VERSION dv
      SET ACTIVE = 1, REFDATE = SYSDATE
      WHERE dv.VERSION = p_ver;
      COMMIT;
    END;

  FUNCTION GOB_COMPARE_VERSION(p_ver_inicial VARCHAR2, p_ver_final VARCHAR2)
    RETURN NUMBER
  IS
    v_i_n  NUMBER;
    v_f_n  NUMBER;
    v_cont NUMBER;
    RESULT NUMBER;
    BEGIN
      RESULT := 0;
      v_cont := 0;
      WHILE TRUE
      LOOP
        v_cont := v_cont + 1;
        v_i_n := TO_NUMBER(REGEXP_SUBSTR(p_ver_inicial, '[^.]+', 1, v_cont));
        v_f_n := TO_NUMBER(REGEXP_SUBSTR(p_ver_final, '[^.]+', 1, v_cont));
        EXIT WHEN v_i_n IS NULL AND v_f_n IS NULL;
        RESULT := RESULT + ((nvl(v_i_n, 0) - nvl(v_f_n, 0)) / POWER(10000, v_cont));
      END LOOP;
      RETURN RESULT;
    END;
  FUNCTION gob_is_intermediate_version(
    p_ver_inicial      VARCHAR2,
    p_ver_comprobacion VARCHAR2,
    p_ver_final        VARCHAR2
  )
    RETURN NUMBER
  IS
    RESULT NUMBER;
    BEGIN
      RESULT := 0;
      IF gob_compare_version(p_ver_inicial, p_ver_comprobacion) = -1
         AND gob_compare_version(p_ver_comprobacion, p_ver_final) = -1
      THEN
        RESULT := 1;
      END IF;
      RETURN RESULT;
    END;
  FUNCTION gob_actual_version
    RETURN VARCHAR IS RESULT VARCHAR(10);
    BEGIN
      SELECT dv.version
      INTO RESULT
      FROM database_version dv
      WHERE dv.active <> 0;
      RETURN RESULT;
    END;
  FUNCTION gob_previus_version(
    p_ver VARCHAR2)
    RETURN VARCHAR IS RESULT VARCHAR(100);
    BEGIN
      SELECT version
      INTO RESULT
      FROM (
        SELECT
          dv.version,
          gob_compare_version(p_ver, DV.version)
        FROM database_version dv
        WHERE gob_compare_version(p_ver, dv.version) > 0
        ORDER BY gob_compare_version(p_ver, DV.version))
      WHERE ROWNUM = 1;
      RETURN RESULT;
      EXCEPTION
      WHEN OTHERS THEN
      RETURN NULL;
    END;
  FUNCTION gob_intermediate_versions(
    p_ver_actual VARCHAR2,
    p_ver_final  VARCHAR2)
    RETURN VARCHAR IS RESULT VARCHAR(4000);
    BEGIN
      SELECT LISTAGG(version || '|')
      WITHIN GROUP (
        ORDER BY level DESC)
      INTO RESULT
      FROM database_version
      WHERE gob_compare_version(p_ver_actual, version) < 0
      START WITH version = p_ver_final CONNECT BY NOCYCLE
        PRIOR nvl(previous, gob_previus_version(version)) = version
      ORDER BY level DESC;
      RETURN RESULT;
    END;
  FUNCTION gob_generate_sql(p_ver VARCHAR2)
    RETURN VARCHAR
  IS
    RESULT VARCHAR(4000);
    BEGIN
      RESULT := 'PROMPT LA VERSION ACTUAL ES IGUAL QUE LA QUE SE SOLICITA INSTALAR';
      IF Gob_package.gob_compare_version(p_ver, Gob_package.gob_actual_version()) > 0
      THEN
        SELECT LISTAGG(script || chr(10))
        WITHIN GROUP (
          ORDER BY rownum)
        INTO RESULT
        FROM
          (SELECT 'PROMPT ** INSTALANDO VERSION  ' || version || ' **' || chr(10) || '@@' || version || '\version_' ||
                  version
                  || '.sql' AS script
           FROM
             (SELECT
                regexp_substr(Gob_package.gob_intermediate_versions(GOB_PACKAGE.GOB_ACTUAL_VERSION(), p_ver), '[^|]+',
                              1,
                              level) AS version
              FROM dual
              WHERE Gob_package.gob_intermediate_versions(GOB_PACKAGE.GOB_ACTUAL_VERSION(), p_ver) IS NOT NULL
              CONNECT BY
                regexp_substr(Gob_package.gob_intermediate_versions(GOB_PACKAGE.GOB_ACTUAL_VERSION(), p_ver), '[^|]+',
                              1,
                              level) IS NOT NULL));
      ELSIF Gob_package.gob_compare_version(p_ver, Gob_package.gob_actual_version()) < 0
        THEN
          SELECT LISTAGG(script || chr(10))
          WITHIN GROUP (
            ORDER BY rownum)
          INTO RESULT
          FROM
            (SELECT script
             FROM (SELECT
                     'PROMPT ** UNDO VERSION  ' || version || ' **' || chr(10) || '@@' || version || '\undo_version_' ||
                     version ||
                     '.sql' AS script,
                     rownum AS row_num
                   FROM
                     (SELECT regexp_substr(Gob_package.gob_intermediate_versions(p_ver, GOB_PACKAGE.GOB_ACTUAL_VERSION()),
                                           '[^|]+', 1,
                                           level) AS version
                      FROM dual
                      WHERE Gob_package.gob_intermediate_versions(p_ver, GOB_PACKAGE.GOB_ACTUAL_VERSION()) IS NOT NULL
                      CONNECT BY
                        regexp_substr(Gob_package.gob_intermediate_versions(p_ver, GOB_PACKAGE.GOB_ACTUAL_VERSION()),
                                      '[^|]+', 1,
                                      level) IS NOT NULL))
             ORDER BY row_num DESC);
      END IF;
      RETURN RESULT;
    END;
END;

  /

PROMPT Comprobando paquete de utiles ... OK
