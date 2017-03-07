package com.zookeeper1;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode.Mode;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.KillSession;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

/**
 * 实例学习ZooKeeper的用法： 临时节点
 * 创建了两个节点，一个是临时节点，一个事持久化的节点。 
 * 可以看到， client重连后临时节点不存在了。
 * 
 * @author dell
 *
 */
public class PersistentEphemeralNodeExample {
    private static final String PATH = "/example/ephemeralNode";
    private static final String PATH2 = "/example/node";
    
    public static void main(String[] args) throws Exception{
		TestingServer server = new TestingServer();
		CuratorFramework client = null;
		PersistentEphemeralNode node = null;  //临时节点
		
		try{
			client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000,3));
			client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
				
				public void stateChanged(CuratorFramework client, ConnectionState newState) {
					System.out.println("client state: " + newState.name());
					
				}
			});
			client.start();
			
			node = new PersistentEphemeralNode(client,Mode.EPHEMERAL,PATH,"test".getBytes());
			node.start();
			node.waitForInitialCreate(3, TimeUnit.SECONDS);
			String actualPath = node.getActualPath();
			System.out.println("node " + actualPath + " value:" + new String(client.getData().forPath(actualPath)));
			
			client.create().forPath(PATH2,"persistent node".getBytes());
			System.out.println("node: "+ PATH2 + " value:" + new String(client.getData().forPath(PATH2)));
			KillSession.kill(client.getZookeeperClient().getZooKeeper(), server.getConnectString());
			System.out.println("node " + actualPath + " doesn't exist: " + (client.checkExists().forPath(actualPath)));
			System.out.println("node " + PATH2 + " value:" + new String(client.getData().forPath(PATH2)));
				
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			CloseableUtils.closeQuietly(node);
			CloseableUtils.closeQuietly(client);
			CloseableUtils.closeQuietly(server);
		}
	}
}
