from kafka import KafkaConsumer
import logging
import sys
import json
import os
import requests


logFormatter = logging.Formatter(
    '%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s'
)
rootLogger = logging.getLogger('mysimbdp-coredms-ingestor')
rootLogger.setLevel(logging.DEBUG)

consoleHandler = logging.StreamHandler()
consoleHandler.setFormatter(logFormatter)
consoleHandler.setLevel(logging.DEBUG)
rootLogger.addHandler(consoleHandler)


class CoredmsIngestor:
    def __init__(self):
        pass

    def send_data_message(self, data):
        response = requests.post('http://localhost:3001/api/turtles', json = data)
        return response.json()

    def send_metadata_message(self, data):
        response = requests.post('http://localhost:3001/api/turtles/meta', json = data)
        return response.json()


class KafkaMessageConsumer:
    def __init__(self, servers, group_name):
        super().__init__()
        self.consumer = KafkaConsumer(
            bootstrap_servers = servers,
            group_id = group_name
        )
        self.consumer.subscribe(pattern = 'oqueue5555')
        rootLogger.info('Connected to Kafka broker')

    def consume(self, coredms_client):
        for message in self.consumer:
            rootLogger.info('Message Topic: %s, Partition: %d, Offset: %d' % (
                message.topic, message.partition, message.offset
            ))
            metadata = {
                'topic': message.topic,
                'partition': message.partition,
                'offset': message.offset
            }
            result = coredms_client.send_metadata_message(metadata)
            data = {
                'metadata_id': result['insertedId'],
                'topic': message.topic,
                'message': json.loads(message.value)
            }
            res = coredms_client.send_data_message(data)
            rootLogger.info(f'Sent turtle data point to mysimbdp-coredms')


kafka_brokers = sys.argv[1].split(',')
group_name = sys.argv[2]
running = True

kafka_consumer = KafkaMessageConsumer(kafka_brokers, group_name)
coredms_client = CoredmsIngestor()

kafka_consumer.consume(coredms_client)
