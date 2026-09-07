-- ============================================================
-- Script: Drop the contact_management_db database
-- WARNING: Destructive — use only in dev/test environments
-- ============================================================

USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = 'contact_management_db')
BEGIN
    ALTER DATABASE contact_management_db SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE contact_management_db;
END;
GO
