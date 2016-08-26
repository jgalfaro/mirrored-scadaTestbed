#!/bin/bash

if [ `sudo timeout 1 whoami` != 'root' ]; then
echo "Run with sudo privileges, to fix as root add this line to /etc/sudoers"
echo "`whoami` ALL=(ALL) NOPASSWD: ALL"
exit 1
fi
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CONF="test.conf"

conffile=$(cat $DIR/$CONF)
function getParameter {
	echo "$conffile" | grep -v \# | grep "$1=" | cut -d'=' -f2 | awk '{print $1}'
}

function errorsIn {
	logpath=$1
	errors=$(grep "AUTOTEST FAILURE" $logpath | wc -l)
	echo $errors
}

function hardErrorsIn {
	logpath=$1
	errors=$(grep "HARDWARE_ERROR" $logpath | wc -l)
	echo $errors
}

LF=$(getParameter logfilename)
LF=$DIR/logs/$LF.log

ping -c 2 `getParameter rtuip`
ping -c 2 `getParameter controllerip`
ping -c 2 `getParameter carip`

timeout 10 ssh -q -i $DIR/keys/id_rsa -o "StrictHostKeyChecking no" `getParameter controlleruser`@`getParameter controllerip` "uptime"
if [ $? -eq 0 ]; then echo "CONTROLLER HOST UP"; else echo "CONTROLLER HOST DOWN"; exit 0; fi
timeout 10 ssh -q -i $DIR/keys/id_rsa -o "StrictHostKeyChecking no" `getParameter rtuuser`@`getParameter rtuip` "uptime"
if [ $? -eq 0 ]; then echo "RTU HOST UP"; else echo "RTU HOST DOWN"; exit 0; fi
timeout 10 ssh -oKexAlgorithms=+diffie-hellman-group1-sha1 -q -i $DIR/keys/id_rsa -o "StrictHostKeyChecking no" `getParameter caruser`@`getParameter carip` "uptime"
if [ $? -eq 0 ]; then echo "PLC HOST UP"; else echo "PLC HOST DOWN IP `getParameter carip`"; exit 0; fi

iterations=$(getParameter attackrounds)
ADIR=$(getParameter attackdir)
echo "`date` -- Starting `getParameter attackname` with $iterations rounds all PLC, RTU and HMI are up and manageable" | tee -a $LF
i=1
RL=$(getParameter rtulog)
RL=$DIR/logs/$RL
CL=$(getParameter controllerlog)
CL=$DIR/logs/$CL
AL=$(getParameter attlog)
AL=$DIR/logs/$AL
CUSER=$(getParameter controlleruser)
CIP=$(getParameter controllerip)
CDIR=$(getParameter controlprojectdir)
ANAME=$(getParameter attackname)
timeout 10 ssh -q -i $DIR/keys/id_rsa -o "StrictHostKeyChecking no" `getParameter controlleruser`@`getParameter controllerip` "~/automatizedtesting/remotecompile.sh"
while [ $i -le $iterations ]; do
	echo "`date` -- Starting round $i" | tee -a $LF
	
	ping -c 1 `getParameter rtuip`
	ping -c 1 `getParameter controllerip`
	ping -c 1 `getParameter carip`
	ssh -q -i $DIR/keys/id_rsa -o "StrictHostKeyChecking no" `getParameter rtuuser`@`getParameter rtuip` "~/automatizedtesting/remotereset.sh `getParameter carip`" 2>&1 | tee $RL$i.log &
	sleep 5
	ssh -q -i $DIR/keys/id_rsa -o "StrictHostKeyChecking no" `getParameter controlleruser`@`getParameter controllerip` "~/automatizedtesting/remoterun.sh `getParameter carip` `getParameter attackname`.$i.log" 2>&1 | tee $CL$i.log &
	sleep 15
	if [ `hardErrorsIn $CL$i.log` -ge 1 ] || [ `errorsIn $CL$i.log` -ge 1 ] ; then
		ssh -q -i $DIR/keys/id_rsa -o "StrictHostKeyChecking no" `getParameter controlleruser`@`getParameter controllerip` "~/automatizedtesting/killjava.sh"
		echo "`date` -- Error starting the controller" | tee -a $LF
		sleep 20
		continue
	fi
	sudo $ADIR/remote_handler.sh `getParameter attackname` `getParameter attinterface` 2>&1 | tee $AL$i.log
	sleep 5
	if [ `errorsIn $RL$i.log` -ge 1 ] || [ `errorsIn $CL$i.log` -ge 1 ] || [ `errorsIn $AL$i.log` -ge 1 ] || [ `hardErrorsIn $CL$i.log` -ge 1 ] ; then
		echo "`date` -- Failure detected in round $i , repeating this round" | tee -a $LF
		failure=$(grep "AUTOTEST FAILURE" $DIR/logs/*.$i.log)
		echo "`date` -- Failure in $failure"
		ssh -q -i $DIR/keys/id_rsa -o "StrictHostKeyChecking no" `getParameter controlleruser`@`getParameter controllerip` "sudo rm $CDIR/data/$ANAME.$i.log"
	else
		ssh -q -i $DIR/keys/id_rsa -o "StrictHostKeyChecking no" `getParameter controlleruser`@`getParameter controllerip` "~/automatizedtesting/killjava.sh"
		echo "`date` -- Succesful round $i , storing data" | tee -a $LF
		echo "Executing: scp -i keys/id_rsa -o "StrictHostKeyChecking no" $CUSER@$CIP:$CDIR/data/$ANAME.$i.log $DIR/logs/ ................................"
		scp -i keys/id_rsa -o "StrictHostKeyChecking no" $CUSER@$CIP:$CDIR/data/$ANAME.$i.log $DIR/logs/
		ssh -q -i $DIR/keys/id_rsa -o "StrictHostKeyChecking no" `getParameter controlleruser`@`getParameter controllerip` "sudo rm $CDIR/data/$ANAME.$i.log"
		(( i++ ))
	fi
	sleep 10
done
