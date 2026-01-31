-- Remove unique constraint on provider_name to allow multiple configurations per provider
-- This allows users to configure multiple instances of the same provider (e.g., multiple OpenAI configs with different models)

ALTER TABLE ai_configurations DROP CONSTRAINT IF EXISTS ai_configurations_provider_name_key;
