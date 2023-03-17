#!/bin/sh

lein codox
python3 FunctionsToMD.py
python3 HTMLFix.py
