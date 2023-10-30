--liquibase formatted sql

-- changeset salvador:001-crear-tabla
-- preconditions onFail:MARK_RAN onError:MARK_RAN
-- precondition-sql-check expectedResult:0 select count(*) from pg_tables where tablename = 'character';
CREATE TABLE public.CHARACTER
(
    ID SERIAL NOT NULL PRIMARY KEY,
    NAME VARCHAR(30) NOT NULL,
    HEIGHT VARCHAR(30) NOT NULL,
    MASS VARCHAR(10) NOT NULL,
    HAIR_COLOR VARCHAR(50) NOT NULL,
    EYE_COLOR VARCHAR(10) NOT NULL,
    BIRTH_YEAR VARCHAR(10) NOT NULL,
    GENDER VARCHAR(10) NOT NULL
);
-- rollback drop table public.character cascade;