-- Database initialization script for Ski Shop Microservices
-- Creates multiple databases for different services

-- Create databases for each microservice
CREATE DATABASE IF NOT EXISTS skishop_auth;
CREATE DATABASE IF NOT EXISTS skishop_user;
CREATE DATABASE IF NOT EXISTS skishop_inventory;
CREATE DATABASE IF NOT EXISTS skishop_sales;
CREATE DATABASE IF NOT EXISTS skishop_payment;
CREATE DATABASE IF NOT EXISTS skishop_point;
CREATE DATABASE IF NOT EXISTS skishop_coupon;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE skishop_auth TO skishop_user;
GRANT ALL PRIVILEGES ON DATABASE skishop_user TO skishop_user;
GRANT ALL PRIVILEGES ON DATABASE skishop_inventory TO skishop_user;
GRANT ALL PRIVILEGES ON DATABASE skishop_sales TO skishop_user;
GRANT ALL PRIVILEGES ON DATABASE skishop_payment TO skishop_user;
GRANT ALL PRIVILEGES ON DATABASE skishop_point TO skishop_user;
GRANT ALL PRIVILEGES ON DATABASE skishop_coupon TO skishop_user;
