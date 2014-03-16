#!/bin/bash
##################################################

echo "Configuring custodian-web"
cp -r /usr/share/viridiansoftware/install/custodian/dist/* /var/www/custodian/

echo "Cleaning up after install"
rm -rf /usr/share/viridiansoftware/install

echo "Done"
