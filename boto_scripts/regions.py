# !/usr/bin/python
# 
# boto_example.py

import boto.ec2
import time
import sys

class Region:
	def __init__(self, region_name, aws_key, aws_secret, key_pair, security_groups):
		self.region_name = region_name
		self.aws_key = aws_key
		self.aws_secret = aws_secret
		self.key_pair = key_pair
		self.security_groups = security_groups

class Locations:
	def __init__(self):
		region_0 = Region('us-east-1', 'AKIAIU5VIKCKPL6JHXAQ','ogBb4N/DLQ5hIzCiA799lKO66JSAPEEVEFgZ3e2O','marvel-east',['boto'])
		self.regions = [region_0]

	def return_region(self, index):
		return self.regions[index]

