package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public class MenuScreen extends ScreenAdapter {
    private Game game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;
    private String titulo = "Menu Principal";
    private String[] nombresBotones = {"Partida Individual", "Partida Multijugador", "Coliseo", "Opciones", "Perfil"};
    private Color colorTexto = Color.WHITE;
    private float botonWidth = 400;
    private float botonHeight = 100;
    private float espacioEntreBotones = 60;

    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();

        font = new BitmapFont();
        font.setColor(colorTexto);
        font.getData().setScale(5);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Dibujar los botones
        float yOffset = Gdx.graphics.getHeight() / 2 + (botonHeight + espacioEntreBotones) * nombresBotones.length / 2;
        for (int i = 0; i < nombresBotones.length; i++) {
            font.setColor(colorTexto);
            float buttonX = (Gdx.graphics.getWidth() - font.getSpaceXadvance() * nombresBotones[i].length()) / 2; // Centrar el texto
            float buttonY = yOffset - i * (botonHeight + espacioEntreBotones);
            font.draw(batch, nombresBotones[i], buttonX, buttonY + botonHeight / 2 + font.getLineHeight() / 2);
        }

        // Dibujar el título
        float tituloX = (Gdx.graphics.getWidth() - font.getSpaceXadvance() * titulo.length()) / 2; // Centrar el título
        font.draw(batch, titulo, tituloX, Gdx.graphics.getHeight() - 100);

        batch.end();

        // Manejar toques en los botones
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.input.getY();
            Vector3 touchPoint = new Vector3(touchX, touchY, 0);
            camera.unproject(touchPoint);

            for (int i = 0; i < nombresBotones.length; i++) {
                float buttonY = Gdx.graphics.getHeight() / 2 + (botonHeight + espacioEntreBotones) * nombresBotones.length / 2 - i * (botonHeight + espacioEntreBotones);
                if (touchPoint.y >= buttonY && touchPoint.y <= buttonY + botonHeight) {
                    switch (i) {
                        case 0:
                            game.setScreen(new PartidaIndividualScreen((MyGdxGame) game));
                            break;
                        case 1:
                            game.setScreen(new PartidaMultijugadorScreen((MyGdxGame) game));
                            break;
                        case 2:
                            game.setScreen(new ColisseuScreen((MyGdxGame) game));
                            break;
                        case 3:
                            game.setScreen(new OpcionesScreen((MyGdxGame) game));
                            break;
                        case 4:
                            game.setScreen(new PerfilScreen((MyGdxGame) game));
                            break;
                        default:
                            break;
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void hide() {
        batch.dispose();
        font.dispose();
    }
}
