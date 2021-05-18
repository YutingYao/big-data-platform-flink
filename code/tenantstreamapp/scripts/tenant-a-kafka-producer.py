from kafka import KafkaProducer
import os, logging, sys, time
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--queue', help='queue name', default='iqueue5555')
parser.add_argument('--kafka', help='kafka host', default='localhost:9092')
parser.add_argument(
    '--input_file',
    help='csv data file',
    default='../../../data/turtledata.csv'
)
parser.add_argument('--speed', help='messages per second', default='5')
args = parser.parse_args()
producer = KafkaProducer(
    bootstrap_servers=args.kafka,
    value_serializer=lambda x: x.encode('utf-8')
)
f = open(args.input_file, 'r')
count = 0
f.readline()
for line in f:
    count += 1
    print('Sending line {}'.format(count))
    producer.send(args.queue, line)
    time.sleep(1.0 / int(args.speed))
