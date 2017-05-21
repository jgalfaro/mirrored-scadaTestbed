## Synopsis
Part of the research activities are hampered by the lack of realistic ex-
perimental environments, there is an inherent necessity of cost-effective
testbed in order to test novel theories in the SCADA security domain. As
an effort to obtain more valuable data from experimentation, a testbed was
developed to support the research of security methods and techniques in
the SCADA systems.
The purpose of this work is to expand the capabilities of a current SCADA
testbed and test security techniques that are currently undergoing research.
The architecture is based in the Lego Bricks Mindstorm and Raspberry Pi
which represent the SCADA network nodes. We are mainly focusing on
two SCADA protocols, Modbus and DNP3, the latter allows more hard-
ware compatibility, and possible security improvements which are under
implementation in the OpenDNP3 libraries.
Supervisory Control and Data Acquisition (SCADA) systems are widely
used in critical industry such as water treatment, train controlling and sig-
naling, or even electric plants. When most of the SCADA protocols were
conceived, they were targeted for dedicated networks thus reducing any
risk of network security. To reduce cost and ease the deployment and
maintenance of such systems they are now converging into using shared
networks, hence exposing different vulnerabilities that previously were not
considered.
Developing a set of adversaries was an important milestone in pursuing
providing a real-world environment where malicious threats exist. There-
fore the development of compatible attackers was expected, different at-
tackers model were useful to fully test the security techniques against dif-
ferent types of adversaries in the testbed. This report includes information
about the different attackers that were implemented. These attackers vary
in knowledge level and computational power in order to provide an in-
sight of the opponent perspective and difficulties, when using the security
features.

## Code Example

CONTROLLER <=> RTU <=> PLC

## Motivation
The purpose of this work is to expand the capabilities of a current SCADA
testbed and test security techniques that are currently undergoing research.
The architecture is based in the Lego Bricks Mindstorm and Raspberry Pi
which represent the SCADA network nodes. We are mainly focusing on
two SCADA protocols, Modbus and DNP3, the latter allows more hard-
ware compatibility, and possible security improvements which are under
implementation in the OpenDNP3 libraries.

More info available at: [LegoSCADA](http://www-public.tem-tsp.eu/~garcia_a/web/prototypes/legoscada/)
