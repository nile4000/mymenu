create table if not exists integration_config (
  key text primary key,
  value_text text null,
  value_encrypted text null,
  updated_at timestamptz not null default now()
);

alter table receipt add column if not exists "External_Source" text;
alter table receipt add column if not exists "External_Receipt_Id" text;

create unique index if not exists ux_receipt_external_source_id
on receipt ("External_Source", "External_Receipt_Id")
where "External_Source" is not null and "External_Receipt_Id" is not null;
