'''
Created on Dec 7, 2016

@author: mkoerner
'''
from py4j.java_gateway import JavaGateway, GatewayParameters
import logging
import argparse

# set argument parser
parser = argparse.ArgumentParser(description='Process some integers.')
parser.add_argument('pdf',
                   help='path to a pdf file')
args = parser.parse_args()

#set logger level
logger = logging.getLogger("py4j")
logger.setLevel(logging.CRITICAL)
logger.addHandler(logging.StreamHandler())

gateway = JavaGateway(
gateway_parameters=GatewayParameters(
        address='127.0.0.1',
        port=25333,
        auto_close=True
        ))

runner = gateway.entry_point.getExtractionRunner()
file = gateway.jvm.java.io.File(args.pdf)
lines=runner.extractReferencesFromPdf(file)

#TODO write to data base
for line in lines:
    print(line.encode('utf-8'))

