#!/bin/bash


if command -v yum &> /dev/null; then

  sudo yum install -y amazon-cloudwatch-agent
elif command -v apt-get &> /dev/null; then

  sudo apt-get update -y
  sudo apt-get install -y amazon-cloudwatch-agent
fi


sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc


sudo mv /tmp/cloudwatch_config.json /opt/aws/amazon-cloudwatch-agent/etc/cloudwatch_config.json


sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/etc/cloudwatch_config.json -s

sudo systemctl enable amazon-cloudwatch-agent
