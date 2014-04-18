#!/bin/bash
##################################################

echo "Configuring watchcat web frontend"
cp -r /usr/share/watchcat/install/web/dist/* /var/www/watchcat/

echo "Cleaning up after install"
rm -rf /usr/share/watchcat/install

echo "Done"
