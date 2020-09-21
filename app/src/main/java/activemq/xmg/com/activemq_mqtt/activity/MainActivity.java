package activemq.xmg.com.activemq_mqtt.activity;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import activemq.xmg.com.activemq_mqtt.Constants;
import activemq.xmg.com.activemq_mqtt.R;
import activemq.xmg.com.activemq_mqtt.adapter.MessageAdapter;
import activemq.xmg.com.activemq_mqtt.bean.Message;
import activemq.xmg.com.activemq_mqtt.moudle.alarm.AlarmBean;
import activemq.xmg.com.activemq_mqtt.moudle.alarm.AlarmManager;
import activemq.xmg.com.activemq_mqtt.moudle.util.SharePreferenceUtils;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.ed_client_id)
    EditText edClientId;
    @Bind(R.id.ed_server)
    EditText edServer;
    @Bind(R.id.ed_port)
    EditText edPort;
    @Bind(R.id.msg)
    EditText msg;
    @Bind(R.id.btn_connect)
    Button btnConnect;
    @Bind(R.id.send)
    Button send;
    @Bind(R.id.heart)
    TextView heart;
    private String serverIP;
    private String port;
    private static MqttAndroidClient client;
    private String TAG = "MqttActivity";
    private MqttConnectOptions conOpt;

    @Bind(R.id.message)
     RecyclerView messageRecyclerView;
    private MessageAdapter adapter;

    String device = "";

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setTitle("MQ CONTROL");
        if(btnConnect == null) {
            btnConnect = findViewById(R.id.btn_connect);
            edClientId = findViewById(R.id.ed_client_id);
            edServer = findViewById(R.id.ed_server);
            edPort = findViewById(R.id.ed_port);
            send = findViewById(R.id.send);
            msg = findViewById(R.id.msg);
            messageRecyclerView = findViewById(R.id.message);
        }
        Constants.clientID = "wangqi_" + UUID.randomUUID().toString().substring(0, 4);
        edClientId.setText( Constants.clientID);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            long lastTime = 0;
            @Override
            public void onClick(View view) {
                if(System.currentTimeMillis() - lastTime<1000){
                    return;
                }
                lastTime  =System.currentTimeMillis();
                if(client==null||!client.isConnected()) {
                    btnConnect.setText("断开连接");
                    //获取用户id
                    Constants.clientID = edClientId.getText().toString().trim();
                    //获取ip地址
                    serverIP = edServer.getText().toString().trim();
                    //获取端口号
                    port = edPort.getText().toString().trim();
                    startConnect( Constants.clientID, serverIP, port);
                }else if(client!=null){
                    btnConnect.setText("连接服务器");
                    try {
                        client.disconnect();
                        client.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    client = null;
                    showMsg(new Message("A","关闭连接",true));
                }
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            int i = 0;
            @Override
            public void onClick(View v) {
                String m = msg.getText().toString();
                Toast.makeText(MainActivity.this, "发送："+m, Toast.LENGTH_SHORT).show();
                publish(m);
            }
        });
        adapter = new MessageAdapter();
        messageRecyclerView.setAdapter(adapter);
        SharePreferenceUtils.setContext(this);
        AlarmManager.init(this);
    }

    final Runnable heartBeatTimeOut = new Runnable() {
        @Override
        public void run() {
            AlarmManager.getDefault().startAlarm(new AlarmBean(AlarmBean.LEVEL_CARE, AlarmBean.TYPE_VIBRATOR, "心跳超时"));
            handler.postDelayed(heartBeatTimeOut, 90000);
        }
    };
    private void startConnect(String clientID, String serverIP, String port) {
        //服务器地址
        String  uri ="tcp://";
        uri=uri+serverIP+":"+port;

        client = new MqttAndroidClient(this, uri, clientID);
        // 设置MQTT监听并且接受消息
        client.setCallback(new MqttCallback() {

            private Date date= new Date(0);
            private Gson gson = new Gson();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:HH:dd HH:mm:ss");

            @Override
            public void connectionLost(Throwable throwable) {
                showMsg(new Message("服务器","连接断开",true));
                Log.i(TAG, "connectionLost: ");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                final String str1 = new String(mqttMessage.getPayload());
                Log.i(TAG, "mqtt messageArrived: "+str1);
                Message message = gson.fromJson(str1, Message.class);
                switch (message.type){
                    case Message.TYPE_DEVICE:
                        device = message.name;
                        handler.postDelayed(heartBeatTimeOut,60000);
                        break;
                    case Message.TYPE_HEARTBEAT:
                        if(device.equals(message.name)) {
                            date.setTime(message.time);
                            settext(heart,sdf.format(date));
                            handler.removeCallbacks(heartBeatTimeOut);
                            handler.postDelayed(heartBeatTimeOut, 60000);
                        }else{
                            if(device.equals("")){
                                device = message.name;
                                showMsg(new Message("Sys","新用户接入："+message.name,true));
                            }
                            settext(heart,"新用户接入："+message.name);
                        }
                        break;
                    case Message.TYPE_MESSAGE:
                        showMsg(message);
                        break;
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Log.i(TAG, "deliveryComplete: ");
            }
        });

        conOpt = new MqttConnectOptions();
        // 清除缓存
        conOpt.setCleanSession(true);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(20);
        // 用户名
        conOpt.setUserName("admin");
        // 密码
        conOpt.setPassword("passWord".toCharArray());

        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + clientID + "\"}";
        Integer qos = 0;
        Boolean retained = false;
        if ((!message.equals("")) || (!Constants.TOPIC_CLIENT.equals(""))) {
            // 最后的遗嘱
            try {
                conOpt.setWill(Constants.TOPIC_CLIENT, message.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (Exception e) {
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }

        if (doConnect) {
            doClientConnection();
        }
    }
    private void settext(final TextView tv, final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(msg);
            }
        });
    }
    private void showMsg(final Message message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.addMessage(message);
                adapter.notifyItemInserted(adapter.getItemCount()-1);
            }
        });
    }
    /** 连接MQTT服务器 */
    private void doClientConnection() {
        if (!client.isConnected() && isConnectIsNomarl()) {
            try {
                client.connect(conOpt, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }else if(!isConnectIsNomarl()){
            iMqttActionListener.onFailure(null,new NetworkErrorException("网络不可用"));
        }

    }
    /** 判断网络是否连接 */
    private boolean isConnectIsNomarl() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }
    public void publishCMD(int type){
        Integer qos = 0;
        Boolean retained = false;
        try {
            Message me = new Message( Constants.clientID,type,false);
            showMsg(me);
            Log.i(TAG, "publish: "+Constants.TOPIC_CLIENT+"  "+me.toString());
            client.publish(Constants.TOPIC_CLIENT, me.toString().getBytes(), qos.intValue(), retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void publish(String msg){
        Integer qos = 0;
        Boolean retained = false;
        try {
            Message me = new Message("ME", msg, false);
            showMsg(me);
            Log.i(TAG, "publish: "+Constants.TOPIC_CLIENT+"  "+me.toString());
            client.publish(Constants.TOPIC_CLIENT, me.toString().getBytes(), qos.intValue(), retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    // MQTT是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
//            Log.i(TAG, "连接成功 ");
            showMsg(new Message("服务器","连接成功",true));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnConnect.setText("断开连接");
                    send.setEnabled(true);
                }
            });
            try {
                Log.i(TAG, "onSuccess: subscribe "+Constants.TOPIC_CONTROL);
                // 订阅myTopic话题
                client.subscribe(Constants.TOPIC_CONTROL, 1);
                publishCMD(Message.TYPE_DEVICE);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            showMsg(new Message("A","连接断开",true));
            // 连接失败，重连
        }
    };

    /**
     * 获取MqttAndroidClient实例
     * @return
     */
    public static MqttAndroidClient getMqttAndroidClientInstace(){
        if(client!=null)
            return  client;
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlarmManager.getDefault().destroy();
        SharePreferenceUtils.setContext(null);
        if(client!=null)
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
    }
}
