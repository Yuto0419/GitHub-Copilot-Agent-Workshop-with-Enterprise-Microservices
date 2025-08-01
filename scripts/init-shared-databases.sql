-- Initialize databases and users for all microservices in shared PostgreSQL instance
-- This script should be run once after the shared PostgreSQL container starts
-- Connect as superuser (postgres) to execute this script

-- Create databases for each microservice
CREATE DATABASE skishop_auth;
CREATE DATABASE skishop_payment; 
CREATE DATABASE skishop_user;
CREATE DATABASE skishop_sales;
CREATE DATABASE coupon_db;
CREATE DATABASE skishop_inventory;
CREATE DATABASE skishop_points;
CREATE DATABASE skishop_ai_support;

-- Create service-specific users (if needed for isolation)
-- For development environment, we'll create users that some services expect

-- Authentication service user
CREATE USER auth_user WITH PASSWORD 'auth_password';
GRANT ALL PRIVILEGES ON DATABASE skishop_auth TO auth_user;

-- User management service user (expects skishop_user)
CREATE USER skishop_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE skishop_user TO skishop_user;

-- Payment service user
CREATE USER payment_user WITH PASSWORD 'payment_password';
GRANT ALL PRIVILEGES ON DATABASE skishop_payment TO payment_user;

-- Sales service user
CREATE USER sales_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE skishop_sales TO sales_user;

-- Coupon service user
CREATE USER coupon_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE coupon_db TO coupon_user;

-- Inventory service user
CREATE USER inventory_user WITH PASSWORD 'inventory_password';
GRANT ALL PRIVILEGES ON DATABASE skishop_inventory TO inventory_user;

-- Points service user
CREATE USER points_user WITH PASSWORD 'points_password';
GRANT ALL PRIVILEGES ON DATABASE skishop_points TO points_user;

-- AI Support service user
CREATE USER ai_support_user WITH PASSWORD 'ai_support_password';
GRANT ALL PRIVILEGES ON DATABASE skishop_ai_support TO ai_support_user;

-- Grant all privileges to postgres user (for development convenience)
GRANT ALL PRIVILEGES ON DATABASE skishop_auth TO postgres;
GRANT ALL PRIVILEGES ON DATABASE skishop_payment TO postgres;
GRANT ALL PRIVILEGES ON DATABASE skishop_user TO postgres;
GRANT ALL PRIVILEGES ON DATABASE skishop_sales TO postgres;
GRANT ALL PRIVILEGES ON DATABASE coupon_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE skishop_inventory TO postgres;
GRANT ALL PRIVILEGES ON DATABASE skishop_points TO postgres;
GRANT ALL PRIVILEGES ON DATABASE skishop_ai_support TO postgres;
