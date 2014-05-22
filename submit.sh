#!/bin/sh
cat $1 | uuencode x | mail -s "" submit@tigris.csc.kth.se
