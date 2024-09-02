INSERT INTO user_preferences (user_id)
SELECT id FROM public.user
WHERE id NOT IN (SELECT user_id FROM user_preferences);
