#!/bin/bash

if [ "$1" == "" ] || [ "$2" == "" ]; then
echo "Correct usage $0 [attackscript] [interface]"
exit 1
elif [ `sudo timeout 1 whoami` != 'root' ]; then
echo "Run with sudo privileges, to fix as root add this line to /etc/sudoers"
echo "`whoami` ALL=(ALL) NOPASSWD: ALL"
exit 1
fi

attackscript=$1
interface=$2
logfile=$3
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ ! -f $DIR/$attackscript ]; then
	echo "Attack script not in automatized folder"
	exit 1
fi
sudo arp -d 192.168.2.3
# echo "$DIR/$attackscript $interface"

sleep 2
sudo $DIR/$attackscript $interface
if [ $? -eq 0 ]; then
echo "AUTOTEST SUCCESS"
else
echo "AUTOTEST FAILURE"
fi
