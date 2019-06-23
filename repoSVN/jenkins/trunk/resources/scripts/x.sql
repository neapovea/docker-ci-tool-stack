CREATE OR REPLACE PACKAGE BODY Gob_package AS
--   PROCEDURE gob_generate_sql(p_ver VARCHAR2) IS BEGIN
--     SELECT *
--     FROM TABLE (gob_intermediate_versions2(gob_actual_version(), p_ver));
--   END;
  PROCEDURE gob_active_version(p_ver VARCHAR2) IS
    BEGIN
      UPDATE DATABASE_VERSION dv
      SET ACTIVE = 1, REFDATE = SYSDATE
      WHERE dv.VERSION = p_ver
            AND dv.REFDATE = (SELECT MAX(dv2.REFDATE)
                               FROM DATABASE_VERSION dv2
                               WHERE dv2.VERSION = p_ver);
      -- Desactivar versiones anteriores
      UPDATE DATABASE_VERSION dv
      SET ACTIVE = 0
      WHERE ACTIVE = 2;
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
        RESULT := RESULT + ((v_i_n - v_f_n) / POWER(10000, v_cont));
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
    RETURN VARCHAR IS RESULT VARCHAR(10);
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
    END;
  FUNCTION gob_intermediate_versions(
    p_ver_actual VARCHAR2,
    p_ver_final  VARCHAR2)
    RETURN VARCHAR IS RESULT VARCHAR(4000);
    BEGIN
      SELECT LISTAGG(version || chr(10))
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
  FUNCTION gob_intermediate_versions2(
    p_ver_actual VARCHAR2,
    p_ver_final  VARCHAR2)
    RETURN GOB_TABLE_ROW_TYPE IS RESULT GOB_TABLE_ROW_TYPE;
    BEGIN
      SELECT *
      BULK COLLECT INTO RESULT
      FROM database_version
      WHERE gob_compare_version(p_ver_actual, version) < 0
      START WITH version = p_ver_final CONNECT BY NOCYCLE
        PRIOR nvl(previous, gob_previus_version(version)) = version
      ORDER BY level DESC;
      RETURN RESULT;
    END;

END;