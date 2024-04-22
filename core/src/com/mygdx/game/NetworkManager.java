package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.Disposable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkManager implements Disposable {
    private ExecutorService executorService;

    public NetworkManager() {
        executorService = Executors.newFixedThreadPool(5);
    }

    // Método para hacer una solicitud HTTP GET
    public void makeHttpGetRequest(String url, HttpResponseListener listener) {
        HttpRequestBuilder builder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = builder.newRequest().method(Net.HttpMethods.GET).url(url).build();
        Gdx.net.sendHttpRequest(httpRequest, listener);
    }

    // Método para hacer una solicitud HTTP POST
    public void makeHttpPostRequest(String url, String postData, HttpResponseListener listener) {
        HttpRequestBuilder builder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = builder.newRequest().method(Net.HttpMethods.POST).url(url).content(postData).build();
        Gdx.net.sendHttpRequest(httpRequest, listener);
    }

    // Método para conectar a un servidor WebSocket
    public void connectToWebSocket(String url, WebSocketListener listener) {
        executorService.execute(() -> {
            try {
                URI uri = new URI(url);
                SocketHints hints = new SocketHints();
                Socket socket = Gdx.net.newClientSocket(Net.Protocol.TCP, uri.getHost(), uri.getPort(), hints);
                listener.onConnected(socket);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                String line;
                while ((line = reader.readLine()) != null) {
                    listener.onMessageReceived(line);
                }
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
                listener.onConnectionError(e);
            }
        });
    }

    // Interfaz para manejar eventos del WebSocket
    public interface WebSocketListener {
        void onConnected(Socket socket);
        void onMessageReceived(String message);
        void onConnectionError(Throwable throwable);
    }

    @Override
    public void dispose() {
        executorService.shutdown();
    }
}
