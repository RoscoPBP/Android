package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.github.czyzby.websocket.WebSockets;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class PartidaMultijugadorScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private Stage stage;

    private float stateTime = 0;
    private float lastSendTime = 0;
    private float sendInterval = 1.0f;

    private final BitmapFont font;
    private OrthographicCamera camera;

    private Socket socket;
    private String serverUrl = "https://roscodrom4.ieti.site";
    private String apiKey = "";
    private String nickname = "";
    Boolean comprovar = false;

    private int numButtons = 10; // Número inicial de botones
    private float buttonSize = 120; // Tamaño inicial de los botones
    private Label labelLetras, titleLabel;
    private final CountDownLatch latch = new CountDownLatch(1);

    private  String letters;
    private float tiempoRestante = 5; // Tiempo restante inicial del temporizador en segundos
    private Label labelTiempo;
    private Label espera;

    private int puntos = 0;
    Sound sonidoTrue = Gdx.audio.newSound(Gdx.files.internal("acierto.wav"));
    Sound sonidoFalse = Gdx.audio.newSound(Gdx.files.internal("fallo.wav"));

    private Table listas ;
    private ScrollPane scrollPane;
    private ArrayList<String> palabrasEnviadas = new ArrayList<>(); // Lista de palabras enviadas

    boolean alta = false;
    boolean ready = false;
    private TextButton.TextButtonStyle buttonStyle ;
    private  Texture sendButtonTexture;

    private ArrayList<Character> lettersList = new ArrayList<>();
    private ArrayList<Character> vocales = new ArrayList<>(Arrays.asList('A', 'E', 'I', 'O', 'U'));
    private ArrayList<Character> consonantesMuyUsadas = new ArrayList<>(Arrays.asList('L', 'N', 'S', 'T', 'R'));
    private ArrayList<Character> consonantesPocoUsadas = new ArrayList<>(Arrays.asList('B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'M', 'P', 'Q', 'V', 'W', 'X', 'Y', 'Z'));
    private Random random = new Random();

    public PartidaMultijugadorScreen(MyGdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        stage = new Stage(new ScreenViewport(camera));
        font = new BitmapFont();
        texturas();
        cargarApiKey();
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
                espera.setText("");
                System.out.println("sssssssssssssssssssss");
                JSONObject json = (JSONObject) args[0];

                try {
                    String lettersString = json.getString("letters");
                    lettersList = convertirALista(lettersString);

                    inicializarRosco();
                    // Ahora lettersList contiene los caracteres como elementos individuales en un ArrayList
                    // Puedes usar lettersList como lo necesites en tu código
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }).on("PARAULA_OK", args -> {
                JSONObject json = (JSONObject) args[0];
                System.out.println(json);
                try {
                    puntos+= json.getInt("value");
                    titleLabel.setText("puntos: " + puntos);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }).on("PARAULA_ACERTADA", args -> {
                JSONObject json = (JSONObject) args[0];
                System.out.println(json);
                try {
                    String palabra = json.getString("paraula");
                    palabrasEnviadas.add(palabra);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }).on("FI_PARTIDA", args -> {
                JSONObject json = (JSONObject) args[0];
                System.out.println(json);
                showDialog("","fin");
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        // Pasados 30 segundos, cambiar de pantalla
                        game.setScreen(new MenuScreen(game));
                        dispose();
                    }
                }, 30);
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        font.getData().setScale(5);
        Gdx.input.setInputProcessor(stage);

        Table appBarTable = new Table();
        appBarTable.setWidth(stage.getWidth());
        appBarTable.setFillParent(true);
        appBarTable.align(Align.top);
        appBarTable.padTop(10).padLeft(10);

        // Botón de retroceso
        Texture backButtonTexture = new Texture("back_button.png");
        ImageButton backButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(backButtonTexture)));
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
        });


        // Texto "Login" centrado
        titleLabel = new Label("puntos: "+ puntos, new Label.LabelStyle(font, Color.WHITE));
        appBarTable.add(backButton).size(100, 100).padRight(20);
        appBarTable.add(titleLabel).expandX().right().padRight(50);

        stage.addActor(appBarTable);


        labelTiempo = new Label("", new Label.LabelStyle(font, Color.WHITE));
        Table tableTiempo = new Table();
        tableTiempo.setFillParent(true);
        tableTiempo.top().pad(20);
        tableTiempo.add(labelTiempo).row();
        tableTiempo.add();
        espera = new Label("En espera", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(espera);

        if (alta){
            // Crear y programar el temporizador
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    if (tiempoRestante >= 0) {
                        tiempoRestante -= 1;
                    } // Reducir el tiempo restante en 1 segundo
                    if (tiempoRestante <= 0) {
                        // El temporizador ha terminado, aquí puedes agregar la lógica para manejarlo
                        //Gdx.app.log("Timer", "¡Tiempo terminado!");

                        // Mostrar un mensaje de fin de partida
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                showDialog("", "Fin de partida \n puntuacion");
                            }
                        });

                        // Cambiar a la pantalla del menú principal
                        /**
                         Gdx.app.postRunnable(new Runnable() {
                        @Override public void run() {
                        game.setScreen(new MenuScreen(game));
                        }
                        });
                         **/
                    }
                }
            }, 1, 1);
        }


        listas = new Table();
        scrollPane = new ScrollPane(listas, game.skin); // Asignar tu skin al ScrollPane
        listas.setFillParent(true); // Para que el ScrollPane ocupe todo el espacio del Stage


        scrollPane.getStyle().background = null;
        scrollPane.updateVisualScroll();
        scrollPane.setFadeScrollBars(true);
        scrollPane.setScrollingDisabled(true, false); // Deshabilita el desplazamiento en la dirección x
        scrollPane.setForceScroll(false, true); // Fuerza el desplazamiento solo en la dirección y
        scrollPane.setScrollBarPositions(false,false);


        Table t = new Table();
        t.setFillParent(true);
        t.align(Align.top);
        t.padTop(150);
        t.add(scrollPane).width(stage.getWidth()).height(600);

        stage.addActor(t);



    }


    private static ArrayList<Character> convertirALista(String lettersString) {
        // Eliminar los corchetes del principio y del final del String
        lettersString = lettersString.substring(1, lettersString.length() - 1);
        // Dividir el String en substrings utilizando la coma como delimitador
        String[] substrings = lettersString.split(",");
        ArrayList<Character> lettersList = new ArrayList<>();
        // Recorrer cada substring y extraer el carácter entre comillas dobles
        for (String substring : substrings) {
            // Eliminar espacios en blanco y comillas dobles
            substring = substring.trim().replace("\"", "");
            // Obtener el primer carácter del substring y añadirlo a la lista
            lettersList.add(substring.charAt(0));
        }
        return lettersList;
    }

    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(title, game.skin) {
            @Override
            public float getPrefWidth() {
                return 600; // Ancho deseado del diálogo
            }

            @Override
            public float getPrefHeight() {
                return 400; // Alto deseado del diálogo
            }
        };

        // Configurar la fuente del título
        Label titleLabel = dialog.getTitleLabel();
        titleLabel.setFontScale(3);

        // Configurar la fuente del encabezado
        Label label = new Label(message, game.skin);
        label.setFontScale(3); // Escalar la fuente del texto
        dialog.text(label);

        // Configurar el botón "OK" para volver a la pantalla inicial
        TextButton okButton = new TextButton("OK", game.skin);
        okButton.getLabel().setFontScale(2); // Escalar la fuente del botón
        okButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Cambiar a la pantalla del menú principal
                game.setScreen(new MenuScreen(game));
                dialog.hide(); // Ocultar el diálogo después de cambiar de pantalla
            }
        });

        // Agregar el botón "OK" al diálogo y configurar su tamaño
        dialog.button(okButton).pad(20); // Ajustar el tamaño según sea necesario

        // Configurar el tamaño del diálogo
        dialog.getContentTable().pad(20); // Añadir espacio entre el texto y los bordes del diálogo

        dialog.show(stage);
    }

    public void texturas(){

        inicializarLabelLetras();
        TextButton.TextButtonStyle enviarButtonStyle = new TextButton.TextButtonStyle(game.skin.get("default", TextButton.TextButtonStyle.class));
        enviarButtonStyle.up = new TextureRegionDrawable(game.skin.getRegion("default-round"));
        enviarButtonStyle.down = new TextureRegionDrawable(game.skin.getRegion("default-round-down"));
        sendButtonTexture = new Texture("send_button.png");

        buttonStyle = new TextButton.TextButtonStyle(game.skin.get("default", TextButton.TextButtonStyle.class));

        // Crear una textura circular como fondo del botón para el estado "up"
        Pixmap pixmapUp = new Pixmap((int) buttonSize, (int) buttonSize, Pixmap.Format.RGBA8888);
        pixmapUp.setColor(Color.WHITE);
        pixmapUp.fillCircle((int) (buttonSize / 2f), (int) (buttonSize / 2f), (int) (buttonSize / 2f));
        Texture textureUp = new Texture(pixmapUp);
        pixmapUp.dispose();
        TextureRegionDrawable upDrawable = new TextureRegionDrawable(new TextureRegion(textureUp));

        // Crear una textura circular como fondo del botón para el estado "down"
        Pixmap pixmapDown = new Pixmap((int) buttonSize, (int) buttonSize, Pixmap.Format.RGBA8888);
        pixmapDown.setColor(colorFromHexString("f79b08"));
        pixmapDown.fillCircle((int) (buttonSize / 2f), (int) (buttonSize / 2f), (int) (buttonSize / 2f));
        Texture textureDown = new Texture(pixmapDown);
        pixmapDown.dispose();
        TextureRegionDrawable downDrawable = new TextureRegionDrawable(new TextureRegion(textureDown));

        // Asignar las regiones de textura a los estilos de botón
        buttonStyle.up = upDrawable;
        buttonStyle.down = downDrawable;
        // Asignar color de fuente negro
        buttonStyle.fontColor = colorFromHexString("1630BE");
    }

    private void actualizarTablaLetras() {
        // Limpiar la tabla antes de agregar nuevas palabras
        listas.clear();

        // Agregar nuevas palabras al principio de la lista de palabras
        for (int i = palabrasEnviadas.size() - 1; i >= 0; i--) {
            String palabra = palabrasEnviadas.get(i);
            Label label = new Label(palabra, new Label.LabelStyle(font, Color.WHITE));
            if (i != palabrasEnviadas.size() - 1) { // Omitir el espacio en la primera palabra
                listas.row(); // Agregar una nueva fila solo después de la primera palabra
            }
            listas.add(label).padTop(0).padBottom(20).row(); // Ajustar el padding superior e inferior
        }

        // Obtener la altura total del contenido
        float contenidoHeight = listas.getPrefHeight();

        // Ajustar la altura del ScrollPane para asegurarse de que todo el contenido sea visible
        scrollPane.setHeight(stage.getHeight() - 200); // Ajustar según tus necesidades

        // Ajustar el scroll para que la parte superior del contenido sea visible
        scrollPane.setScrollY(contenidoHeight);
    }

    private void inicializarRosco() {
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 3f; // Ajustar la altura para dejar más espacio en la parte inferior
        float radius = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) / 3f;

        for (int i = 0; i < numButtons; i++) {
            float angle = i * 360f / numButtons;
            float buttonX = centerX + (float) Math.cos(Math.toRadians(angle)) * radius;
            float buttonY = centerY + (float) Math.sin(Math.toRadians(angle)) * radius;

            final String letra = obtenerLetraAleatoria();


            TextButton button = new TextButton(letra, buttonStyle);
            button.setSize(buttonSize, buttonSize);
            button.setPosition(buttonX - button.getWidth() / 2, buttonY - button.getHeight() / 2);
            button.getLabel().setFontScale(5); // Ajustar el tamaño del texto

            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Obtener la letra del botón pulsado
                    String letraPulsada = letra;

                    // Obtener el texto actual de la etiqueta
                    String textoActual = labelLetras.getText().toString();

                    // Agregar la letra pulsada al texto actual de la etiqueta
                    textoActual += letraPulsada;

                    // Actualizar el texto de la etiqueta
                    labelLetras.setText(textoActual);
                }
            });

            stage.addActor(button);
        }


        // Agregar el botón "Enviar" dentro del rosco

        ImageButton enviarButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(sendButtonTexture)));
        //TextButton enviarButton = new TextButton("", enviarButtonStyle);
        enviarButton.setSize(200, 200);
        enviarButton.setPosition(centerX - 200 / 2, centerY - 200 / 2); // Centrado en el centro del rosco
        //enviarButton.getLabel().setFontScale(5); // Ajustar el tamaño del texto

        enviarButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Aquí puedes manejar la lógica cuando se hace clic en el botón "Enviar"

                String palabra  = String.valueOf(labelLetras.getText());


                FileHandle fileHandle = Gdx.files.internal("DISC2-LP.txt");

                String targetWord = palabra;

                socket.emit("PARAULA", "PARAULA=" + palabra + ";API_KEY=" + apiKey);

                Boolean comprovar = Funciones.findWord(fileHandle, targetWord);

                if (comprovar) {
                    palabrasEnviadas.add(palabra);

                    sonidoTrue.play(); // Reproducir el sonido si la condición es verdadera
                    System.out.println("true");
                } else {
                    sonidoFalse.play();
                    System.out.println("false");// Reproducir el sonido si la condición es falsa
                }

                labelLetras.setText("");
                actualizarTablaLetras();
                System.out.println("Botón 'Enviar' presionado");
            }
        });
        stage.addActor(enviarButton);
    }



    private void inicializarLabelLetras() {

        // Crear la etiqueta de texto para mostrar las letras de los botones pulsados
        BitmapFont font = new BitmapFont();
        font.getData().setScale(5); // Aumentar el tamaño de la fuente
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        labelLetras = new Label("", labelStyle);

        // Crear una tabla para centrar la etiqueta en la pantalla
        Table table = new Table();
        table.setFillParent(true); // La tabla ocupa todo el espacio del stage
        table.add(labelLetras).expand().center(); // Centrar la etiqueta en la tabla
        table.padBottom(275);
        stage.addActor(table); // Agregar la tabla al stage

    }

    private String obtenerLetraAleatoria() {
        char letra;
        // Verificar si la lista tiene elementos
        if (!lettersList.isEmpty()) {
            // Si la lista tiene elementos, eliminar el primer elemento
            letra = lettersList.remove(0);
        } else {
            // Si la lista está vacía, asignar un valor por defecto a 'letra'
            letra = ' ';
        }
        return String.valueOf(letra);
    }



    private Color colorFromHex(long hex) {
        float a = (hex & 0xFF000000L) >> 24;
        float r = (hex & 0xFF0000L) >> 16;
        float g = (hex & 0xFF00L) >> 8;
        float b = (hex & 0xFFL);

        return new Color(r/255f, g/255f, b/255f, a/255f);
    }

    private Color colorFromHexString(String s) {
        // Eliminar el signo "#" si está presente
        if(s.startsWith("#"))
            s = s.substring(1);

        // Convertir de la forma "#RRGGBB" a "AARRGGBB"
        String hexString = "FF" + s;

        // Asegurarse de que la longitud sea 8 (AARRGGBB)
        if(hexString.length() != 8)
            throw new IllegalArgumentException("String must have the form #RRGGBB");

        // Convertir a Color
        return colorFromHex(Long.parseLong(hexString, 16));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 1, 1); // Cambiar a azul
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (alta){
            if (tiempoRestante >= 0) {
                labelTiempo.setText(tiempoRestante + "s");
            }
            if (socket != null) {
                stateTime += delta;
                // Comprueba si ha pasado el intervalo de tiempo desde el último envío
                if (stateTime - lastSendTime > sendInterval) {
                    // Actualiza el tiempo del último envío
                    lastSendTime = stateTime;
                }
            }
        }

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
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
    //finn
}