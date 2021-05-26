#!/bin/bash
bash <(curl -Ls https://coverage.codacy.com/get.sh) report -r ExampleApplication/jacocoReport/report.xml --commit-uuid "$1"
