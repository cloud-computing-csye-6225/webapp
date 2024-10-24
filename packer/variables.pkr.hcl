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