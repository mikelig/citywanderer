package dte.masteriot.mdp.citywanderer.MonumentInfo;

import static dte.masteriot.mdp.citywanderer.ListOfMonuments.MainActivity.dataset;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


// For radio buttons
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;
import org.jsoup.Jsoup;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.provider.Settings;
import android.view.WindowManager;

import dte.masteriot.mdp.citywanderer.ListOfMonuments.MainActivity;
import dte.masteriot.mdp.citywanderer.R;


public class InfoMonument extends AppCompatActivity implements TextToSpeech.OnInitListener, SensorEventListener {
    private static final String TAG = "DATASET";
    XmlPullParserFactory parserFactory;
    String xmlText;
    Toolbar appbar;
    TextView description;
    TextView url;
    String textDescription;
    ImageView imageView;
    LineChart chart;
    String imageUrl;
    TextView address;
    String textAddress;
    private TextToSpeech textToSpeech ;
    private Button speakButton;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private int position;
    private FloatingActionButton button_left;
    private FloatingActionButton button_right;
    private FloatingActionButton button_exit;
    String textWeb;
    RadioGroup radioGroup;
    Button mqttButton;
    String specificTopic;
    MqttAndroidClient mqttClient;
    //String apiKey = getString(R.string.google_cloud_translate_api_key);
    //Translate translate = TranslateOptions.newBuilder()
    //        .setApiKey(apiKey)
    //        .build()
    //       .getService();

    List<ILineDataSet> dataSets;
    List<Entry> ValuesConcurrent;
    private static InfoMonument instance; // implemented to use publish() from InfoMonuments.java

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infomonuments);
        instance = this;
        appbar = findViewById(R.id.appbar);
        description = findViewById(R.id.descriptionMonument);
        address = findViewById(R.id.address);
        imageView = findViewById(R.id.imageMonument);
        url = findViewById(R.id.url);
        chart = (LineChart) findViewById(R.id.chart);
        button_left = findViewById(R.id.left);
        button_right = findViewById(R.id.right);
        button_exit = findViewById(R.id.exit);
        textToSpeech = new TextToSpeech(this, this);
        speakButton = findViewById(R.id.textToSpeechButton);
        radioGroup = findViewById(R.id.radioGroup);
        mqttButton = findViewById(R.id.mqttButton);
        // Get intent, action and type
        Intent intent = getIntent();
        String action = intent.getAction();
        Long reference_timestamp = System.currentTimeMillis()/1000;

        // Sensor
        // Get the reference to the sensor manager and the sensor:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // MQTT
        specificTopic = intent.getStringExtra("TOPIC");
        mqttClient = ((MainActivity) MainActivity.getInstance()).getMqttClient();

        // Chart
        dataSets = new ArrayList<ILineDataSet>();
        ValuesConcurrent = new ArrayList<Entry>();
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new LineChartXAxisValueFormatter());

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llama al método para leer el texto
                speakText(String.valueOf(Html.fromHtml(textDescription)));
          }
        });

        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity(); // Cierra la actividad actual y todas las actividades asociadas
                Intent intent = new Intent((MainActivity) MainActivity.getInstance(), MainActivity.class);
                startActivity(intent);
            }
        });

        button_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position != dataset.getSize()-1){

                    String monument = dataset.getItemAtPosition(position +1).getTitle();
                    String topic = dataset.getItemAtPosition(position + 1 ).getTopic();

                    Intent i = new Intent((MainActivity) MainActivity.getInstance(), InfoMonument.class);
                    i.setAction(Intent.ACTION_SEND);
                    i.putExtra("XML_TEXT", xmlText);
                    i.putExtra("MONUMENT", monument);
                    i.putExtra("TOPIC", topic);
                    i.putExtra("position", position + 1 );
                    Context context = (MainActivity) MainActivity.getInstance();
                    context.startActivity(i);
                }
            }
        });

        button_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position != 0){

                    String monument = dataset.getItemAtPosition(position - 1).getTitle();
                    String topic = dataset.getItemAtPosition(position - 1 ).getTopic();

                    Intent i = new Intent((MainActivity) MainActivity.getInstance(), InfoMonument.class);
                    i.setAction(Intent.ACTION_SEND);
                    i.putExtra("XML_TEXT", xmlText);
                    i.putExtra("MONUMENT", monument);
                    i.putExtra("TOPIC", topic);
                    i.putExtra("position", position - 1 );
                    Context context = (MainActivity) MainActivity.getInstance();
                    context.startActivity(i);
                }
            }
        });

        mqttButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Find the selected radio button
                int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();

                if (checkedRadioButtonId != -1) {
                    RadioButton checkedRadioButton = findViewById(checkedRadioButtonId);

                    if (checkedRadioButton != null) {
                        // Get the text of the selected radio button
                        String selectedText = checkedRadioButton.getText().toString();

                        // Get the current time in HH:MM:SS format
                        String currentTime = getCurrentTime();

                        // Convert the radio button text to int
                        int radioButtonTextAsInt = Integer.parseInt(selectedText);

                        // TEST
                        /*
                        Long tsLong = System.currentTimeMillis()/1000;
                        Long Xnew = tsLong - reference_timestamp;
                        AxisValueFormatter xAxisFormatter = new HourAxisValueFormatter(referenceTimestamp);
                        XAxis xAxis = mChart.getXAxis();
                        xAxis.setValueFormatter(xAxisFormatter);

                         */


                        //do_chart();

                        publishMessage(mqttClient, specificTopic, selectedText);

                        // Do something with the selected text (e.g., display in a Toast)
                        Toast.makeText(InfoMonument.this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // No radio button is selected
                }
            }
        });

        if (Intent.ACTION_SEND.equals(action)) {
            xmlText = intent.getStringExtra("XML_TEXT");
            String monument = intent.getStringExtra("MONUMENT");
            position = intent.getIntExtra("position",0);
            Log.d("Swipe", "position: " + position);

            if (monument != null && xmlText != null) {
                appbar.setTitle(monument);
                //setSupportActionBar(appbar);
                get_info_monuments(xmlText, monument);
            }
        }

    }

    @Override
    protected void onDestroy() {
        // Libera los recursos de TextToSpeech cuando la actividad se destruye
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            dte.masteriot.mdp.citywanderer.RecyclerView.MyOnItemActivatedListener.isItemClicked = false;
        }
        super.onDestroy();
}

    // Add a method to get the current time in HH:MM:SS format
    private String getCurrentTime() {
        // Use java.util.Calendar to get the current time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    // Add a method to get the current time in hours
    private float getTimeInHours() {
        // Use java.util.Calendar to get the current time
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        // Convert time to hours
        //float timeInHours = hours + minutes / 60f + seconds / 3600f;

        return seconds;
    }

    public ArrayList get_info_monuments (String string_XML, String monument_to_find) {

        ArrayList info_monuments = new ArrayList<>(); // This string will contain the loaded contents of a text resource

        try {

            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = new ByteArrayInputStream(string_XML.getBytes());

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);

            //do_chart(0,0);

            int eventType = parser.getEventType(); // current event state of the parser
            boolean monument_found = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String elementName = null;
                elementName = parser.getName(); // name of the current element


                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("title".equals(elementName)) {
                            String monument = parser.nextText(); // if next element is TEXT then element content is returned
                            if (monument.equals(monument_to_find)){
                                monument_found = true;
                            }
                            else {
                                monument_found = false;
                            }

                        }
                        else if ("description".equals(elementName)){
                            if (monument_found){
                                textDescription = parser.nextText();
                                textDescription = manipularHTML(textDescription);
                                description.setText(Html.fromHtml(textDescription));
                            }
                        }
                        else if ("address".equals(elementName)){
                            if (monument_found){
                                textAddress = parser.nextText();
                                address.setText(Html.fromHtml(textAddress));
                            }
                        }
                        else if ("image".equals(elementName)){
                            if (monument_found){
                                imageUrl = parser.nextText();
                                Glide.with(this).load(imageUrl).into(imageView);
                            }
                        }
                        else if ("uri".equals(elementName)){
                            if (monument_found){
                                textWeb = parser.nextText();
                                SpannableString spannableString = new SpannableString(textWeb);
                                ClickableSpan clickableSpan = new ClickableSpan() {

                                    @Override
                                    public void onClick(@NonNull View view) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(textWeb));
                                        startActivity(intent);
                                    }
                                };
                                spannableString.setSpan(clickableSpan, 0, textWeb.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                                url.setText(spannableString);
                                url.setMovementMethod(LinkMovementMethod.getInstance());
                            }
                        }
                }
                eventType = parser.next(); // Get next parsing event
            }

        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return info_monuments;

    }

    public void do_chart (double timestamp, double concurrency) {

        // List of datasets:
        Entry e = new Entry((float) timestamp, (int) concurrency);
        ValuesConcurrent.add(e);

        /*
        ValuesConcurrent.add(new Entry((float) 16.00, (float) 2.00));
        ValuesConcurrent.add(new Entry((float) 16.05, (float) 5.00));
        ValuesConcurrent.add(new Entry((float) 16.10, (float) 3.00));
        */

        // add entries to datasets:
        LineDataSet dataSetSine = new LineDataSet(ValuesConcurrent, "concurrency");
        // configure datasets colors:
        dataSetSine.setColor(Color.RED);
        dataSetSine.setCircleColor(Color.RED);
        // add datasets to the list of datasets:
        dataSets.add(dataSetSine);
        // create line data:
        LineData lineDataSineAndCosine = new LineData(dataSets);
        // set data to chart:
        chart.setData(lineDataSineAndCosine);
        // configure chart:

        chart.getDescription().setEnabled(false);
        chart.animateX(3000); // animation to draw chart
        chart.invalidate(); // refresh

    }

    private void speakText(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // int langResult = textToSpeech.setLanguage(Locale.ENGLISH);
            int langResult = textToSpeech.setLanguage(new Locale("es", "ES")); // set Spanish lannguage

            if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                    langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle language not supported
            } else {
                speakButton.setEnabled(true);
            }
        } else {
            // Handle initialization failure
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity(); // Cierra la actividad actual y todas las actividades asociadas
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void publishMessage(MqttAndroidClient mqttClient, String topic, String msg) {
        MqttMessage message = new MqttMessage();
        message.setPayload(msg.getBytes());
        message.setRetained(false);
        message.setQos(0);
        try {
            if (!mqttClient.isConnected()) {
                Log.d("MQTT_PUBLISH", "CLIENT NOT CONNECTED");
            }
            mqttClient.publish(topic, message);
            Log.d("MQTT_PUBLISH", "topic: " + topic + " msg: " + msg);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MQTT_PUBLISH", e.toString());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){
            float lightValue = sensorEvent.values[0];
            if (lightValue < 10) {
                // Ambiente muy oscuro, ajustar a un valor bajo
                setBrightness(0.1f);
            } else if (lightValue < 100) {
                // Ambiente con poca luz, ajustar a un valor moderado
                setBrightness(0.5f);
            } else {
                // Ambiente bien iluminado, ajustar a un valor alto
                setBrightness(1.0f);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void setBrightness(float brightness) {
        // Verificar si el ajuste automático de brillo está habilitado y desactivarlo si es necesario
        try {
            int mode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        // Ajustar el brillo de la pantalla
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = brightness;
        getWindow().setAttributes(layoutParams);
    }

    private void setConcurrencyData() {
        // now in hours
        long now = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());

    }

    private String manipularHTML(String html) {
        // Parsear el HTML con Jsoup
        org.jsoup.nodes.Document document = Jsoup.parse(html);

        // Seleccionar todas las etiquetas 'a' y su contenido y eliminarlas
        document.select("a").remove();

        // Obtener el texto modificado
        return document.body().text();

    }

    public static InfoMonument getInstance() {
        return instance;
    }
}

