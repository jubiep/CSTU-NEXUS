-- Remove the legacy learning_preference table.
-- Learning preferences are stored in user_preferences (see 005_user_preferences.sql),
-- which is the table read by /api/users/preferences and the Match Score calculation.
DROP TABLE IF EXISTS learning_preference;
