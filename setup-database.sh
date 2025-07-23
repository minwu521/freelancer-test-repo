#!/bin/bash

echo "Setting up PostgreSQL database for the application..."

# Check if PostgreSQL is installed
if ! command -v psql &> /dev/null; then
    echo "PostgreSQL is not installed. Please install it first:"
    echo "  sudo apt update"
    echo "  sudo apt install postgresql postgresql-contrib"
    exit 1
fi

# Check if PostgreSQL service is running
if ! sudo systemctl is-active --quiet postgresql; then
    echo "PostgreSQL service is not running. Starting it..."
    sudo systemctl start postgresql
fi

# Create the database
echo "Creating database 'reai'..."
sudo -u postgres psql <<EOF
-- Create database if it doesn't exist
SELECT 'CREATE DATABASE reai'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'reai')\gexec

-- Verify the database was created
\l reai
EOF

# Test the connection
echo "Testing database connection..."
PGPASSWORD=postgres psql -h localhost -U postgres -d reai -c "SELECT version();" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✓ Database 'reai' is ready!"
    echo "You can now run: ./gradlew web-app:bootRun --args='--spring.profiles.active=dev'"
else
    echo "⚠ Database created but connection test failed."
    echo "This might be due to authentication settings."
    echo ""
    echo "Try running these commands manually:"
    echo "  sudo -u postgres psql"
    echo "  ALTER USER postgres PASSWORD 'postgres';"
    echo "  \q"
fi