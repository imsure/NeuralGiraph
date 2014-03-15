#!/usr/bin/env python

"""
Generate 1000 numbers randomly from 1 ~ 100,000 in which
800 are ranged from 1 ~ 80,000 and 200 are from 80,001 ~ 100,000.
"""

from random import randint

f = open('random_1000.txt', 'w')

for i in range(0, 800):
    f.write(str(randint(1, 80000)))
    f.write('\n')

for i in range(0, 200):
    f.write(str(randint(80000, 100000)))
    f.write('\n')