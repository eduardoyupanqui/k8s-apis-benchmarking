CREATE DATABASE mydb;
\c mydb
CREATE TABLE public.go_image (
	id uuid NOT NULL,
	lastmodified date NOT NULL
);