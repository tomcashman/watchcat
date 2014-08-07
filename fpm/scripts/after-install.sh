#!/bin/bash
##################################################

echo "Configuring watchcat"
cp -r /usr/share/watchcat/install/node/build/libs/* /opt/watchcat/bin
mv /opt/watchcat/bin/watchcat-node*.jar /opt/watchcat/bin/watchcat-node.jar

cp -r /usr/share/watchcat/install/node/fpm/etc/* /etc/watchcat

cp /usr/share/watchcat/install/node/fpm/init.d/watchcat /etc/init.d/watchcat
chmod +x /etc/init.d/watchcat

echo "Cleaning up after install"
rm -rf /usr/share/watchcat/install

echo "Done"
