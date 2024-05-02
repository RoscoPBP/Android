package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.socket.client.IO;
import io.socket.emitter.Emitter;
import io.socket.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class SalaEspera extends ScreenAdapter {
    private final MyGdxGame game;
    private Stage stage;

    private final BitmapFont font;
    private OrthographicCamera camera;

    private Socket socket;
    private String serverUrl = "https://roscodrom4.ieti.site";
    private String apiKey = "";
    private String nickname = "";

    private double tiempoRestante = 10;
    private int tempsRestant = 0;

    private Label loadingLabel, conection;
    private String letters = "";

    public SalaEspera(MyGdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        stage = new Stage(new ScreenViewport(camera));
        font = new BitmapFont();
        font.getData().setScale(5);
        cargarApiKey();

        loadingLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
        Table tableLoading = new Table();
        tableLoading.setFillParent(true);
        tableLoading.top().padTop(200);
        tableLoading.add(loadingLabel);
        stage.addActor(tableLoading);

        conection = new Label("conectando ...", new Label.LabelStyle(font, Color.WHITE));
        Table tableconection = new Table();
        tableconection.setFillParent(true);
        tableconection.center();
        tableconection.add(conection).padBottom(200);
        stage.addActor(tableconection);

        try {
            socket = IO.socket(serverUrl);
            socket.connect();

            socket.on(Socket.EVENT_CONNECT, args -> {
                System.out.println("Conectado al servidor");
                conection.setText("conetado");
                emitirAlta();
            }).on("ALTA", args -> {
                JSONObject json = (JSONObject) args[0];
                try {
                    boolean alta = json.getBoolean("alta");
                    JSONObject tiempoRestanteObject = json.getJSONObject("tiempoRestante");
                    boolean enPartida = tiempoRestanteObject.getBoolean("enPartida");

                    if (!alta) {
                        loadingLabel.setText("en espera");
                    } else {
                        game.setScreen(getScreenForCurrentPage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).on("INICI_PARTIDA", args -> {
                JSONObject json = (JSONObject) args[0];
                try {
                    letters = json.getString("letters");
                    game.setScreen(getScreenForCurrentPage());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        TextButton altaButton = new TextButton("Alta", game.skin);
        altaButton.getLabel().setFontScale(2); // Escalar la fuente del botón
        altaButton.setSize(600, 400); // Establecer el tamaño del botón a 200x100
        altaButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                emitirAlta(); // Llama al método emitirAlta()
            }
        });

        Table tableButton = new Table();
        tableButton.setFillParent(true);
        tableButton.bottom().pad(200);
        tableButton.add(altaButton).height(100).width(200);
        stage.addActor(tableButton);
    }

    private ScreenAdapter getScreenForCurrentPage() {
        return new PartidaMultijugadorScreen((MyGdxGame) game);
    }

    public void cargarApiKey() {
        try {
            FileHandle jsonFile = Gdx.files.local("respuesta.json");
            String contenido = jsonFile.readString();
            JSONObject jsonObject = new JSONObject(contenido);
            apiKey = jsonObject.getString("api_key");
            nickname = jsonObject.getString("name");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void emitirAlta() {
        loadingLabel.setText("en espera");
        socket.emit("ALTA", "ALTA=" + nickname + ";API_KEY=" + apiKey);
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 1, 1); // Cambiar a azul
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void dispose() {
        socket.disconnect();
        stage.dispose();
    }
}
