# big-data-platform-flink

## Synopsis

This repository contains files required to build a simple big data platform that accommodates multiple tenants. It is implemented with Apache Flink, Kafka, and Node.js and written in Python and Java.

![Preview image](big-data-platform-flink.png)

## Motivation

The goal of this project is to demonstrate how large amounts of data, either in batch or stream mode, can be ingested, processed, and stored in a distributed database. This demo version only runs locally, but the same principles apply when deploying it in a cloud environment.

## Installation

The project requires **Docker**, **Node.js**, **Python 3**, **pipenv**, **Apache Kafka**, **Apache Maven**, and **Apache Flink (flink-1.12.2-bin-scala_2.12)** for installation.

Navigate to **code**. Create a file called **secrets.env** where you write credentials in the following format:

```
# MongoDB init credentials
MONGO_INITDB_ROOT_USERNAME=root
MONGO_INITDB_ROOT_PASSWORD=password

# Mongo Express credentials
ME_CONFIG_MONGODB_ADMINUSERNAME=root
ME_CONFIG_MONGODB_ADMINPASSWORD=password

# Ingestor credentials
MONGO_USERNAME=root
MONGO_PASSWORD=password
```

Then you can setup MongoDB and Mongo Express (for accessing MongoDB):

```
$ docker-compose up -d
```

MongoDB is now accessible via Mongo Express in the browser at http://localhost:8888.

![Mongo Express](mongo-express.png)

### Setting up mysimbdp-coredms

Navigate to **code/mysimbdp-coredms**. Create a file called **.env** where you write the port number and credentials in the following format (username and password need to match the ones above):

```
PORT=3001
MONGODB_URI_TENANT_A=mongodb://root:password@localhost:27017/
```

Then you can run the following:

```
$ npm i
$ npm start
```

You should see both messages below:

```
Server running on port 3001
connected to MongoDB
```

mysimbdp-coredms is now ready to receive connections.

### Setting up Zookeeper and Kafka

In a new terminal, navigate to your local installation of Apache Kafka (Zookeeper should have installed along with it). Typically you might copy the installation binary to a directory like **~/bin/kafka** which would produce the following:

```
$ ls
bin  config  libs  LICENSE  logs  NOTICE  site-docs
```

Then you would be able to start Zookeeper:

```
$ bin/zookeeper-server-start.sh config/zookeeper.properties 
```

If you got a wall of text that looks like the following, Zookeeper is has started up correctly:

```
[2021-05-18 08:43:16,846] INFO Snapshotting: 0x0 to /tmp/zookeeper/version-2/snapshot.0 (org.apache.zookeeper.server.persistence.FileTxnSnapLog)
[2021-05-18 08:43:16,858] INFO Using checkIntervalMs=60000 maxPerMinute=10000 (org.apache.zookeeper.server.ContainerManager)
```

Likewise in a new terminal window (**~/bin/kafka**), you can now start Kafka:

```
$ bin/kafka-server-start.sh config/server.properties 
```

If you got a wall of text like above, Kafka is running correctly.

### Setting up Flink

In a new terminal, navigate to your local installation of Apache Flink. It is important that the Scala version of the Flink installation binary is 2.12. Here I have used Flink version 1.12.2 so my installation directory is **~/bin/flink-1.12.2-bin-scala_2.12**, producing the following output:

```
$ ls
bin  conf  examples  lib  LICENSE  licenses  log  NOTICE  opt  plugins  README.txt
```

Then you can start Flink:

```
$ bin/start-cluster.sh 
```

The Flink UI is now usable at http://localhost:8081.

![Apache Flink](flink.png)

### Setting up tenantstreamapp

In a new terminal window, navigate to this repository and run:

```
$ cd code/tenantstreamapp
$ mvn clean package
```

If you get the **BUILD SUCCESS** message, the JAR file to be submitted to Flink is located in **target/tenantstreamapp-0.1-SNAPSHOT.jar**. Submit it through Flink web UI from **Submit New Job** -> **Add New**. Select the JAR file, click on it in the list in Flink and the blue **Submit** button that appears. You should be directed to a page with **tenantstreamapp** and **RUNNING** in green, indicating that it is running correctly in Flink.

### Setting up DatabaseIngestor

Navigate to the root of this repository and run:

```
$ cd code/DatabaseIngestor
# The following commands use `pipenv` and `requirements.txt`
$ pipenv install
$ pipenv shell
$ python3 CoredmsIngestor.py localhost:9092 kafka-database-consumer-group-2
```

If you see a message like the following, DatabaseIngestor is running correctly:

```
2021-05-18 09:21:36,789 [MainThread  ] [INFO ]  Connected to Kafka broker
```

### Setting up tenant-a-kafka-producer

In a new terminal window, navigate to the root of this repository and run:

```
$ cd code/tenantstreamapp/scripts
# The following commands use `pipenv` and `requirements.txt`
$ pipenv install
$ pipenv shell
$ python3 tenant-a-kafka-producer.py
```

If you see messages produced like below, tenant-a-kafka-producer is running correctly:

```
Sending line 1
Sending line 2
...
```

The different available options are visible at the beginning of the code.
For example, the message producing speed can be set with `--speed` option.

### Stopping everything

After you are done testing, deconstructing the demo is simple:

* Terminal 1 (MongoDB / Mongo Express):
  * `docker-compose down`
* Terminal 2 (mysimbdp-coredms):
  * ctrl+c
* Terminal 3 (Zookeeper):
  * ctrl+c
* Terminal 4 (Kafka):
  * ctrl+c
* Terminal 5 (Flink):
  * `bin/stop-cluster.sh`
* Terminal 6 (DatabaseIngestor):
  1. Stop the ingestor first by pressing: ctrl+c
  2. Then exit the pipenv shell with the command: `exit`
  3. Then remove the pipenv environment with the command: `pipenv --rm`
* Terminal 7 (tenant-a-kafka-producer):
  1. Stop the producer first by pressing: ctrl+c
  2. Then exit the pipenv shell with the command: `exit`
  3. Then remove the pipenv environment with the command: `pipenv --rm`

Finally you can close all the terminal windows.

## Contributors

This project is intended as a demo and is therefore not open to contributions.

## License

The files in this repository are licensed under the MIT license.
