package com.example.eunhan.csc201_projectchatapplication_app;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData;
import com.pubnub.api.models.consumer.presence.PNHereNowOccupantData;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.consumer.presence.PNSetStateResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;

public class MainActivity extends TabActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    String pubkey = "pub-c-17c6584e-18c7-48fc-80b4-c326a05baff0";
    String subkey = "sub-c-523a490e-f471-11e6-9c1c-0619f8945a4f";
    public String channel = "lobby";
    PubNub pubnub;

    EditText input;
    Button sendbtn;
    Button createbtn;

    private ArrayList<String> userlist = new ArrayList<String>();
    private ArrayList<String> chatlist = new ArrayList<String>();
    private ArrayList<String> chatroomlist = new ArrayList<String>();
    ListView userlistview;
    ListView chatlistview;
    ListView chatroomlistview;
    ArrayAdapter<String> Adapter;

    TabHost tabHost;

    private SharedPreferences mSharedPrefs;
    public static final String DATASTREAM_UUID = "this is not null";
    public static final String DATASTREAM_PREFS = "DATASTREAM_PREFS";
    public String uuid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabHost = getTabHost();
        LayoutInflater.from(this).inflate(R.layout.activity_main, tabHost.getTabContentView(), true);

        tabHost.addTab(tabHost.newTabSpec("users")
                .setIndicator("users")
                .setContent(R.id.usersview));
        tabHost.addTab(tabHost.newTabSpec("chat list")
                .setIndicator("chat list")
                .setContent(R.id.chatlistview));
        tabHost.addTab(tabHost.newTabSpec("chat room")
                .setIndicator("chat room")
                .setContent(R.id.chatroomview));


        input = (EditText) findViewById(R.id.input);
        sendbtn = (Button) findViewById(R.id.sendbtn);
        sendbtn.setOnClickListener(this);
        createbtn = (Button) findViewById(R.id.cratechatbtn);
        createbtn.setOnClickListener(this);
        userlistview = (ListView) findViewById(R.id.users);
        chatlistview = (ListView) findViewById(R.id.chatlist);
        chatroomlistview = (ListView) findViewById(R.id.chatroom);
        chatlistview.setOnItemClickListener(this);

        mSharedPrefs = getSharedPreferences(this.DATASTREAM_PREFS, MODE_PRIVATE);
        if (!mSharedPrefs.contains(this.DATASTREAM_UUID)) {
            Intent toLogin = new Intent(this, LoginActivity.class);
            startActivity(toLogin);
            return;
        }

        this.uuid = mSharedPrefs.getString(this.DATASTREAM_UUID, "");

        intochatlist("[bot channel]");
        connertPubNub(); //connected to pubnub server
        this.pubnub.subscribe().channels(Arrays.asList(channel)).withPresence().execute(); //automatically put into bot chatroom(channel)
        intochatlist(String.valueOf(pubnub.getSubscribedChannels().toString()));// get list of chat lists.
        renewPubNub();


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sendbtn:

                if(this.channel.equals("bot channel")){
                    intochatroom(this.uuid +": "+input.getText().toString());
                    if(input.getText().toString().equals("hi")){
                        intochatroom("bot: hi, how are you?");
                    }
                    else if(input.getText().toString().equals("who are you?"))
                    {
                        intochatroom("bot: I am answering bot. please ask me anything!");
                    }
                    else if(input.getText().toString().equals("how are you?"))
                    {
                        intochatroom("bot: I'm good! Thank you for asking!! You are so kind.");
                    }

                }
                else{
                    this.pubnub.publish().channel(channel).message(input.getText().toString()).async(
                            new PNCallback<PNPublishResult>() {
                                @Override
                                public void onResponse(PNPublishResult result, PNStatus status) {
                                    try {
                                        if (!status.isError()) {
                                            Log.v("check chat publish", "fine");
                                        } else {
                                            Log.v("check chat publish", "not working");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                    );}

                break;
            case R.id.cratechatbtn:

                disconnectAndCleanup();
//
//                SparseBooleanArray checkedItems = userlistview.getCheckedItemPositions();
//                String s = "";
//                int count = Adapter.getCount();
//
//
//                for (int i = count - 1; i >= 0; i--) {
//                    if (checkedItems.get(i)) {
//                        s += userlist.get(i).substring(0, userlist.get(i).toString().lastIndexOf(":")) + ", ";
//                    }
//                }
//
////                this.channel = s.substring(0, s.toString().lastIndexOf(","));
//                this.pubnub.unsubscribeAll();
//                this.channel = "bot channel";
//                this.pubnub.subscribe().channels(Arrays.asList(channel)).withPresence().execute();
//
//                s = s.substring(0, s.toString().lastIndexOf(",")) + "-chatroom created";
//                Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
//
//                // 모든 선택 상태 초기화.
//                userlistview.clearChoices();
//
//                tabHost.setCurrentTab(2);// tab moving
//
                break;
        }


    }

    private final void connertPubNub() {
        PNConfiguration config = new PNConfiguration();

        config.setPublishKey(this.pubkey);
        config.setSubscribeKey(this.subkey);
        config.setSecure(true);
        config.setUuid(this.uuid);


        pubnub = new PubNub(config);

    }

    private final void renewPubNub() {

        pubnub.hereNow().channels(Arrays.asList(channel)).async(new PNCallback<PNHereNowResult>() {
            @Override
            public void onResponse(PNHereNowResult result, PNStatus status) {
                if (status.isError()) {
                    return;
                }

                try {

                    for (Map.Entry<String, PNHereNowChannelData> entry : result.getChannels().entrySet()) {
                        for (PNHereNowOccupantData occupant : entry.getValue().getOccupants()) {
                            String s = occupant.getUuid() + ": join";
                            intousers(s);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    pubnub.setPresenceState()
                            .channels(Arrays.asList(channel))
                            .async(new PNCallback<PNSetStateResult>() {
                                @Override
                                public void onResponse(PNSetStateResult result, final PNStatus status) {
                                    // handle set state response


                                }
                            });
                }
            }

            @Override
            public void message(final PubNub pubnub, final PNMessageResult message) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //stuff that updates ui
                        String s = message.getPublisher() + ": " + message.getMessage().toString();//get messages in real time
                        intochatroom(s);//put into list view


                    }
                });


            }

            @Override
            public void presence(PubNub pubnub, final PNPresenceEventResult presence) {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String s = presence.getUuid() + ": " + presence.getEvent(); //get user status in real time
                        intousers(s); // put into listview


                    }
                });


            }
        });


    }

    private void intousers(String s) //print lists into user tab
    {

        for (int i = 0; i < userlist.size(); i++) {// delete user that has same uuid
            if (userlist.get(i).toString().lastIndexOf(":") != -1 && s.lastIndexOf(":") != -1) {
                String str1 = userlist.get(i).substring(0, userlist.get(i).toString().lastIndexOf(":"));
                String str2 = s.substring(0, s.lastIndexOf(":"));

                if (str1.equals(str2)) {
                    userlist.remove(i);

                }
            }

        }
        userlist.add(s);
        Adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, userlist);
        userlistview.setAdapter(Adapter);
    }

    private void intochatlist(String s) //print lists into chatlist tab + delete duplication by using Treeset(sort itself)
    {
        chatlist.add(s);
        TreeSet<String> distinctData = new TreeSet<String>(chatlist);
        chatlist = new ArrayList<String>(distinctData);
        Adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, chatlist);
        chatlistview.setAdapter(Adapter);
    }

    private void intochatroom(String s) //print lists into chatroom tab
    {
        chatroomlist.add(s);
        Adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, chatroomlist);
        chatroomlistview.setAdapter(Adapter);
    }

    private void clearchatroom() //print lists into chatroom tab
    {
        chatroomlist.clear();
        Adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, chatroomlist);
        chatroomlistview.setAdapter(Adapter);
    }

    private void disconnectAndCleanup() {
        getSharedPreferences(this.DATASTREAM_PREFS, MODE_PRIVATE).edit().clear().commit();

        if (this.pubnub != null) {
            this.pubnub.unsubscribe().channels(Arrays.asList(channel)).execute();
            this.pubnub.stop();
            this.pubnub = null;
        }

        mSharedPrefs = getSharedPreferences(this.DATASTREAM_PREFS, MODE_PRIVATE);
        if (!mSharedPrefs.contains(this.DATASTREAM_UUID)) {
            Intent toLogin = new Intent(this, LoginActivity.class);
            startActivity(toLogin);
            return;
        }

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String s = String.valueOf(parent.getItemAtPosition(position)).trim();

        if (s.equals("bot channel")) {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            this.pubnub.unsubscribeAll();
            this.channel = s.substring(1, s.length() - 1);
            clearchatroom();

            tabHost.setCurrentTab(2);// tab moving


        } else {

            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            this.pubnub.unsubscribeAll();
            this.channel = s.substring(1, s.length() - 1);
            this.pubnub.subscribe().channels(Arrays.asList(channel)).withPresence().execute();
            clearchatroom();
            tabHost.setCurrentTab(2);// tab moving
        }

    }
}
