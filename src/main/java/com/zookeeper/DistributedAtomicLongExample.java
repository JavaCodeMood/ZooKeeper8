package com.zookeeper;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;

import com.google.common.collect.Lists;

/**
 * zookeeper实例：Long类型的计数器
 * @author dell
 *
 */
public class DistributedAtomicLongExample {
	private static final int QTY = 5;
	private static final String PATH = "/examples/counter";

	public static void main(String[] args) throws Exception {
		TestingServer server = new TestingServer();
		try {
			CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(),
					new ExponentialBackoffRetry(1000, 3));
			client.start();

			List<DistributedAtomicLong> examples = Lists.newArrayList();
			ExecutorService service = Executors.newFixedThreadPool(QTY);
			for (int i = 0; i < QTY; ++i) {
				final DistributedAtomicLong count = new DistributedAtomicLong(client, PATH, new RetryNTimes(10, 10));

				//add()增加特定的值
				examples.add(count);
				Callable<Void> task = new Callable<Void>() {

					public Void call() throws Exception {
						try {
							//increment  加一
							AtomicValue<Long> value = count.increment();
							//返回结果的succeeded()， 它代表此操作是否成功
							System.out.println("succeed:" + value.succeeded());
							if (value.succeeded()) {
								 //如果操作成功， preValue()代表操作前的值， postValue()代表操作后的值。
								System.out.println("Increment: from " + value.preValue() + " to " + value.postValue());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				};
				service.submit(task);
			}
			service.shutdown();
			service.awaitTermination(10, TimeUnit.MINUTES);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
