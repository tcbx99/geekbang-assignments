/*
 * Copyright (c) 2022.
 * For educational usages only.
 */

create extension if not exists pg_trgm;
create index idx_name_upper on users using gin (upper("name") gin_trgm_ops);
reindex table users;