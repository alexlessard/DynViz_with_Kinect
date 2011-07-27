#!/bin/bash

REPLACEMENTPORT=18256
SEED=481

# Verify that the number of inputs is correct
if [ $# -ne 3 ]
then
  echo "Argument Exception: Format is `randomize_pcap.sh <input_file.pcap> <output_file.pcap> <port to replace>`"
  exit 4
fi

# Now lets make sure the tcprelay package is installed... if not, go get it... (uncomment the following line to automatically check and get it in linux flavors with apt-get)
#type -P tcprewrite &>/dev/null || { sudo apt-get install tcpreplay; }

# Set the variables
INPUT=$1
OUTPUT=$2
PORT=$3
TEMPFILE="intermediatefile.pcap"
TEMPFILE2="intermediatefile2.pcap"
TEMPFILE3="intermediatefile3.pcap"
SPLITCACHE="splitfile.cache"

# Radomize the addressing information
tcprewrite --seed=$SEED --infile=$INPUT --outfile=$TEMPFILE

# Replace the old ports with the new ones
tcprewrite --portmap=$PORT:$REPLACEMENTPORT --infile=$TEMPFILE --outfile=$TEMPFILE2

# Split the traffic into two files
tcpprep --auto=bridge --pcap=$TEMPFILE2 --cachefile=$SPLITCACHE

# Create random MAC addresses
MAC1=`(date; cat /proc/interrupts) | md5sum | sed -r 's/^(.{10}).*$/\1/; s/([0-9a-f]{2})/\1:/g; s/:$//;'`
MAC2=`(date; cat /proc/interrupts) | md5sum | sed -r 's/^(.{10}).*$/\1/; s/([0-9a-f]{2})/\1:/g; s/:$//;'`
MAC3=`(date; cat /proc/interrupts) | md5sum | sed -r 's/^(.{10}).*$/\1/; s/([0-9a-f]{2})/\1:/g; s/:$//;'`
MAC4=`(date; cat /proc/interrupts) | md5sum | sed -r 's/^(.{10}).*$/\1/; s/([0-9a-f]{2})/\1:/g; s/:$//;'`

# Replace any MAC addresses and output the file
tcprewrite --enet-dmac=$MAC1,$MAC2 --enet-smac=$MAC3,$MAC4 --cachefile=$SPLITCACHE --infile=$TEMPFILE2 --outfile=$TEMPFILE3

# Finally, remove everything except the UDP packets (no need to show the world STP)
tcpdump -r $TEMPFILE3 -w $OUTPUT -s0 udp

# Cleanup our temp files
rm $TEMPFILE
rm $TEMPFILE2
rm $TEMPFILE3
rm $SPLITCACHE

# The one tyupe of packet that remains after this (aside from UDP) is if there are any Browser packets, I removed them in mine by calling 
# tcp dump with the ip address destination (since they all went to the same destination and no other packets had that destination)
# to do the lookup on as the serach parameter and the not keyword such that all packets without that as the dest. would remain, ie: 
# tcpdump -r sample_output_with_browser.pcap -w sample_output_final.pcap -s0 not net 250.206.150.183


