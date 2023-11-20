DROP TABLE IF EXISTS public.client_apartment;
DROP TABLE IF EXISTS public.apartment;
DROP TABLE IF EXISTS public.client;

CREATE TABLE IF NOT EXISTS public.apartment
(
    id uuid NOT NULL,
    price real,
    capacity bigint,
    availability boolean,
    status character varying(16),
    CONSTRAINT apartment_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.client
(
    id uuid NOT NULL,
    name character varying(32),
    status character varying(16),
    CONSTRAINT client_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.client_apartment
(
    client_id uuid NOT NULL,
    apartment_id uuid NOT NULL,
    CONSTRAINT apartment_id FOREIGN KEY (apartment_id)
        REFERENCES public.apartment (id),
    CONSTRAINT client_id FOREIGN KEY (client_id)
        REFERENCES public.client (id)
);