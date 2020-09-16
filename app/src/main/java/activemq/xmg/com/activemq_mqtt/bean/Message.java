package activemq.xmg.com.activemq_mqtt.bean;

/**
 * Description :
 * Author : liujun
 * Email  : liujin2son@163.com
 * Date   : 2016/10/26 0026
 */

public class Message {
    public Long time;
    public String string;
    public boolean isLeft=true;


    public Message(String string, boolean isLeft) {
        time = System.currentTimeMillis();
        this.string = string;
        this.isLeft = isLeft;
    }

}
