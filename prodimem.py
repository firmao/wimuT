#!/usr/bin/env python
"""

ProDiMem - Processor, disk, memory monitor for computations.

Usage:
    python prodimem.py [PID] [SECONDS]

Author:
    Tommaso Soru <tom@tommaso-soru.it>

"""
import sys
import subprocess
import shlex
import re
import os
import sched
import time
import datetime

PID = sys.argv[1]
SECS = int(sys.argv[2])
CWD = os.getcwd() # get current directory

s = sched.scheduler(time.time, time.sleep)

def parse_ps_aux(pid):
    process = subprocess.Popen(shlex.split("ps aux"), stdout=subprocess.PIPE)
    while True:
        output = process.stdout.readline()
        if output == '' and process.poll() is not None:
            break
        if output:
            output = re.sub(r"[ ]+", " ", output)[:-1].split(" ")
            if output[1] == pid:
                # print output
                process.poll()
                return [output[2], output[3]]
    process.poll()
    return ["N/A", "N/A"]

def parse_df(pid):
    process = subprocess.Popen(shlex.split("df"), stdout=subprocess.PIPE)
    possible_disk = None
    heuristic = 0
    while True:
        output = process.stdout.readline()
        if output == '' and process.poll() is not None:
            break
        if output:
            output = re.sub(r"[ ]+", " ", output)[:-1].split(" ")
            # print output
            if CWD.startswith(output[-1]):
                if len(output[-1]) > heuristic:
                    possible_disk = output
                    heuristic = len(output[-1])
    # print possible_disk
    process.poll()
    if possible_disk is not None:
        return possible_disk[2]
    else:
        return "N/A"

def do_something(sc): 
    st = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
    ps_aux = parse_ps_aux(PID)
    df = parse_df(PID)
    print "{}\t{}\t{}\t{}".format(st, ps_aux[0], ps_aux[1], df)
    sys.stdout.flush()
    s.enter(SECS, 1, do_something, (sc,))

print "timestamp\tperc_cpu\tperc_ram\tdisk_size".format()
s.enter(SECS, 1, do_something, (s,))
s.run()
