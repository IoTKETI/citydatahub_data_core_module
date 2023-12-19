package kr.re.keti.sc.datamanager.common.bulk;

import org.slf4j.Logger;

/**
 * Bulk processing channel interface
 * @param <T> bulk process object type
 */
public interface IBulkChannel<T> extends IBulkProducer<T> {
	/**
	 * initialize bulk channel
	 */
	public void init();

	/**
	 * start bulk channel
	 */
	public void start();

	/**
	 * stop bulk channel
	 */
	public void stop();

	/**
	 * destroy bulk channel
	 */
	public void destroy();

	/**
	 * clear data pending processing
	 * @param processRemainingData whether to process remain data upon clearing
	 */
	public void clear(boolean processRemainingData);

	/**
	 * clear data pending processing
	 * @param processRemainingData whether to process remain data upon clearing
	 * @param logger logger
	 */
	public void clear(boolean processRemainingData, Logger logger);
}