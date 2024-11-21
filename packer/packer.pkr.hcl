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
  ami_name        = "${var.ami_name}-${formatdate("YYYY_MM_DD-HH_mm", timestamp())}"
  ami_description = var.ami_description
  instance_type   = var.instance_type
  region          = var.aws_region
  source_ami      = var.source_ami
  ssh_username    = var.ssh_username
  subnet_id       = var.subnet_id


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

}

# Build and provisioning block
build {
  sources = ["source.amazon-ebs.my-ami"]

  # Initial setup script
  provisioner "shell" {
    script = "${path.root}/../scripts/initial_setup.sh"
  }


  # Ensure csye6225 user exists
  provisioner "shell" {
    inline = [
      "sudo mkdir -p \"/opt/webapp\"",
      "sudo groupadd csye6225 || true",
      "sudo useradd --system -s /usr/sbin/nologin -g csye6225 csye6225 || true",
    ]
  }

  # Create necessary permissions and ownership
  provisioner "shell" {
    inline = [
      "sudo chown -R csye6225:csye6225 /opt/webapp",
      "sudo chmod -R 755 /opt/webapp"
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
      "sudo mv /tmp/application.properties /opt/webapp/application.properties"
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
      "sudo mv /tmp/webapp.service /etc/systemd/system/webapp.service"
    ]
  }

  # Install the Amazon CloudWatch Agent
  provisioner "shell" {
    inline = [
      "wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb",
      "sudo dpkg -i amazon-cloudwatch-agent.deb",
      "sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/bin"
    ]
  }


  # Upload the CloudWatch agent configuration file to /tmp first
  provisioner "file" {
    source      = "${path.root}/../infrastructure/aws/cloudwatch_config.json"
    destination = "/tmp/cloudwatch_config.json"
  }

  # Move the CloudWatch configuration file from /tmp to /opt
  provisioner "shell" {
    inline = [
      "sudo mv /tmp/cloudwatch_config.json /opt/cloudwatch_config.json",
      "sudo chmod 644 /opt/cloudwatch_config.json"
    ]
  }

  # Start and enable the CloudWatch agent with the specified configuration
  provisioner "shell" {
    inline = [
      "sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/cloudwatch_config.json -s",
      "sudo systemctl enable amazon-cloudwatch-agent.service",
      "sudo systemctl start amazon-cloudwatch-agent.service"
    ]
  }


  # Reload systemd and start the web app service
  provisioner "shell" {
    inline = [
      "sudo systemctl daemon-reload",
      "sudo systemctl enable webapp",
      "sudo systemctl start webapp"
    ]
  }

  post-processor "manifest" {
    output = "manifest.json"
    strip_path = true
  }
  post-processor "shell-local" {
    inline = [
      "ami_id=$(jq -r '.builds[-1].artifact_id' manifest.json | cut -d':' -f2 | cut -d',' -f1)",
      "echo $ami_id > output-ami-id.txt"
    ]
  }
}
