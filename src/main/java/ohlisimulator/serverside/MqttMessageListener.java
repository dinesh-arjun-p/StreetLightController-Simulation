package ohlisimulator.serverside;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import ohlisimulator.controller.DataScheduler;
import ohlisimulator.dao.Dao;
import ohlisimulator.dao.DragonFly;
import ohlisimulator.vendor.Bosun;
import ohlisimulator.vendor.Vendor;



public class MqttMessageListener {
	MqttAsyncClient client;
	IMqttActionListener publishListener;
	IMqttActionListener subscribeListener;
	IMqttActionListener connectListener;
	private static volatile MqttMessageListener listener;
	public static MqttMessageListener getListener(){
		if(listener==null) {
			synchronized(MqttMessageListener.class){
				if(listener==null)
					listener=new MqttMessageListener();
			}
		}
		return listener;
	}
	
	int cores = Runtime.getRuntime().availableProcessors();
	ForkJoinPool pool=new ForkJoinPool(cores-3);	
	ThreadLocal<Bosun> threadBosun =
	        ThreadLocal.withInitial(() -> new Bosun());
	boolean connected=false;
	Thread discoveryRetrySchedulerThread;
	
	DataScheduler  dataScheduler;
	
	
	public void setDataScheduler(DataScheduler dataScheduler) {
		this.dataScheduler=dataScheduler;
	}
	
	public boolean connectBroker()throws Exception{
		
		String broker;
        String clientId;
        String username;
        String password;
        String topic;
        String ssl;
		
        Properties props = new Properties();
		InputStream input = MqttMessageListener.class
                .getClassLoader()
                .getResourceAsStream("config.properties");
		try {
			props.load(input);
        	broker = props.getProperty("broker3");
        	clientId = props.getProperty("clientId");
        	username = props.getProperty("username");
        	password = props.getProperty("password");
        	topic = props.getProperty("topic");
        	ssl=props.getProperty("ssl");
        }
        catch(Exception e) {
        	e.printStackTrace();
        	return false;
        }
        
        CountDownLatch latch = new CountDownLatch(1);
        
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
      
        if(ssl.equals("yes")) {
        FileInputStream fis = new FileInputStream("/etc/mosquitto/certs/ca.crt");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(fis);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("ca-cert", caCert);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        options.setSocketFactory(sslContext.getSocketFactory());
        }

       

       client = new MqttAsyncClient(broker,
    	        clientId + "-" + System.currentTimeMillis());
       
       
       
        // ---------------- Callback for messages and connection events ----------------
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection lost:");
                cause.printStackTrace();

            }

            @Override
            public void messageArrived(String topic, MqttMessage msg) throws Exception {
            	String message =new String(msg.getPayload());

            	System.out.println("Message Arrived:"+message);
            	pool.execute(() -> {
            		System.out.println("Executing");
            	    Vendor vendor = threadBosun.get();
            	    MessageTask msgTask =new MessageTask(vendor, topic, msg);
            	    msgTask.process();
            	});
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("Delivery complete for message: " + token.getMessageId());
            }
        });

        // ---------------- Publish listener ----------------
        publishListener = new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken token) {
                System.out.println("Message published successfully!");
                // Optionally, release latch if this is the last operation
                // latch.countDown();
            }

            @Override
            public void onFailure(IMqttToken token, Throwable exception) {
                System.out.println("Failed to publish message: " + exception.getMessage());
                latch.countDown();
            }
        };

        // ---------------- Subscribe listener ----------------
        subscribeListener = new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken token) {
                System.out.println("Subscribed successfully to topic: " + topic);

                // Publish after subscription is successful
                MqttMessage message = new MqttMessage("From Java Async".getBytes());
                message.setQos(0);
                message.setRetained(false);

                
            }

            @Override
            public void onFailure(IMqttToken token, Throwable exception) {
                System.out.println("Failed to subscribe: " + exception.getMessage());
                latch.countDown();
            }
        };
        
        // ---------------- Connect listener ----------------
        connectListener = new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken token) {
                System.out.println("Connected successfully!");
                try {
                    client.subscribe(topic, 0, null, subscribeListener);
                } catch (MqttException e) {
                    e.printStackTrace();
                     
                }finally {
                	connected=true;
                	latch.countDown();
                }
            }

            @Override
            public void onFailure(IMqttToken token, Throwable exception) {
                System.out.println("Failed to connect: " + exception.getMessage());
                connected=false;
                latch.countDown();
            }
        };

        // ---------------- Connect asynchronously ----------------
        client.connect(options, null, connectListener);

        
        latch.await();
        
        return connected;
	}
	
	public boolean closeClient() throws Exception{
		 if (client != null && client.isConnected()) {

		        IMqttToken token = client.disconnect();
		        token.waitForCompletion(); 

		        System.out.println("Disconnected successfully");
		    }

		    client.close();
		    System.out.println("Client closed");
		    Dao dao=new DragonFly();
		    dao.clearDatabase();
//		    discoveryRetryScheduler.shutdown();
		    dataScheduler.shutdown();
		
		    
		    return true;
	}
	
	public void publishMessage (JSONObject publish,String deviceId) throws Exception{
		String topic ="BS_Dev/"+deviceId;
		String payload = publish.toString();

		MqttMessage message = new MqttMessage(payload.getBytes());
		message.setQos(0);
		message.setRetained(false);
		
		
		client.publish(topic,message,null,publishListener);
	}


	
	
}
