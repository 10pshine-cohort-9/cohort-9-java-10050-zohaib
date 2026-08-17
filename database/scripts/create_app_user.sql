-- ============================================================
-- Script: Create a least-privilege SQL Server login for the app
-- Replace <StrongPassword> with a real password before running
-- ============================================================

USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'contact_app_user')
BEGIN
    CREATE LOGIN contact_app_user
    WITH PASSWORD = '<StrongPassword>';
END;
GO

USE contact_management_db;
GO

IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'contact_app_user')
BEGIN
    CREATE USER contact_app_user FOR LOGIN contact_app_user;
END;
GO

-- Grant only the permissions the application needs
GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA::dbo TO contact_app_user;
GO
