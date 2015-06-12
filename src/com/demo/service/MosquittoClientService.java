package com.demo.service;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.hawtdispatch.Dispatch;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.demo.Constant;
import com.demo.mqttdemo.MainActivity;
import com.example.mqttdemo.R;

public class MosquittoClientService extends Service {
	private IBinder binder = new MosquittoClientService.LocalBinder();
	private MQTT mqtt = new MQTT();
	private CallbackConnection callbackConnection;
	private String username;

	@Override
	public void onCreate() {
		// �����������ý�岥����
		// if(mediaPlayer==null)
		// mediaPlayer=MediaPlayer.create(this, uri);
		Log.i(Constant.TAG, "onCreate");
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(Constant.TAG, "onBind");
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		username = intent.getStringExtra("username");
		Log.i(Constant.TAG, "onStartCommand:" + username);
		mqttConnect();
		return START_STICKY;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(Constant.TAG, "onStart");
		super.onStart(intent, startId);
	}

	// ����������̳�Binder
	public class LocalBinder extends Binder {
		// ���ر��ط���
		MosquittoClientService getService() {
			return MosquittoClientService.this;
		}
	}

	@Override
	public void onDestroy() {
		Log.i(Constant.TAG, "onDestroy");
		super.onDestroy();
	}

	// ����֪ͨ
	public void createInform(String message) {
		// ����һ��PendingIntent�����û����֪ͨʱ����ת��ĳ��Activity(Ҳ���Է��͹㲥��)
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent, 0);

		// ����һ��֪ͨ
		Notification notification = new Notification(R.drawable.ic_launcher,
				message, System.currentTimeMillis());
		notification.setLatestEventInfo(this, "����鿴", message, pendingIntent);

		// ��NotificationManager��notify����֪ͨ�û����ɱ�������Ϣ֪ͨ
		NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nManager.notify(100, notification);// id��Ӧ����֪ͨ��Ψһ��ʶ
		// ���ӵ����ͬid��֪ͨ�Ѿ����ύ����û�б��Ƴ����÷������ø��µ���Ϣ���滻֮ǰ��֪ͨ��
	}
	
	public void mqttConnect() {
		if(callbackConnection != null){
			return;
		}
		try {
			// MQTT����˵��
			mqtt.setHost("tcp://l-backyard1.ops.dev.cn6.qunar.com:1883");
			mqtt.setClientId(username); // �������ÿͻ��˻Ự��ID����setCleanSession(false);������ʱ��MQTT���������ø�ID�����Ӧ�ĻỰ����IDӦ����23���ַ���Ĭ�ϸ��ݱ�����ַ���˿ں�ʱ���Զ�����
			mqtt.setCleanSession(false); // ����Ϊfalse��MQTT���������־û��ͻ��˻Ự�����嶩�ĺ�ACKλ�ã�Ĭ��Ϊtrue
			mqtt.setKeepAlive((short) 60);// ����ͻ��˴�����Ϣ�����ʱ�������������������Ծݴ��ж���ͻ��˵������Ƿ��Ѿ��Ͽ����Ӷ�����TCP/IP��ʱ�ĳ�ʱ��ȴ�
			mqtt.setUserName("admin");// ��������֤�û���
			mqtt.setPassword("admin");// ��������֤����

			mqtt.setWillTopic("willTopic");// ���á���������Ϣ�Ļ��⣬���ͻ����������֮������������жϣ��������������ͻ��˵ġ���������Ϣ
			mqtt.setWillMessage("willMessage");// ���á���������Ϣ�����ݣ�Ĭ���ǳ���Ϊ�����Ϣ
			mqtt.setWillQos(QoS.AT_LEAST_ONCE);// ���á���������Ϣ��QoS��Ĭ��ΪQoS.ATMOSTONCE
			mqtt.setWillRetain(true);// ����Ҫ�ڷ�������������Ϣʱӵ��retainѡ���Ϊtrue
			mqtt.setVersion("3.1.1");

			// ʧ������������˵��
			mqtt.setConnectAttemptsMax(10L);// �ͻ����״����ӵ�������ʱ�����ӵ�������Դ����������ô����ͻ��˽����ش���-1��Ϊ���������ޣ�Ĭ��Ϊ-1
			mqtt.setReconnectAttemptsMax(3L);// �ͻ����Ѿ����ӵ�������������ĳ��ԭ�����ӶϿ�ʱ��������Դ����������ô����ͻ��˽����ش���-1��Ϊ���������ޣ�Ĭ��Ϊ-1
			mqtt.setReconnectDelay(10L);// �״������Ӽ����������Ĭ��Ϊ10ms
			mqtt.setReconnectDelayMax(30000L);// �����Ӽ����������Ĭ��Ϊ30000ms
			mqtt.setReconnectBackOffMultiplier(2);// ����������ָ���ع顣����Ϊ1��ͣ��ָ���ع飬Ĭ��Ϊ2

			// Socket����˵��
			mqtt.setReceiveBufferSize(65536);// ����socket���ջ�������С��Ĭ��Ϊ65536��64k��
			mqtt.setSendBufferSize(65536);// ����socket���ͻ�������С��Ĭ��Ϊ65536��64k��
			mqtt.setTrafficClass(8);// ���÷������ݰ�ͷ���������ͻ���������ֶΣ�Ĭ��Ϊ8����Ϊ��������󻯴���

			// ������������˵��
			mqtt.setMaxReadRate(0);// �������ӵ����������ʣ���λΪbytes/s��Ĭ��Ϊ0����������
			mqtt.setMaxWriteRate(0);// �������ӵ���������ʣ���λΪbytes/s��Ĭ��Ϊ0����������

			// ѡ����Ϣ�ַ�����
			mqtt.setDispatchQueue(Dispatch.createQueue("foo"));// ��û�е��÷���setDispatchQueue���ͻ��˽�Ϊ�����½�һ�����С������ʵ�ֶ������ʹ�ù��õĶ��У���ʽ��ָ��������һ���ǳ������ʵ�ַ���

			// ���ø�����
			// mqtt.setTracer(new Tracer() {
			// @Override
			// public void onReceive(MQTTFrame frame) {
			// System.out.println("recv: " + frame);
			// }
			//
			// @Override
			// public void onSend(MQTTFrame frame) {
			// System.out.println("send: " + frame);
			// }
			//
			// @Override
			// public void debug(String message, Object... args) {
			// System.out.println(String.format("debug: " + message, args));
			// }
			// });

			// ʹ�ûص�ʽAPI
			callbackConnection = mqtt.callbackConnection();

			// ���Ӽ���
			callbackConnection.listener(new Listener() {

				// ���ն��Ļ��ⷢ������Ϣ
				@Override
				public void onPublish(UTF8Buffer topic, Buffer payload,
						Runnable onComplete) {
					System.out
							.println("=============receive msg================"
									+ new String(payload.toByteArray()));

					// ֪ͨ������
					createInform(new String(payload.toByteArray()));

					onComplete.run();
				}

				// ����ʧ��
				@Override
				public void onFailure(Throwable value) {
					System.out.println("===========connect failure===========");
					callbackConnection.disconnect(null);
				}

				// ���ӶϿ�
				@Override
				public void onDisconnected() {
					System.out.println("====mqtt disconnected=====");

				}

				// ���ӳɹ�
				@Override
				public void onConnected() {
					System.out.println("====mqtt connected=====");

				}

			});

			// ����
			callbackConnection.connect(new Callback<Void>() {

				// ����ʧ��
				public void onFailure(Throwable value) {
					System.out.println("============����ʧ�ܣ�"
							+ value.getLocalizedMessage() + "============");
				}

				// ���ӳɹ�
				public void onSuccess(Void v) {
					// ��������
					Topic[] topics = { new Topic("foo", QoS.AT_LEAST_ONCE) };
					callbackConnection.subscribe(topics,
							new Callback<byte[]>() {
								// ��������ɹ�
								public void onSuccess(byte[] qoses) {
									System.out.println("========���ĳɹ�=======");
								}

								// ��������ʧ��
								public void onFailure(Throwable value) {
									System.out.println("========����ʧ��=======");
									callbackConnection.disconnect(null);
								}
							});

					// ������Ϣ
					callbackConnection.publish("foo", ("Hello ").getBytes(),
							QoS.AT_LEAST_ONCE, true, new Callback<Void>() {
								public void onSuccess(Void v) {
									System.out
											.println("===========��Ϣ�����ɹ�============");
								}

								public void onFailure(Throwable value) {
									System.out.println("========��Ϣ����ʧ��=======");
									callbackConnection.disconnect(null);
								}
							});

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
