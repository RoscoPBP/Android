package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class MenuScreen extends ScreenAdapter {
    private Game game;
    private Stage stage;
    private Table table;
    private String titulo = "Menu Principal";
    private String[] nombresBotones = {"Partida Individual", "Partida Multijugador", "Coliseo", "Opciones", "Perfil"};
    private int currentPageIndex = 0; // Índice de la página actual

    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(AppConfig.VIEWPORT);
        Gdx.input.setInputProcessor(stage);

        table = new Table();
        table.setWidth(stage.getWidth());
        table.setFillParent(true);
        table.align(Align.top);
        table.top().padTop(20);

        // Agregar título
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(6);
        Label titleLabel = new Label(titulo, new Label.LabelStyle(titleFont, Color.WHITE));
        table.add(titleLabel).padBottom(150).padTop(150).row();

        // Ajustar tamaño de la fuente para los botones
        BitmapFont buttonFont = new BitmapFont();
        buttonFont.getData().setScale(4); // Ajustar escala para cambiar el tamaño del texto

        // Agregar botones con texto más grande
        for (String nombreBoton : nombresBotones) {
            TextButton button = new TextButton(nombreBoton, new TextButton.TextButtonStyle(null, null, null, buttonFont));
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Manejar clic en botón
                    handleButtonClick(nombreBoton);
                }
            });
            table.add(button).width(800).height(150).padBottom(100).row(); // Ajustar tamaño de botón
        }

        stage.addActor(table);
    }

    private void handleButtonClick(String buttonText) {
        switch (buttonText) {
            case "Partida Individual":
                currentPageIndex = 0;
                break;
            case "Partida Multijugador":
                currentPageIndex = 1;
                break;
            case "Coliseo":
                currentPageIndex = 2;
                break;
            case "Opciones":
                currentPageIndex = 3;
                break;
            case "Perfil":
                currentPageIndex = 4;
                break;
            default:
                break;
        }
        // Actualizar la pantalla según la página actual
        game.setScreen(getScreenForCurrentPage());
    }

    private ScreenAdapter getScreenForCurrentPage() {
        switch (currentPageIndex) {
            case 0:
                return new PartidaIndividualScreen((MyGdxGame) game);
            case 1:
                if (perfilFileExists()) {
                    return new PartidaMultijugadorScreen((MyGdxGame) game);
                }else{
                    Gdx.app.log("MenuScreen", "Perfil file not found, redirecting to PerfilScreen");
                    return new PerfilScreen((MyGdxGame) game);
                }
            case 2:
                return new ColisseuScreen((MyGdxGame) game);
            case 3:
                return new OpcionesScreen((MyGdxGame) game);
            case 4:
                if (perfilFileExists()) {
                    Gdx.app.debug("update", "update");
                    return new UpdateScreen((MyGdxGame) game);
                } else {
                    Gdx.app.log("MenuScreen", "Perfil file not found, redirecting to PerfilScreen");
                    return new PerfilScreen((MyGdxGame) game);
                }
            default:
                return null;
        }
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
    public void hide() {
        stage.dispose();
    }
    private boolean perfilFileExists() {
        // Verificar si el archivo "respuesta.json" existe
        FileHandle file = Gdx.files.local("respuesta.json");
        return file.exists();
    }
}
