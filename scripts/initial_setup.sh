#!/bin/bash

echo "Updating package list..."
sudo apt-get update -y

echo "Upgrading packages..."
sudo apt-get upgrade -y

echo "Installing Nginx..."
sudo apt-get install -y nginx

echo "Enabling Nginx to start on boot..."
sudo systemctl enable nginx

echo "Starting Nginx service..."
sudo systemctl start nginx

echo "Cleaning up package manager cache..."
sudo apt-get clean

echo "Initial setup complete!"
