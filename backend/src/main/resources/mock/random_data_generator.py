import os
import random
import json


def generate(user_id, arr_song_id, file, arr):
	for song_id in arr_song_id:
		if random.randint(0, 10) > 2:
			continue
		my_dict = {
			'user_id': user_id,
			'song_id': song_id,
			'value': random.randint(1, 5)
		}
		arr . append(my_dict)

f = open("random_data.json", "w")
arr = []

for i in range(8, 51):
	generate(i, range(1, 101), f, arr)

f . write(json.dumps(arr))





'''import os
import random
import json

def generate(user_id, arr_song_id, file):
	first = True
	for song_id in arr_song_id:
		if random.randint(0, 10) > 2:
			continue
		tmp = ""
		if first:
			tmp += "\n"
			first = False
		else:
			tmp += ",\n"
		tmp += "{\n"
		tmp += "\"user_id\": " + str(user_id) + ",\n"
		tmp += "\"song_id\": " + str(song_id) + ",\n"
		tmp += "\"value\": " + str(random.randint(1, 5)) + "\n"
		tmp += "}"
		file . write(tmp)

f = open("random_data.json", "w")

f . write("[") 
for i in range(1, 50):
	generate(i, range(1, 101), f)
f . write("\n]")
'''