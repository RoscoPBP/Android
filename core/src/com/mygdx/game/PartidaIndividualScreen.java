package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PartidaIndividualScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private Stage stage;
    private OrthographicCamera camera;
    private int numButtons = 10; // Número inicial de botones
    private float buttonSize = 120; // Tamaño inicial de los botones
    private Label labelLetras;

    public PartidaIndividualScreen(MyGdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        stage = new Stage(new ScreenViewport(camera));
        Gdx.input.setInputProcessor(stage);

        inicializarRosco();
        inicializarLabelLetras();
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
            pixmapDown.setColor(Color.RED);
            pixmapDown.fillCircle((int) (buttonSize / 2f), (int) (buttonSize / 2f), (int) (buttonSize / 2f));
            Texture textureDown = new Texture(pixmapDown);
            pixmapDown.dispose();
            TextureRegionDrawable downDrawable = new TextureRegionDrawable(new TextureRegion(textureDown));

            // Asignar las regiones de textura a los estilos de botón
            buttonStyle.up = upDrawable;
            buttonStyle.down = downDrawable;

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
        Texture backButtonTexture = new Texture("back_button.png");
        ImageButton enviarButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(backButtonTexture)));
        //TextButton enviarButton = new TextButton("", enviarButtonStyle);
        enviarButton.setSize(200, 200);
        enviarButton.setPosition(centerX - 200 / 2, centerY - 200 / 2); // Centrado en el centro del rosco
        //enviarButton.getLabel().setFontScale(5); // Ajustar el tamaño del texto

        enviarButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Aquí puedes manejar la lógica cuando se hace clic en el botón "Enviar"
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
        table.padBottom(220);

        stage.addActor(table); // Agregar la tabla al stage
    }
    private String obtenerLetraAleatoria() {
        // Retorna una letra aleatoria (puedes personalizar esto según tus necesidades)
        char letra = (char) ('A' + (int) (Math.random() * 26));
        return String.valueOf(letra);
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
}
