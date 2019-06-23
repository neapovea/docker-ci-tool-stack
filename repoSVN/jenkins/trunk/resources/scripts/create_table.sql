BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE DATABASE_VERSION
  (
    VERSION  VARCHAR2(255) NOT NULL
      CONSTRAINT DATABASE_VERSION_VERSION_PK
      PRIMARY KEY,
    REFDATE  DATE,
    ACTIVE   NUMBER(1),
    PREVIOUS VARCHAR2(255)
  )';
  DBMS_OUTPUT.put_line('OK: tabla database_version creada');
  EXCEPTION
  WHEN OTHERS
  THEN
  IF SQLCODE = -955
  THEN
    DBMS_OUTPUT.put_line('OK: tabla database_version ya existe');
  ELSE
    RAISE;
  END IF;

END;
/

PROMPT Comprobando tabla de versiones ... OK
