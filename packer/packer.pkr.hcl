packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0, <2.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "ami_name" {
  type    = string
  default = "my-custom-ami"
}

variable "source_ami" {
  type    = string
  default = "ami-0866a3c8686eaeeba"
}

variable "ami_users" {
  type    = list(string)
  default = ["816069136972"]
}

variable "ami_description" {
  type    = string
  default = "creating ami from CLI CSYE 6225"
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "instance_type" {
  type    = string
  default = "t2.small"
}


variable "subnet_id" {
  type    = string
  default = "subnet-03c85a27d9152795e"
}

# Define the builder
source "amazon-ebs" "my-ami" {
  instance_type   = var.instance_type
  region          = var.aws_region
  ami_name        = var.ami_name
  ami_description = var.ami_description
  source_ami      = var.source_ami
  ssh_username    = var.ssh_username
  subnet_id       = var.subnet_id
  ami_users       = var.ami_users


  aws_polling {
    delay_seconds = 100
    max_attempts  = 50
  }


  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/sda1"
    volume_size           = 8
    volume_type           = "gp2"
  }

  source_ami_filter {
    filters = {
      "virtualization-type" = "hvm",
      "name"                = "*ubuntu-bionic-18.04-amd64-server-*",
      "root-device-type"    = "ebs"
    }
    owners      = ["099720109477"]
    most_recent = true
  }
}

# Build and provisioning block
build {
  sources = ["source.amazon-ebs.my-ami"]

  # Initial setup script
  provisioner "shell" {
    script = "${path.root}/../scripts/initial_setup.sh"
  }

  provisioner "shell" {
    script = "${path.root}/../scripts/db_setup.sh"
  }

  # Ensure csye6225 user exists
  provisioner "shell" {
    inline = [
      "sudo useradd -m -s /usr/sbin/nologin csye6225 || true"
    ]
  }

  # Create necessary directories and set permissions
  provisioner "shell" {
    inline = [
      "sudo mkdir -p /home/csye6225",
      "sudo chown csye6225:csye6225 /home/csye6225",
      "sudo mkdir -p /opt/webapp",
      "sudo chown csye6225:csye6225 /opt/webapp"
    ]
  }

  # Upload the JAR file to /tmp first
  provisioner "file" {
    source      = "${path.root}/../target/webapp-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/webapp-0.0.1-SNAPSHOT.jar"
    timeout     = "2m"
  }

  # Move the JAR file from /tmp to /opt/webapp
  provisioner "shell" {
    inline = [
      "sudo mv /tmp/webapp-0.0.1-SNAPSHOT.jar /opt/webapp/webapp-0.0.1-SNAPSHOT.jar",
      "sudo chown csye6225:csye6225 /opt/webapp/webapp-0.0.1-SNAPSHOT.jar",
      "sudo chmod 755 /opt/webapp/webapp-0.0.1-SNAPSHOT.jar"
    ]
  }

  # Upload the application.properties file to /tmp first
  provisioner "file" {
    source      = "${path.root}/../src/main/resources/application.properties"
    destination = "/tmp/application.properties"
  }

  # Move the application.properties file from /tmp to /opt/webapp
  provisioner "shell" {
    inline = [
      "sudo mv /tmp/application.properties /opt/webapp/application.properties",
      "sudo chown csye6225:csye6225 /opt/webapp/application.properties",
      "sudo chmod 755 /opt/webapp/application.properties"
    ]
  }

  # Upload the systemd service file to /tmp first
  provisioner "file" {
    source      = "${path.root}/../scripts/webapp.service"
    destination = "/tmp/webapp.service"
  }

  # Move the service file from /tmp to /etc/systemd/system
  provisioner "shell" {
    inline = [
      "sudo mv /tmp/webapp.service /etc/systemd/system/webapp.service",
      "sudo chown root:root /etc/systemd/system/webapp.service",
      "sudo chmod 644 /etc/systemd/system/webapp.service"
    ]
  }

  # Reload systemd and start the web app service
  provisioner "shell" {
    inline = [
      "sudo systemctl daemon-reload",
      "sudo systemctl enable webapp.service",
      "sudo systemctl start webapp.service"
    ]
  }
}
