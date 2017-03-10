package today.jvm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Very simple message,- maxChar is text's char with
 * a maximum integer value.
 */
class Message {
	String text;
	char maxChar;

	public Message(String text) {
		this.text = text;
	}
}

/**
 * Inefficient message processor. Even though this may be obvious, make a flight
 * record, and inspect it carefully to find out what brings performance down.
 */
class MessageProcessor implements Runnable {
	private Message message;

	public MessageProcessor(Message m) {
		this.message = m;
	}

	@Override
	public void run() {
		for (int rep = 0; rep < 100_000; rep++) {
			char[] chars = message.text.toCharArray();
			char maxChar = 0;
			for (int i = 0; i < message.text.length(); i++) {
				char c = chars[i];
				if (c > maxChar) {
					maxChar = c;
				}
			}
			message.maxChar = maxChar;
		}
	}
}

/**
 * Demonstration of a simple Java program with inefficient code. Use
 * profiler to see what's wrong.
 * 
 * Additionally, demonstrates ExecutorService with varying fixed
 * thread pool size, and builds a neat summary table.
 * 
 * JVM options to make a record:
 * -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=dumponexit=true,filename=/home/art/recording.jfr
 * 
 * @author Arturs Licis
 *
 */
public class BadPerformanceDemo {
	public static final int MESSAGE_COUNT = 2_500;

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Warming up ...");
		runTest(1);
		System.out.println("Warm-up done. Starting tests ...\n\n");

		System.out.printf("| Nr. Threads | Time spent (s) | messages / s |\n");
		System.out.printf("|=============|================|==============|\n");

		for (int nThreads = 1; nThreads <= 10; nThreads++) {
			System.out.printf("| %11d | ", nThreads);
			long timeSpent = runTest(nThreads);
			System.out.printf("%14.1f | %12.1f |\n", timeSpent / 1000d, (double) MESSAGE_COUNT / (timeSpent) * 1000);
		}
	}

	/**
	 * Runs message processing using fixed thread pool. Waits for all
	 * messages to be processed before returning time spent in ms. 
	 * 
	 * @param nThreads - number of threads for the fixed thread pool.
	 */
	public static long runTest(int nThreads) throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		long start = System.currentTimeMillis();
		for (int i = 0; i < MESSAGE_COUNT; i++) {
			executor.submit(new MessageProcessor(new Message("The quick brown fox jumps over the lazy dog " + i)));
		}
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.MINUTES);
		long end = System.currentTimeMillis();
		return end - start;
	}
}
