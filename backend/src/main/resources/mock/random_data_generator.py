import os
import random


def generate(user_id, arr_song_id, file):
	for val in arr_song_id:
		if random.randint(0, 10) > 2:
			continue
		tmp = "INSERT INTO REVIEWS (user_id, song_id, value) VALUES (\'"  + str(user_id) + "\'"
		tmp = tmp + ", \'" + str(val) + "\', \'" + str(random.randint(1, 5)) + "\'"
		tmp = tmp + ");"
		file . write(tmp + "\n")

f = open("random_data_insert.txt", "w")


for i in range(1, 50):
	generate(i, range(1, 101), f)
