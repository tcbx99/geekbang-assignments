/*
 * Copyright (c) 2022.
 * For educational usages only.
 */
drop index idx_name;
create extension if not exists pg_trgm;
create index idx_name on users using gin ("name" gin_trgm_ops);
reindex table users;