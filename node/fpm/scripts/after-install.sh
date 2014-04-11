#!/bin/bash
##################################################

echo "Configuring custodian"
cp -r /usr/share/viridiansoftware/install/watchcat-node/build/libs/* /opt/watchcat/bin
mv /opt/watchcat/bin/watchcat-node*.jar /opt/watchcat/bin/watchcat-node.jar

cp -r /usr/share/viridiansoftware/install/watchcat-node/fpm/etc/* /etc/watchcat

cp /usr/share/viridiansoftware/install/watchcat-node/fpm/init.d/watchcat /etc/init.d/watchcat
chmod +x /etc/init.d/watchcat

echo "Cleaning up after install"
rm -rf /usr/share/viridiansoftware/install

echo "Done"
