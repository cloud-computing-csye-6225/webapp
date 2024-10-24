#!/bin/bash


set -e

echo "Setting noninteractive environment"
export DEBIAN_FRONTEND=noninteractive
export CHECKPOINT_DISABLE=1

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

echo "Installing java dependencies"

sudo apt install openjdk-17-jdk -y

echo "**********************"

which java
echo "**********************"
echo "Initial setup complete!"
