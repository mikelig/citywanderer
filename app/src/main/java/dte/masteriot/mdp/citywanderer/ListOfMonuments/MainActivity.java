package dte.masteriot.mdp.citywanderer.ListOfMonuments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import dte.masteriot.mdp.citywanderer.R;
import dte.masteriot.mdp.citywanderer.RecyclerView.Dataset;
import dte.masteriot.mdp.citywanderer.RecyclerView.MyAdapter;
import dte.masteriot.mdp.citywanderer.RecyclerView.MyItemDetailsLookup;
import dte.masteriot.mdp.citywanderer.RecyclerView.MyItemKeyProvider;
import dte.masteriot.mdp.citywanderer.RecyclerView.MyOnItemActivatedListener;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "DATASET";

    // App-specific dataset:
    //private ArrayList<String> monumentNameList = createTestMonumentList();
    String xmlText;
    private ArrayList<String> monumentNameList = createTestMonumentList();
    ExecutorService es;
    MyOnItemActivatedListener onItemActivatedListener;
    private Dataset dataset;

    private RecyclerView recyclerView;
    private SelectionTracker<Long> tracker;
    private static final String URL_XML_MONUMENTS = "https://www.zaragoza.es/sede/servicio/monumento.xml";

    // MQTT
    final String mqttServerUri = "tcp://192.168.56.1:1883";
    String publishMessage = "Hello World!";
    MqttAndroidClient mqttAndroidClient;
    MqttConnectOptions mqttConnectOptions;
    Long tsLong = System.currentTimeMillis()/1000;
    String clientId = "client" + tsLong.toString();
    String tmpString;
    private static MainActivity instance; // implemented to use publish() from InfoMonuments.java
    private Context mContext;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Button searchMonument;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        //setupToolbar();

        // Create an executor for the background tasks:
        es = Executors.newSingleThreadExecutor();

        // Execute the loading task in background:
        Log.d("Initial Parse", "Going to launch background thread...");

        LoadURLContent loadURLContents = new LoadURLContent(handler_initialXMLparce, URL_XML_MONUMENTS);
        es.execute(loadURLContents);

        // Get the reference to the sensor manager and the sensor:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        searchMonument = findViewById(R.id.buttonSearch);
        editText = findViewById(R.id.plain_text_input);

        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Prepare the RecyclerView:
        recyclerView = findViewById(R.id.recyclerView);
        dataset = new Dataset(monumentNameList);
        MyAdapter recyclerViewAdapter = new MyAdapter(dataset);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Choose the layout manager to be set.
        // some options for the layout manager:  GridLayoutManager, LinearLayoutManager, StaggeredGridLayoutManager
        // by default, a linear layout is chosen:
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Selection tracker (to allow for selection of items):
        onItemActivatedListener = new MyOnItemActivatedListener(this, dataset);
        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                recyclerView,
                new MyItemKeyProvider(ItemKeyProvider.SCOPE_MAPPED, recyclerView),
//                new StableIdKeyProvider(recyclerView), // This caused the app to crash on long clicks
                new MyItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withOnItemActivatedListener(onItemActivatedListener)
                .build();
        recyclerViewAdapter.setSelectionTracker(tracker);

        if (savedInstanceState != null) {
            // Restore state related to selections previously made
            tracker.onRestoreInstanceState(savedInstanceState);
        }

        searchMonument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textForSearch = editText.getText().toString();
                if (textForSearch == null){
                    // change dataset
                    dataset.setNewData(monumentNameList);
                    recyclerView.getAdapter().notifyDataSetChanged();
                    onItemActivatedListener.set_XML_text(xmlText);

                } else {
                    // change dataset
                    dataset.setNewData(containsSubstring( monumentNameList, textForSearch ));
                    recyclerView.getAdapter().notifyDataSetChanged();
                    onItemActivatedListener.set_XML_text(xmlText);
                }


            }
        });

        clientId = clientId + System.currentTimeMillis();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), mqttServerUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    addToHistory("Reconnected to : " + serverURI);
                } else {
                    addToHistory("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                addToHistory(topic + "  Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false); // so that we dont need to resubscribe if disconnect() happens

        addToHistory("Connecting to " + mqttServerUri + "...");
        mqttConnect(mqttConnectOptions);

        //mqtttest();

    }


    private void mqttConnect(MqttConnectOptions mqttConnectOptions) {
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic("status");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to connect to: " + mqttServerUri +
                            ". Cause: " + ((exception.getCause() == null)?
                            exception.toString() : exception.getCause()));
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            addToHistory(e.toString());
        }
    }

    private void addToHistory(String mainText) {
        Log.d("MQTT", "LOG: " + mainText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    public void subscribeToTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    addToHistory("Subscribed to: " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to subscribe to: " + topic);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            addToHistory(e.toString());
        }

    }

    private void resetTopics() {
        try {
            // unsubscribe from all topics
            mqttAndroidClient.unsubscribe("monuments/#");
        } catch (MqttException e) {
            e.printStackTrace();
            addToHistory(e.toString());
        }
        // subscribe to new topics
        for(int i=0; i<dataset.getSize();i++) {
            tmpString = dataset.getItemAtPosition(i).getTopic();
            addToHistory("Going to subscribe to: " + tmpString);
            subscribeToTopic(tmpString);
        }

    }




    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        tracker.onSaveInstanceState(outState); // Save state about selections.
    }

    private ArrayList createTestMonumentList() {
        ArrayList<String> monumentNamesList = new ArrayList<>();
        monumentNamesList.add("Loading monument list...");
        return monumentNamesList;
    }

    Handler handler_initialXMLparce = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d("Initial Parse", "Entered fun: handler_initialXMLparce.handleMessage");
            // message received from background thread: load complete (or failure)
            ArrayList<String> monumentNamesList_aux;
            String string_msg;

            super.handleMessage(msg);
            monumentNamesList_aux = msg.getData().getStringArrayList("monuments");
            if (monumentNamesList_aux != null){
                monumentNameList = monumentNamesList_aux;
                for (int i = 0; i<monumentNameList.size(); i++){
                    Log.d("Initial Parse", monumentNameList.get(i));
                }
            }

            if((string_msg = msg.getData().getString("text")) != null) {
                xmlText = string_msg;
                Log.d("Initial Parse", xmlText);
            }
            // change dataset
            dataset.setNewData(monumentNameList);
            //mqttDisconnect();
            //mqttConnect(
            // mqttConnectOptions);
            resetTopics();
            recyclerView.getAdapter().notifyDataSetChanged();
            onItemActivatedListener.set_XML_text(xmlText);

        }
    };

    public MqttAndroidClient getMqttClient() {
        return this.mqttAndroidClient;
    }

    public static MainActivity getInstance() {
        return instance;
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
        // Before doing anything, we need to ensure that the app has the necessary permission to
        // change system (e.g. set brightness level)
        if (!Settings.System.canWrite(this)) {
            // If App do not have permission, request it:
            // Start the ACTION_MANAGE_WRITE_SETTINGS activity

            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

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


    // Función para verificar si un String coincide con alguna parte de los elementos en ArrayList
    public static ArrayList<String> containsSubstring(ArrayList<String> list, String word) {
        ArrayList<String> searchList = new ArrayList<>();
        for (String element : list) {
            if (element.contains(word)) {
                searchList.add(element);

            }
        }
        return searchList;

    }
}