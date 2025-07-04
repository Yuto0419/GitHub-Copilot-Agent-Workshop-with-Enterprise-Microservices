-- Initialization script for Payment Cart Service database
-- This script sets up the database with proper encoding and initial configuration

-- Set timezone
SET timezone = 'Asia/Tokyo';

-- Create necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create a function to update timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE skishop_payment TO postgres;

-- Log the initialization
DO $$
BEGIN
    RAISE NOTICE 'Payment Cart Service database initialized successfully at %', NOW();
END $$;
