#!/bin/bash
##################################################

echo "Configuring custodian"
cp -r /usr/share/viridiansoftware/install/custodian-collector/build/libs/* /opt/custodian/collector/bin
mv /opt/custodian/collector/bin/custodian-collector*.jar /opt/custodian/collector/bin/custodian-collector.jar

cp -r /usr/share/viridiansoftware/install/custodian-collector/fpm/etc/* /etc/custodian

cp /usr/share/viridiansoftware/install/custodian-collector/fpm/init.d/custodian-collector /etc/init.d/custodian-collector
chmod +x /etc/init.d/custodian-collector

echo "Cleaning up after install"
rm -rf /usr/share/viridiansoftware/install

echo "Done"
