# Database

## Folder Structure

```
database/
├── migrations/          # Versioned schema changes (run in order)
│   ├── V1__create_users_table.sql
│   ├── V2__create_contacts_table.sql
│   └── V3__add_audit_trigger.sql
├── seeds/               # Dev/test data only — never run in production
│   └── seed_dev_data.sql
└── scripts/             # One-time admin scripts
    ├── create_database.sql
    ├── drop_database.sql
    └── create_app_user.sql
```

## Setup Order

1. Run `scripts/create_database.sql` as sysadmin to create the DB.
2. Run `scripts/create_app_user.sql` to create the app login.
3. Run migration scripts in version order: V1 → V2 → V3.
4. (Dev only) Run `seeds/seed_dev_data.sql` for test data.

## Notes

- Migration files follow the naming convention `V{n}__{description}.sql` (compatible with Flyway if added later).
- `drop_database.sql` is destructive — never run against production.
- Bcrypt password hashes in seed data are placeholders; replace before use.
