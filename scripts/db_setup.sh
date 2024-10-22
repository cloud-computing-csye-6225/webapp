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

# Log into MySQL as root and set the password
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Qwaszx12345!';"
sudo mysql -e "FLUSH PRIVILEGES;"

# Create a test database and table
sudo mysql -e "CREATE DATABASE mysql_test;"
sudo mysql -e "USE mysql_test;"
sudo mysql -e "CREATE TABLE table1 (id INT, name VARCHAR(45));"
sudo mysql -e "INSERT INTO table1 VALUES(1, 'Virat'), (2, 'Sachin'), (3, 'Dhoni'), (4, 'ABD');"
sudo mysql -e "SELECT * FROM table1;"
sudo mysql -e "EXIT;"

echo "Database setup and test complete!"
exit 0
