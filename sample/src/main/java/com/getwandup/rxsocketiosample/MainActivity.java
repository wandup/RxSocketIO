package com.getwandup.rxsocketiosample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.getwandup.rxsocketio.RxSocketIO;
import com.getwandup.rxsocketio.domain.SocketEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.log_output)
    TextView logOutput;

    private RxSocketIO rxSocketIO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initRxSocketIO();
    }

    @OnClick(R.id.disconnect)
    protected void disconnect() {
        rxSocketIO.getSocket().disconnect();
    }

    private void log(String message) {
        logOutput.append("\n");
        logOutput.append(message);
    }

    private void initRxSocketIO() {
        Collection<String> events = new ArrayList<>();
        events.add("connect");
        events.add("new message");
        events.add("disconnect");

        rxSocketIO = new RxSocketIO();
        Observable<SocketEvent> observable =
                rxSocketIO.connect("http://chat.socket.io/", events)
                        .doOnError(new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                log("Error: " + throwable.getMessage());
                            }
                        });

        ConnectableObservable connObs = observable.publish();
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<SocketEvent, Boolean>() {
                    @Override
                    public Boolean call(SocketEvent socketEvent) {
                        return socketEvent.getType().equals("connect");
                    }
                }).subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                log("Connected!");
            }
        });
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<SocketEvent, Boolean>() {
                    @Override
                    public Boolean call(SocketEvent socketEvent) {
                        return socketEvent.getType().equals("new message");
                    }
                }).subscribe(new Action1<SocketEvent>() {
            @Override
            public void call(SocketEvent socketEvent) {
                JSONObject data = (JSONObject) socketEvent.getArgs()[0];
                String username;
                String message;
                try {
                    username = data.getString("username");
                    message = data.getString("message");
                    log(username + ": " + message);
                } catch (JSONException e) {
                    return;
                }
            }
        });
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<SocketEvent, Boolean>() {
                    @Override
                    public Boolean call(SocketEvent socketEvent) {
                        return socketEvent.getType().equals("disconnect");
                    }
                }).subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                log("Disconnected :(");
            }
        });
        connObs.connect();
    }
}
