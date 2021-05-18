/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mysimbdp;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

public class StreamingJob {

	public static void main(String[] args) throws Exception {
		// set up the streaming execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment
				.getExecutionEnvironment();
		
		String inputQueue;
		String outputQueue;
		String kafkaUrl;
		int parallelismDegree;

		final ParameterTool params = ParameterTool.fromArgs(args);
		inputQueue = params.get("iqueue", "iqueue5555");
		outputQueue = params.get("oqueue", "oqueue5555");
		kafkaUrl = params.get("kafka", "localhost:9092");
		parallelismDegree = params.getInt("parallelism", 1);

		final CheckpointingMode checkpointingMode = CheckpointingMode.EXACTLY_ONCE;
		// create a checkpoint every minute to recover from failure
		env.enableCheckpointing(1000*60, checkpointingMode);
		// use event time and watermarks instead of processing time
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		// turn the binary data in Kafka into Java objects
		SimpleStringSchema inputSchema = new SimpleStringSchema();

		// data source: Kafka
		Properties kafkaConsumerProps = new Properties();
		kafkaConsumerProps.setProperty("bootstrap.servers", kafkaUrl);
		kafkaConsumerProps.setProperty("group.id", "kafkaGroup1");

		FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(
				inputQueue,
				inputSchema,
				kafkaConsumerProps);
		kafkaConsumer.setStartFromEarliest();
		// set watermarks to make event time usable
		kafkaConsumer.assignTimestampsAndWatermarks(
				WatermarkStrategy
						.forBoundedOutOfOrderness(Duration.ofSeconds(5)));
						// handle watermarks with no events in a window
						// .withIdleness(Duration.ofMinutes(1)));

		final DataStream<String> reviewDataStream = env
				.addSource(kafkaConsumer)
				.setParallelism(parallelismDegree); // scale the input stream
		
		DataStream<String> processedReviewDataStream = reviewDataStream
				.flatMap(new TurtleDataParser())
				.keyBy(new TurtleKeySelector())
				.window(TumblingEventTimeWindows.of(Time.seconds(5)))
				.process(new MyProcessWindowFunction());

		Properties kafkaProducerProps = new Properties();
		kafkaProducerProps.setProperty("bootstrap.servers", kafkaUrl);

		// schema used for output data
		StringSerializationSchema outputSchema =
			new StringSerializationSchema(outputQueue);

    /*
     * Semantic.EXACTLY_ONCE causes the use of Kafka transactions to
     * provide exactly-once delivery guarantees. Therefore, any Kafka
     * consumers reading the transaction data must set `isolation.level`
     * setting to `read_committed` or `read_uncommitted`.
     * 
     * Additionally, `FlinkKafkaProducer` by default sets the
     * `transaction.timeout.ms` property in producer config to 1 hour.
     * It cannot be larger than the `transaction.max.timeout.ms` setting in
     * Kafka brokers which is by default 15 minutes. Therefore,
     * `transaction.max.timeout.ms` needs to be increased before using
     * Semantic.EXACTLY_ONCE here.
     */
		FlinkKafkaProducer<String> kafkaProducer = new FlinkKafkaProducer<>(
				outputQueue,
				outputSchema,
				kafkaProducerProps,
				FlinkKafkaProducer.Semantic.AT_LEAST_ONCE);

    // print output to console
    processedReviewDataStream.print().setParallelism(1);
		// send output to Kafka
		processedReviewDataStream.addSink(kafkaProducer).setParallelism(1);

		// execute program and label it in Flink UI
		env.execute("tenantstreamapp");
	}

	public static class TurtleDataParser implements FlatMapFunction<
			String,
			TurtleDataEvent> {

		@Override
		public void flatMap(
				String line,
				Collector<TurtleDataEvent> out) throws Exception {

			// parse received message
			//System.out.println("Input: " + line);
			String[] parsedMsg = line.split(",");
			/* for (String a : parsedMsg) {
				System.out.println(a);
			} */

			// filter out unneccessary fields
			/* System.out.println("parsedMsg: " + Arrays.toString(parsedMsg));
			System.out.println("parsedMsg.length: " + parsedMsg.length);
			System.out.println("parsedMsg[10]: " + parsedMsg[10]); */
			TurtleDataEvent dataEvent = new TurtleDataEvent(
					parsedMsg[10].strip(),           // dev_id
					parsedMsg[1],                    // readable_time
					Float.parseFloat(parsedMsg[2])); // acceleration
			out.collect(dataEvent);
		}
	}

	// create a KeyedStream
	public static class TurtleKeySelector implements KeySelector<
			TurtleDataEvent,
			String> {

		@Override
		public String getKey(TurtleDataEvent event) throws Exception {
			// set the key value to partition stream data
			return event.dev_id;
		}
	}

	private static class MyProcessWindowFunction extends ProcessWindowFunction<
			TurtleDataEvent, // input type
			String,          // output type
			String,          // key type
			TimeWindow> {    // window type

		@Override
		public void process(
				String dev_id,
				Context context,
				Iterable<TurtleDataEvent> dataEvents,
				Collector<String> out) {

			// report the average acceleration for each turtle during the window
			float sum = 0;
      int count = 0;
			for (TurtleDataEvent dataEvent : dataEvents) {
				sum += dataEvent.acceleration;
        count += 1;
			}
      System.out.println("Reporting the average acceleration");
      out.collect(new TurtleAccelReport(dev_id, sum / count).toJSON());
		}
	}
}
