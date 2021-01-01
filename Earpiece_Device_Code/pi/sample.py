import subprocess

subprocess.call(['sudo', 'wpa_cli', '-i', 'wlan0', 'reconfigure'])
