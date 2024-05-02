package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class NetworkManager {
    private String serverUrl = "https://roscodrom4.ieti.site";
    private String apiKey = "";
    private String nickname = "";

    private Socket socket;
    private boolean alta = false;
    private boolean ready = false;

    public NetworkManager() {
        cargarApiKey();
        connectToServer();
    }

    public void cargarApiKey(){
        try {
            FileHandle jsonFile = Gdx.files.local("respuesta.json");

            // Leer el contenido del archivo JSON
            String contenido = jsonFile.readString();

            // Crear un objeto JSON a partir del contenido
            JSONObject jsonObject = new JSONObject(contenido);

            // Obtener el valor de api_key del objeto JSON
            apiKey = jsonObject.getString("api_key");
            nickname = jsonObject.getString("name");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectToServer() {
        try {
            socket = IO.socket(serverUrl);
            socket.connect();

            socket.on(Socket.EVENT_CONNECT, args -> {
                System.out.println("Conectado al servidor");
                socket.emit("ALTA", "ALTA=" + nickname + ";API_KEY=" + apiKey);
            }).on("ALTA", args -> {
                JSONObject json = (JSONObject) args[0];
                try {
                    alta = json.getBoolean("alta");
                    System.out.println(alta);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).on("INICI_PARTIDA", args -> {
                System.out.println("sssssssssssssssssssss");
                JSONObject json = (JSONObject) args[0];


            }).on("Palabra", args -> {
                JSONObject json = (JSONObject) args[0];
                System.out.println(json);
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromServer() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }

    public boolean isAlta() {
        return alta;
    }

    public boolean isReady() {
        return ready;
    }
}
