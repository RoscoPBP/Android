package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class PartidaIndividualScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private Stage stage;
    private final BitmapFont font;
    private OrthographicCamera camera;
    private int numButtons = 10; // Número inicial de botones
    private float buttonSize = 120; // Tamaño inicial de los botones
    private Label labelLetras;

    private GlyphLayout glyphLayout;

    private Table listas ;
    private ScrollPane scrollPane;
    private ArrayList<String> palabrasEnviadas = new ArrayList<>(); // Lista de palabras enviadas
    private final int NUMERO_PALABRAS_VISIBLES = 4;

    private ArrayList<Character> vocales = new ArrayList<>(Arrays.asList('A', 'E', 'I', 'O', 'U'));
    private ArrayList<Character> consonantesMuyUsadas = new ArrayList<>(Arrays.asList('L', 'N', 'S', 'T', 'R'));
    private ArrayList<Character> consonantesPocoUsadas = new ArrayList<>(Arrays.asList('B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'M', 'P', 'Q', 'V', 'W', 'X', 'Y', 'Z'));
    private Random random = new Random();

    public PartidaIndividualScreen(MyGdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        stage = new Stage(new ScreenViewport(camera));
        font = new BitmapFont();
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
        Label titleLabel = new Label("", new Label.LabelStyle(font, Color.BLACK));
        appBarTable.add(backButton).size(100, 100).padRight(20);
        appBarTable.add(titleLabel).expandX().center();

        stage.addActor(appBarTable);

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


        inicializarRosco();
        inicializarLabelLetras();
    }


    private void actualizarTablaLetras() {
        // Limpiar la tabla antes de agregar nuevas palabras
        listas.clear();

        // Agregar nuevas palabras al principio de la lista de palabras
        for (int i = palabrasEnviadas.size() - 1; i >= 0; i--) {
            String palabra = palabrasEnviadas.get(i);
            Label label = new Label(palabra, new Label.LabelStyle(font, Color.BLACK));
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
            TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle(game.skin.get("default", TextButton.TextButtonStyle.class));

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
        TextButton.TextButtonStyle enviarButtonStyle = new TextButton.TextButtonStyle(game.skin.get("default", TextButton.TextButtonStyle.class));
        enviarButtonStyle.up = new TextureRegionDrawable(game.skin.getRegion("default-round"));
        enviarButtonStyle.down = new TextureRegionDrawable(game.skin.getRegion("default-round-down"));
        Texture backButtonTexture = new Texture("send_button.png");
        ImageButton enviarButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(backButtonTexture)));
        //TextButton enviarButton = new TextButton("", enviarButtonStyle);
        enviarButton.setSize(200, 200);
        enviarButton.setPosition(centerX - 200 / 2, centerY - 200 / 2); // Centrado en el centro del rosco
        //enviarButton.getLabel().setFontScale(5); // Ajustar el tamaño del texto

        enviarButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Aquí puedes manejar la lógica cuando se hace clic en el botón "Enviar"
                String palabra  = String.valueOf(labelLetras.getText());
                palabrasEnviadas.add(palabra);
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
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.BLACK);
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

        // Generar un número aleatorio entre 0 y 1 (inclusive)
        int randomType = random.nextInt(2); // 0 o 1

        // Si el número aleatorio es 1 o quedan menos de 2 vocales, elegir una consonante
        if (randomType == 1 || vocales.size() < 2) {
            // Elegir entre consonantes muy usadas y poco usadas
            if (consonantesMuyUsadas.size() >= 5) {
                int index = random.nextInt(consonantesMuyUsadas.size());
                letra = consonantesMuyUsadas.remove(index);
            } else {
                int index = random.nextInt(consonantesPocoUsadas.size());
                letra = consonantesPocoUsadas.remove(index);
            }
        }
        // Si el número aleatorio es 0 y quedan al menos 2 vocales, elegir una vocal
        else {
            int index = random.nextInt(vocales.size());
            letra = vocales.remove(index);
        }

        return String.valueOf(letra);
    }

    private Color colorFromHex(long hex)
    {
        float a = (hex & 0xFF000000L) >> 24;
        float r = (hex & 0xFF0000L) >> 16;
        float g = (hex & 0xFF00L) >> 8;
        float b = (hex & 0xFFL);

        return new Color(r/255f, g/255f, b/255f, a/255f);
    }

    private Color colorFromHexString(String s)
    {
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
    //finn
}
