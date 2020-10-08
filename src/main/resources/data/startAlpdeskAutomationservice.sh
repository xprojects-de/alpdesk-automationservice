#!/bin/sh
cd /home/pi/xhomeautomation
mount /dev/sda1 /media/xhomeautomationusb/
sudo mount | grep sda1
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5001 -jar Alpdesk-Automationservice-Deploy.jar /home/pi/xhomeautomation/homeautomation.properties --spring.profiles.active=prod --spring.datasource.url=jdbc:h2:file:/media/xhomeautomationusb/xhomeautomationdb_prod
