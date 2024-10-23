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

variable "ami_description" {
  type    = string
  default = "creating ami from CLI"
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "instance_type" {
  type    = string
  default = "t2.small"
}

variable "vpc_id" {
  type = string
}

variable "subnet_id" {
  type = string
}

# Define the builder
source "amazon-ebs" "my-ami" {
  region          = var.aws_region
  ami_name        = var.ami_name
  ami_description = var.ami_description
  ami_regions     = ["us-east-1"]
  instance_type   = var.instance_type
  ssh_username    = var.ssh_username
  vpc_id          = var.vpc_id
  subnet_id       = var.subnet_id

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

# Provisioning steps
build {
  sources = ["source.amazon-ebs.my-ami"]

  provisioner "shell" {
    script = "${path.root}/../scripts/initial_setup.sh"
  }

  # Ensure the csye6225 user is created
  provisioner "shell" {
    inline = [
      "sudo useradd -m -s /usr/sbin/nologin csye6225 || true"
    ]
  }

  # Ensure the home directory exists and is owned by csye6225
  provisioner "shell" {
    inline = [
      "sudo mkdir -p /home/csye6225",
      "sudo chown csye6225:csye6225 /home/csye6225"
    ]
  }

  # Ensure the /opt/webapp directory exists before uploading the JAR file
  provisioner "shell" {
    inline = [
      "sudo mkdir -p /opt/webapp",
      "sudo chown csye6225:csye6225 /opt/webapp"
    ]
  }

  # Upload the JAR file to the /opt/webapp directory
  provisioner "file" {
    source      = "${path.root}/../target/webapp-0.0.1-SNAPSHOT.jar"
    destination = "/opt/webapp/webapp-0.0.1-SNAPSHOT.jar"
    timeout     = "2m"
  }

  # Set ownership and permissions for the JAR file
  provisioner "shell" {
    inline = [
      "sudo chown csye6225:csye6225 /opt/webapp/webapp-0.0.1-SNAPSHOT.jar",
      "sudo chmod 755 /opt/webapp/webapp-0.0.1-SNAPSHOT.jar"
    ]
  }

  # Upload the application.properties file to the /opt/webapp directory
  provisioner "file" {
    source      = "${path.root}/../src/main/resources/application.properties"
    destination = "/opt/webapp/application.properties"
  }

  # Set ownership and permissions for the properties file
  provisioner "shell" {
    inline = [
      "sudo chown csye6225:csye6225 /opt/webapp/application.properties",
      "sudo chmod 755 /opt/webapp/application.properties"
    ]
  }

  # Upload the webapp service file to a temporary location
  provisioner "file" {
    source      = "${path.root}/../scripts/webapp.service"
    destination = "/tmp/webapp.service"
  }

  # Move the service file to /etc/systemd/system and set ownership and permissions
  provisioner "shell" {
    inline = [
      "sudo mv /tmp/webapp.service /etc/systemd/system/webapp.service",
      "sudo chown root:root /etc/systemd/system/webapp.service",
      "sudo chmod 644 /etc/systemd/system/webapp.service"
    ]
  }

  # Reload the systemd daemon and enable/start the service
  provisioner "shell" {
    inline = [
      "sudo systemctl daemon-reload",
      "sudo systemctl enable webapp.service",
      "sudo systemctl start webapp.service"
    ]
  }

}

