#!/bin/bash
##################################################

echo "Configuring custodian"
cp -r /usr/share/viridiansoftware/install/custodian/build/libs/* /opt/linux-graph/bin
cp -r /usr/share/viridiansoftware/install/custodian/config/* /etc/linux-graph

echo "Cleaning up after install"
rm -rf /usr/share/viridiansoftware/install

echo "Done"