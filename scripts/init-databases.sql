-- Database initialization script for Ski Shop Microservices
-- Creates multiple databases for different services

-- Create databases for each microservice
SELECT 'CREATE DATABASE skishop_auth' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'skishop_auth')\gexec
SELECT 'CREATE DATABASE skishop_user_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'skishop_user_db')\gexec
SELECT 'CREATE DATABASE skishop_inventory' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'skishop_inventory')\gexec
SELECT 'CREATE DATABASE skishop_sales' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'skishop_sales')\gexec
SELECT 'CREATE DATABASE skishop_payment' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'skishop_payment')\gexec
SELECT 'CREATE DATABASE skishop_point' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'skishop_point')\gexec
SELECT 'CREATE DATABASE skishop_coupon' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'skishop_coupon')\gexec

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE skishop_auth TO skishop_user;
GRANT ALL PRIVILEGES ON DATABASE skishop_user_db TO skishop_user;
GRANT ALL PRIVILEGES ON DATABASE skishop_inventory TO skishop_user;
GRANT ALL PRIVILEGES ON DATABASE skishop_sales TO skishop_user;
GRANT ALL PRIVILEGES ON DATABASE skishop_payment TO skishop_user;
GRANT ALL PRIVILEGES ON DATABASE skishop_point TO skishop_user;
GRANT ALL PRIVILEGES ON DATABASE skishop_coupon TO skishop_user;
