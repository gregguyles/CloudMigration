# !/usr/bin/python
# 
# boto_example.py

import boto.ec2
import time
import sys
from regions import Region, Locations


def run_instances(region_str_id, instances):

	# set variables
	# regions 
	region_id = int(float(region_str_id))
	locations = Locations()
	current_region = locations.return_region(region_id)
	
	running_instances = []
	running_instance_ids = []
	

	# create a connection
	connection = boto.ec2.connect_to_region(current_region.region_name, aws_access_key_id=current_region.aws_key, aws_secret_access_key=current_region.aws_secret)

	# get running instances
	running_instances = connection.start_instances(instances)


	status = running_instances[-1].state
	while status != 'running':
		time.sleep(5)
		status = running_instances[-1].update()

	running_instances = connection.get_only_instances(instances)


	# it will return a list of the running instances
	return_vals = ''
	for i in running_instances:
		if return_vals is not '':
			return_vals += '\n'

		return_vals += i.id.encode('ascii') + '\n' + str(i.ip_address) 

	return return_vals

if __name__ == '__main__':
	arguments = sys.argv
	region = arguments[1]
	instances = arguments[2:]
	print run_instances(region, instances)
