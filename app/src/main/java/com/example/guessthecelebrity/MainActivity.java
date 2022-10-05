package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebUrls = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb = 0;
    String[] answers = new String[4];
    int locationOfCorrectAnswer = 0;
    ImageView imageView;
    Button option1;
    Button option2;
    Button option3;
    Button option4;

    public void celebChosen(View view) {
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG).show();
            newQuestion();
        }else{
            Toast.makeText(getApplicationContext(), "Wrong! It was " + celebNames.get(chosenCeleb), Toast.LENGTH_LONG).show();
            newQuestion();
        }
    }

    public void newQuestion(){
        try{
            Random rand = new Random();
            chosenCeleb = rand.nextInt(celebUrls.size());
            ImageDownloader imageTask = new ImageDownloader();
            Bitmap celebImage = imageTask.execute(celebUrls.get(chosenCeleb)).get();
            imageView.setImageBitmap(celebImage);

            locationOfCorrectAnswer = rand.nextInt(4);
            int incorrectAnswerLocation;
            for(int i=0; i<4; i++){
                if(i == locationOfCorrectAnswer){
                    answers[i] = celebNames.get(chosenCeleb);
                }else{
                    incorrectAnswerLocation = rand.nextInt(celebNames.size());
                    while(incorrectAnswerLocation == chosenCeleb){
                        incorrectAnswerLocation = rand.nextInt(celebNames.size());
                    }
                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }
            option1.setText(answers[0]);
            option2.setText(answers[1]);
            option3.setText(answers[2]);
            option4.setText(answers[3]);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try{
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            // Log.i("celebtag", "CELEB url = " + urls[0]);
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            URL url;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                return result.toString();
            } catch (MalformedURLException e) {
                result.append("Error: MalformedURLException");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result.toString();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        DownloadTask task = new DownloadTask();
        String result = null;
        try{
            result = task.execute("https://www.imdb.com/list/ls052283250/").get();
            String[] splitResult = result.split("class=\"lister-list\"");
            // System.out.println(splitResult[1]);
            Pattern p = Pattern.compile("img alt=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[1]);
            while(m.find()){
                // System.out.println(m.group(1));
                celebNames.add(m.group(1));
            }

            p = Pattern.compile("height=\"209\"\nsrc=\"(.*?)\"");
            m = p.matcher(splitResult[1]);
            while(m.find()){
                // System.out.println(m.group(1));
                celebUrls.add(m.group(1));
            }

            /*
            // Logging out the information
             celebNames = new ArrayList(celebNames.subList(4, celebNames.size()));
             celebUrls = new ArrayList(celebUrls.subList(4, celebUrls.size()));
             System.out.println(celebNames);
             System.out.println(celebUrls);
             Log.i("Contents of URL", result);
             */
             newQuestion();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}