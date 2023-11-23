package dte.masteriot.mdp.citywanderer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;


public class LoadURLContent implements Runnable {

    Handler creator; // handler to the main activity, who creates this task
    private String string_URL;
    private static final String CONTENT_TYPE_XML = "application/xml";
    XmlPullParserFactory parserFactory;

    public LoadURLContent(Handler handler, String strURL) {
        // The constructor accepts 2 arguments:
        // The handler to the creator of this object
        // The URL to load.
        creator = handler;
        string_URL = strURL;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void run() {
        // initial preparation of the message to communicate with the UI Thread:
        Message msg = creator.obtainMessage();
        Bundle msg_data = msg.getData();

        HttpURLConnection urlConnection;
        String response = ""; // This string will contain the loaded contents of a text resource

        ArrayList list_monuments = new ArrayList<>(); // This string will contain the loaded contents of a text resource

        try {
            URL url = new URL(string_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            String actualContentType = urlConnection.getContentType(); // content-type header from HTTP server
            InputStream is = urlConnection.getInputStream();

            // Extract MIME type and subtype (get rid of the possible parameters present in the content-type header
            // Content-type: type/subtype;parameter1=value1;parameter2=value2...
            if((actualContentType != null) && (actualContentType.contains(";"))) {
                Log.d("Initial Parse", ": Complete HTTP content-type header from server = " + actualContentType);
                int beginparam = actualContentType.indexOf(";", 0);
                actualContentType = actualContentType.substring(0, beginparam);
            }

            if (CONTENT_TYPE_XML.equals(actualContentType)) {
                InputStreamReader reader = new InputStreamReader(is);
                BufferedReader in = new BufferedReader(reader);
                // We read the text contents line by line and add them to the response:
                String line = in.readLine();
                while (line != null) {
                    response += line + "\n";
                    line = in.readLine();
                }
            }

            urlConnection.disconnect();

        } catch (Exception e) {
            response = e.toString();
        }

        list_monuments = get_monuments(response);
        Collections.sort(list_monuments);

        if ("".equals(response) == false && list_monuments.size() != 0) {
            msg_data.putString("text", response);
            msg_data.putParcelableArrayList("monuments", list_monuments);
        }
        else {
            Log.e("Initial Parse", "no se ha podido desargar el xml o hacer el parce");
        }

        msg.sendToTarget();

    }

    public ArrayList get_monuments (String string_XML) {

        ArrayList monuments = new ArrayList<>(); // This string will contain the loaded contents of a text resource

        try {

            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = new ByteArrayInputStream(string_XML.getBytes());

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);

            int eventType = parser.getEventType(); // current event state of the parser

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String elementName = null;
                elementName = parser.getName(); // name of the current element

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("title".equals(elementName)) {
                            String monument = parser.nextText(); // if next element is TEXT then element content is returned
                            monuments.add(monument);
                            Log.d("Initial Parse", "title find" + monument);

                        }
                }
                eventType = parser.next(); // Get next parsing event
            }

        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return monuments;

    }
}


