-- ============================================================
-- Script: Create the contact_management_db database
-- Run once as a sysadmin / DBA before running migrations
-- ============================================================

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'contact_management_db')
BEGIN
    CREATE DATABASE contact_management_db
    COLLATE SQL_Latin1_General_CP1_CI_AS;
END;
GO

USE contact_management_db;
GO
