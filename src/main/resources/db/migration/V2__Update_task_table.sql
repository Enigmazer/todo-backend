-- Migration to update task table
-- This migration handles all the changes needed for the task table
-- Uses IF NOT EXISTS to safely handle cases where columns already exist

-- 1. Add new columns with default values
ALTER TABLE task ADD COLUMN IF NOT EXISTS is_email_enabled boolean DEFAULT false NOT NULL;
ALTER TABLE task ADD COLUMN IF NOT EXISTS is_reminder_sent boolean DEFAULT false NOT NULL;

-- 2. Update is_completed column to have default value
ALTER TABLE task ALTER COLUMN is_completed SET DEFAULT false;

-- 3. Handle due_date column - make it NOT NULL
-- First, update any existing NULL values to a default date
-- Using current timestamp as default for NULL values
UPDATE task SET due_date = NOW() WHERE due_date IS NULL;

-- Then make the column NOT NULL and change precision
ALTER TABLE task ALTER COLUMN due_date SET NOT NULL;
ALTER TABLE task ALTER COLUMN due_date TYPE timestamp with time zone;