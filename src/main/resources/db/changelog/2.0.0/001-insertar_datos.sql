--liquibase formatted sql

-- changeset salvador:003-insertar-datos
INSERT INTO public."character"
(id, "name", height, mass, hair_color, eye_color, birth_year, gender, created_at, updated_at)
VALUES(nextval('character_id_seq'::regclass), 'Leia Organa', '150', '49', 'brown', 'brown', '19BBY', 'female', '2014-12-10T15:20:09.791', '2014-12-20T21:17:50.315');
-- rollback DELETE FROM public.CHARACTER WHERE name='Leia Organa';