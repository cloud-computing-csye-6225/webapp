#!/bin/bash

set +e
export DEBIAN_FRONTEND=noninteractive

# Preselect ubuntu focal for MySQL repository configuration
echo "mysql-apt-config mysql-apt-config/select-server string ubuntu focal" | sudo debconf-set-selections

# Download and install MySQL APT repository configuration
wget https://dev.mysql.com/get/mysql-apt-config_0.8.22-1_all.deb
sudo DEBIAN_FRONTEND=noninteractive dpkg -i mysql-apt-config_0.8.22-1_all.deb

# Add the MySQL GPG key
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys B7B3B788A8D3785C

# Update package list
sudo apt-get update -y

# Install MySQL Server
sudo apt-get install -y mysql-server

# Enable and start MySQL service
sudo systemctl enable mysql
sudo systemctl start mysql

# MySQL root password (ensure the environment variable MYSQL_PASSWORD is set beforehand)
MYSQL_PASSWORD="${MYSQL_PASSWORD}"

# Switch the authentication method for root to mysql_native_password and set the password
sudo mysql <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_PASSWORD}';
FLUSH PRIVILEGES;
EOF

# Test if the password change was successful by logging in with the new root credentials
if mysql -u root -p"${MYSQL_PASSWORD}" -e "exit"; then
    echo "Root password successfully changed!"
else
    echo "Failed to change root password."
    exit 1
fi

# Create a test database using the new root credentials
sudo mysql -u root -p"${MYSQL_PASSWORD}" <<EOF
CREATE DATABASE user_db;
USE user_db;
EOF

echo "Database setup complete!"
exit 0
