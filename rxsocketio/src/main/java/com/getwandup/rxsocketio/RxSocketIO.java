package com.getwandup.rxsocketio;

import com.getwandup.rxsocketio.domain.SocketEvent;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.util.Collection;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * @author manolovn
 */
public class RxSocketIO {

    private Socket socket;
    private boolean isUnsubscribed;

    public RxSocketIO() {

    }

    public Observable<SocketEvent> connect(final String endpoint, final Collection<String> events) {
        return Observable.create(new Observable.OnSubscribe<SocketEvent>() {
            @Override
            public void call(final Subscriber<? super SocketEvent> subscriber) {

                try {
                    socket = IO.socket(endpoint);
                } catch (URISyntaxException e) {
                    subscriber.onError(new WebSocketException());
                }

                for (final String ev : events) {
                    socket.on(ev, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            subscriber.onNext(new SocketEvent(ev, args));
                        }
                    });
                }

                socket.connect();
                isUnsubscribed = false;

                subscriber.add(new Subscription() {
                    @Override
                    public void unsubscribe() {
                        for (final String ev : events) {
                            socket.off(ev);
                        }
                        isUnsubscribed = true;
                    }

                    @Override
                    public boolean isUnsubscribed() {
                        return isUnsubscribed;
                    }
                });
            }
        });
    }

    public Socket getSocket() {
        return socket;
    }
}
