# !/usr/bin/python
# 
# boto_example.py

import boto.ec2
import time
import sys
from regions import Region, Locations


def shutdown_instances(region_str_id, instances):

	# set variables
	# regions 
	region_id = int(float(region_str_id))
	locations = Locations()
	current_region = locations.return_region(region_id)
	
	# create a connection
	connection = boto.ec2.connect_to_region(current_region.region_name, aws_access_key_id=current_region.aws_key, aws_secret_access_key=current_region.aws_secret)

	# stop given instances
	# it will return a list of the stopped instances
	stopped_instances = connection.stop_instances(instances) 
	stopped_instance_ids = [i.id.encode('ascii') for i in stopped_instances]
	return stopped_instance_ids

if __name__ == '__main__':
	arguments = sys.argv
	region = arguments[1]
	instances = []

	for index in range(2, len(arguments)):
		instances.append(arguments[index])

	print shutdown_instances(region, instances)
