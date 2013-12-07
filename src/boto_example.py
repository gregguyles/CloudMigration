# !/usr/bin/python
# 
# boto_example.py

import boto.ec2
import time

def run_instances():

	# set variables
	# add in key
	aws_key = 'AKIAIU5VIKCKPL6JHXAQ'
	aws_secret = 'ogBb4N/DLQ5hIzCiA799lKO66JSAPEEVEFgZ3e2O'
	key_pair = 'marvel-east'
	security_groups = ['boto']
	ami_image = 'ami-0568456c'
	instance_type = 't1.micro'
	running_instances = []
	running_instance_ids = []
	min_count = 1 # number of instances to run
	max_count = 1

	# create a connection
	connection = boto.ec2.connect_to_region('us-east-1', aws_access_key_id=aws_key, aws_secret_access_key=aws_secret)
	print connection

	# run an instance
	connection_reservation = connection.run_instances(ami_image, key_name=key_pair, instance_type=instance_type, security_groups=security_groups, min_count=min_count, max_count=max_count)
	
	print connection_reservation

	# get running instances
	running_instances = connection_reservation.instances

	print 'Instances:'
	for i in running_instances:
		print i.id
		print i.state
		running_instance_ids.append(i.id)

	# Give it time to boot up so it doesn't terminate or stop before the instance is 
	# running

	status = running_instances[0].state
	while running_instances[0].state != 'running':
		time.sleep(15)
		state = running_instances[0].update()



	# stop all the instances that were associated with run_instance
	print 'Stop'
	print connection.stop_instances(running_instance_ids)

	# terminate the instances
	print 'Terminate'
	print connection.terminate_instances(running_instance_ids)

if __name__ == '__main__':
	run_instances()
