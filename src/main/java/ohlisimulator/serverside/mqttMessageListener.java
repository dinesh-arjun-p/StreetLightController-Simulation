package ohlisimulator.serverside;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.paho.client.mqttv3.*;


public class mqttMessageListener {
	MqttAsyncClient client;
	IMqttActionListener publishListener;
	IMqttActionListener subscribeListener;
	IMqttActionListener connectListener;
	boolean connected=false;
	public boolean connectBroker()throws Exception{
		
		String broker;
        String clientId;
        String username;
        String password;
        String topic;
		
        Properties props = new Properties();
		InputStream input = mqttMessageListener.class
                .getClassLoader()
                .getResourceAsStream("config.properties");
		try {
			props.load(input);
        	broker = props.getProperty("broker");
        	clientId = props.getProperty("clientId");
        	username = props.getProperty("username");
        	password = props.getProperty("password");
        	topic = props.getProperty("topic");
        }
        catch(Exception e) {
        	e.printStackTrace();
        	return false;
        }
        
        CountDownLatch latch = new CountDownLatch(1);

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

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setSocketFactory(sslContext.getSocketFactory());

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
            	System.out.println("Topic:"+topic);
                System.out.println("Received message: " + new String(msg.getPayload()));
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
		client.close();
		return true;
	}
}
