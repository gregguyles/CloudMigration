# !/usr/bin/python
# 
# boto_example.py

import boto.ec2
import time
import sys
from regions import Region, Locations


def run_instances(region_str_id, num_instances):

	# set variables
	# regions 
	region_id = int(float(region_str_id))
	locations = Locations()
	current_region = locations.return_region(region_id)
	
	ami_image = 'ami-0568456c'
	instance_type = 't1.micro'
	running_instances = []
	running_instance_ids = []
	min_count = num_instances # number of instances to run
	max_count = num_instances

	# create a connection
	connection = boto.ec2.connect_to_region(current_region.region_name, aws_access_key_id=current_region.aws_key, aws_secret_access_key=current_region.aws_secret)

	# run an instance
	connection_reservation = connection.run_instances(ami_image, key_name=current_region.key_pair, instance_type=instance_type, security_groups=current_region.security_groups, min_count=min_count, max_count=max_count)

	# get running instances
	running_instances = connection_reservation.instances

	status = [i.state for i in running_instances]
	while all_running(status):
		status = [i.update() for i in running_instances]
		time.sleep(5)

	# it will return a list of the running instances
	return_vals = ''
	for i in running_instances:
		if return_vals is not '':
			return_vals += '\n'

		return_vals += i.id.encode('ascii') + '\n' + str(i.ip_address) 

	return return_vals

def all_running(items):
	return all(s != 'running' for s in items)

if __name__ == '__main__':
	arguments = sys.argv
	region = arguments[1]
	num_instances = arguments[2]
	print run_instances(region, num_instances)
