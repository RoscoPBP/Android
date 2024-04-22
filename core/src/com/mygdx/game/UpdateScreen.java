package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class UpdateScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Stage stage;
    private TextField nombreTextField;
    private TextField telefonoTextField;
    private TextField emailTextField;
    public String name , apiKey, phoneNumber, email ,avatar;



    public UpdateScreen(MyGdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(5);

        // Configurar el stage para los textfields y la barra de aplicación
        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        cargarRespuestaJSON();

        // Crear la barra de aplicación
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
        Label titleLabel = new Label("Update", new Label.LabelStyle(font, Color.BLACK));
        appBarTable.add(backButton).size(100, 100).padRight(20);
        appBarTable.add(titleLabel).expandX().center();

        stage.addActor(appBarTable);



        String[] texturePaths = {"lol/0.png", "lol/16.png", "lol/17.png", "lol/20.png"};

// Crear un array de ImageButtons para almacenar los botones
        ImageButton[] imageButtons = new ImageButton[texturePaths.length];

// Crear un Table para organizar los botones
        Table imagenTable = new Table();
        imagenTable.setWidth(stage.getWidth());
        imagenTable.setFillParent(true);
        imagenTable.align(Align.bottom);
        imagenTable.padBottom(350).padLeft(10);

// Bucle for para crear los botones
        for (int i = 0; i < texturePaths.length; i++) {
            // Crear la textura correspondiente
            Texture texture = new Texture(texturePaths[i]);

            // Crear el botón correspondiente con la textura y añadirlo al array
            imageButtons[i] = new ImageButton(new TextureRegionDrawable(new TextureRegion(texture)));

            // Agregar un listener común a todos los botones
            imageButtons[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // Obtener la ruta de la textura del botón presionado
                    String texturePath = texturePaths[imagenTable.getChildren().indexOf(actor, true)];
                    avatar = convertImageToBase64(texturePath);

                    Gdx.app.log("Botón presionado", "Textura: " + texturePath);
                }
            });

            // Agregar el botón a la tabla
            imagenTable.add(imageButtons[i]).size(400, 400).align(i % 2 == 0 ? Align.right : Align.left);
            // Agregar un salto de línea después de dos botones
            if ((i + 1) % 2 == 0 && i < texturePaths.length - 1) {
                imagenTable.row();
            }
        }

        // Agregar la tabla al stage
        stage.addActor(imagenTable);







        // Botón de enviar


        Table enviarTable = new Table();
        enviarTable.setWidth(stage.getWidth());
        enviarTable.setFillParent(true);
        enviarTable.align(Align.bottom);
        enviarTable.padBottom(10).padLeft(10);

        Texture enviarTexture = new Texture("enviarButton.png");
        ImageButton enviarButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(enviarTexture)));
        // Dentro del método donde se configura el botón de envío
        enviarButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String nombre = nombreTextField.getText();
                String telefono = telefonoTextField.getText();
                String email = emailTextField.getText();
                Gdx.app.log("Solicitud HTTP", avatar);

                JsonValue json = new JsonValue(JsonValue.ValueType.object);
                json.addChild("api_key", new JsonValue(apiKey));
                json.addChild("name", new JsonValue(nombre));
                json.addChild("email", new JsonValue(email));
                json.addChild("phone_number", new JsonValue(telefono));
                json.addChild("avatar",new JsonValue(avatar));

                String jsonData = json.toJson(JsonWriter.OutputType.json);

                HttpRequest request = new HttpRequest(HttpMethods.POST);
                request.setUrl("https://roscodrom4.ieti.site/api/user/update");
                request.setContent(jsonData);
                request.setHeader("Content-Type", "application/json");

                Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
                    @Override
                    public void handleHttpResponse(Net.HttpResponse httpResponse) {
                        int statusCode = httpResponse.getStatus().getStatusCode();
                        if (statusCode == HttpStatus.SC_OK) {
                            String responseData = httpResponse.getResultAsString();
                            JsonValue jsonResponse = new JsonReader().parse(responseData);
                            String status = jsonResponse.getString("status");
                            if (status.equals("OK")){
                                guardarRespuestaEnJSON(apiKey, jsonData);
                                Gdx.app.log("Solicitud HTTP", "La solicitud fue exitosa");
                                cargarRespuestaJSON();
                            }else {
                                Gdx.app.error("Solicitud HTTP", "status error");

                            }

                        } else {
                            Gdx.app.error("Solicitud HTTP", "Error en la solicitud. Código de estado: " + statusCode);
                        }
                    }

                    @Override
                    public void failed(Throwable t) {
                        Gdx.app.error("Solicitud HTTP", "Error de conexión: " + t.getMessage());
                    }

                    @Override
                    public void cancelled() {
                        Gdx.app.error("Solicitud HTTP", "La solicitud fue cancelada");
                    }
                });
            }
        });





        enviarTable.add(enviarButton).size(400, 400).colspan(2);
        stage.addActor(enviarTable);

        // Crear la tabla para organizar los textfields y labels
        Table table = new Table();
        table.setWidth(stage.getWidth());
        table.setFillParent(true); // La tabla ocupa todo el espacio del stage
        table.align(Align.top);
        table.top().padTop(200);
        // Establecer espacio entre columnas y filas
        table.defaults().pad(20);// Alineación y separación superior

        // Crear el estilo para los textfields
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = font;
        textFieldStyle.fontColor = Color.BLACK; // Color del texto
        textFieldStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture("textfield_bg.png"))); // Fondo del textfield

        // Tamaño de los textfields
        float textFieldWidth = 600; // Ancho
        float textFieldHeight = 100; // Altura

        // Crear las labels
        Label nameLabel = new Label("Nombre:", new Label.LabelStyle(font, Color.BLACK));
        Label telefonoLabel = new Label("Teléfono:", new Label.LabelStyle(font, Color.BLACK));
        Label emailLabel = new Label("Email:", new Label.LabelStyle(font, Color.BLACK));

        // Textfield para el nombre
        nombreTextField = new TextField(name, textFieldStyle);
        nombreTextField.getStyle().background.setLeftWidth(10);
        nombreTextField.setSize(textFieldWidth, textFieldHeight);

        // Textfield para el teléfono
        telefonoTextField = new TextField(phoneNumber, textFieldStyle);
        telefonoTextField.getStyle().background.setLeftWidth(10);
        telefonoTextField.setSize(textFieldWidth, textFieldHeight);
// Establecer el filtro de entrada para permitir solo caracteres de texto
        telefonoTextField.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return Character.isDigit(c); // Permitir solo caracteres de texto
            }
        });

        // Textfield para el correo electrónico
        emailTextField = new TextField(email, textFieldStyle);
        emailTextField.getStyle().background.setLeftWidth(10);
        emailTextField.setSize(textFieldWidth, textFieldHeight);

        // Agregar las labels y textfields a la tabla con separación entre filas

        table.add(nameLabel).align(Align.right);
        table.add(nombreTextField).padBottom(20).width(textFieldWidth).height(textFieldHeight).row();
        table.add(telefonoLabel).align(Align.right);
        table.add(telefonoTextField).padBottom(20).width(textFieldWidth).height(textFieldHeight).row();
        table.add(emailLabel).align(Align.right);
        table.add(emailTextField).padBottom(20).width(textFieldWidth).height(textFieldHeight).row();

        stage.addActor(table); // Agregar la tabla al stage

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar y dibujar el stage
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        stage.dispose();
    }
    private void cargarRespuestaJSON() {
        FileHandle jsonFile = Gdx.files.local("respuesta.json");
        if (jsonFile.exists()) {
            String jsonData = jsonFile.readString();
            JsonValue jsonResponse = new JsonReader().parse(jsonData);
            apiKey = jsonResponse.getString("api_key");
            name = jsonResponse.getString("name");
            email = jsonResponse.getString("email");
            phoneNumber = jsonResponse.getString("phone_number");
            avatar = jsonResponse.getString("avatar");
        } else {
            apiKey = "";
            name = "";
            email = "";
            phoneNumber = "";
            avatar = "";
        }
    }

    private void guardarRespuestaEnJSON(String apiKey, String jsonData) {
        // Actualizar las variables con los nuevos valores del JSON
        JsonValue Data = new JsonReader().parse(jsonData);
        name = Data.getString("name");
        email = Data.getString("email");
        phoneNumber = Data.getString("phone_number");

        JsonValue jsonToSave = new JsonValue(JsonValue.ValueType.object);
        jsonToSave.addChild("api_key", new JsonValue(apiKey));
        jsonToSave.addChild("name", new JsonValue(name));
        jsonToSave.addChild("email", new JsonValue(email));
        jsonToSave.addChild("phone_number", new JsonValue(phoneNumber));
        jsonToSave.addChild("avatar",new JsonValue(avatar));

        String jsonToSaveString = jsonToSave.toJson(JsonWriter.OutputType.json);

        FileHandle jsonFile = Gdx.files.local("respuesta.json");
        jsonFile.writeString(jsonToSaveString, false);
    }
    public static String convertImageToBase64(String imagePath) {
        // Crear el manejador de assets
        FileHandle fileHandle = Gdx.files.internal(imagePath);

        // Verificar si el archivo existe en la ruta especificada
        if (!fileHandle.exists()) {
            System.out.println("La imagen no existe en la ruta especificada.");
            return "";
        }

        // Leer la imagen y convertirla a Base64
        try {
            // Leer los bytes de la imagen
            byte[] imageData = fileHandle.readBytes();

            // Convertir los bytes a Base64
            String base64Image = Base64.getEncoder().encodeToString(imageData);
            return base64Image;
        } catch (Exception e) {
            System.out.println("Error al leer la imagen: " + e.getMessage());
            return "";
        }
    }

}