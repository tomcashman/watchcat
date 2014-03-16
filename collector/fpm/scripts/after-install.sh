#!/bin/bash
##################################################

echo "Installing service wrapper"
cp -r /usr/share/viridiansoftware/install/custodian-collector/fpm/wrapper/*.tar.gz /opt/custodian/wrapper
MACHINE_TYPE=`uname -m`
if [ ${MACHINE_TYPE} == 'x86_64' ]; then
# 64-bit
tar zxvf wrapper-linux-x86-64-3.2.3.tar.gz
else
# 32-bit
tar zxvf wrapper-linux-x86-32-3.2.3.tar.gz
fi
cp /usr/share/viridiansoftware/install/custodian-collector/fpm/wrapper/*.conf /opt/custodian/collector

echo "Configuring custodian"
cp -r /usr/share/viridiansoftware/install/custodian-collector/build/libs/* /opt/custodian/collector/bin
cp -r /usr/share/viridiansoftware/install/custodian-collector/fpm/etc/* /etc/custodian
cp /usr/share/viridiansoftware/install/custodian-collector/fpm/init.d/custodian-collector /etc/init.d/custodian-collector

chmod +x /etc/init.d/custodian-collector

echo "Cleaning up after install"
rm -rf /usr/share/viridiansoftware/install

echo "Done"