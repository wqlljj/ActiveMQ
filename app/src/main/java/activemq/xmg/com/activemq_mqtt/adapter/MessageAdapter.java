package activemq.xmg.com.activemq_mqtt.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import activemq.xmg.com.activemq_mqtt.R;
import activemq.xmg.com.activemq_mqtt.bean.Message;

/**
 * create by wangqi
 * on 2020/9/17 0017
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    ArrayList<Message> messages = new ArrayList<>();

    public void addMessage(Message message){
        messages.add(message);
    }
    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_item, null);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder viewHolder, int i) {
        Message message = messages.get(i);
        SpannableStringBuilder ss = new SpannableStringBuilder();
        int index = 0;
        if(message.isLeft){
            viewHolder.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            ss.append(message.name+":"+message.data);
            index = ss.length();
            ss.append(sdf.format(new Date(message.time)));
            RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(0.5f);
            ss.setSpan(relativeSizeSpan,index,ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }else{
            viewHolder.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            ss.append(message.data);
        }
        viewHolder.msg.setText(ss);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{

        public final TextView msg;
        public final View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            msg = ((TextView) itemView.findViewById(R.id.msg));
            this.itemView = itemView;
        }
    }
}
