--liquibase formatted sql

-- changeset salvador:002-agregar-columnas
-- preconditions onFail:HALT onError:HALT
-- precondition-sql-check expectedResult:1 select count(*) from pg_tables where tablename = 'character';
ALTER TABLE public.CHARACTER
    ADD COLUMN CREATED_AT TIMESTAMP NOT NULL;
ALTER TABLE public.CHARACTER
    ADD COLUMN UPDATED_AT TIMESTAMP NOT NULL;
-- rollback ALTER TABLE public.CHARACTER DROP COLUMN CREATED_AT;
-- rollback ALTER TABLE public.CHARACTER DROP COLUMN UPDATED_AT;