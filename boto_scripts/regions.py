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
		virginia = Region('us-east-1', '', '','cm-key.pub',['migrate'])
		sao_paulo = Region('sa-east-1', '', '', 'cm-key.pub', ['migrate'])
		california = Region('us-west-1', '', '', 'cm-key.pub', ['migrate'])
		ireland = Region('eu-west-1', '', '', 'cm-key.pub', ['migrate'])
		self.regions = [sao_paulo, california, virginia, ireland]

	def return_region(self, index):
		return self.regions[index]

