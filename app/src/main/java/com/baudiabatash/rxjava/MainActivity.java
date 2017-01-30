package com.baudiabatash.rxjava;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


import javax.net.ssl.HttpsURLConnection;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {
    private static final String TAG ="MainActivity";
    //private Subscription subscription;
    private TextView textView,textView2;
    private Subscription mySubscription;
    private Subscription zippedSubscription;

    private String url="http://api.openweathermap.org/data/2.5/weather?q=Dhaka&APPID=387ac89122ce4877d4439db5a81ea9bc";

    Observable<String> myObservable;

    Observable<Integer> myArrayObservable;

    private String bal;

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bal = "";

        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        myObservable = Observable.just("Hello Sohel...");

        Observer<String> myObserver = new Observer<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {

            }
        };

        Action1<String> myAction = new Action1<String>() {
            @Override
            public void call(String s) {
                Log.d("Sohel",s);
            }
        };

        mySubscription=myObservable.subscribe(myAction);

        myArrayObservable
                = Observable.from(new Integer[]{1, 2, 3, 4, 5, 6}) // Emits each item of the array, one at a time
        .map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                return integer*integer;
            }
        })
        .skip(2)
        .filter(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer%2==1;
            }
        });


       /* myArrayObservable.map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                return integer*integer;
            }
        });*/

         myArrayObservable.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer i) {
                Log.d("MyAction", String.valueOf(i)); // Prints the number received
            }
        });


        Observable<String> myStringObservable =
                Observable.from(new String[]{"Sohel","Sumi","Roni","Putul","Rashin","Soddo","Jomshed","Laily","Sultan","Pantul"})
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s.startsWith("S");
                    }
                });

        myStringObservable.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.d("HHHH",s);
            }
        });


        Observable<String> fetchFromGoogle = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {

                try{
                    String data = getResponseFromUrl("http://www.google.com");
                    subscriber.onNext(data); // Emit the contents of the URL
                    subscriber.onCompleted(); // Nothing more to emit
                } catch (IOException e) {
                    subscriber.onError(e); // In case there are network errors
                }

            }
        });

       /* fetchFromGoogle.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        textView.setText(s);
                    }
                });*/


        Observable<String> fetchFromYahoo = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try{
                    String data = getResponseFromUrl(url);
                    subscriber.onNext(data); // Emit the contents of the URL
                    subscriber.onCompleted(); // Nothing more to emit
                } catch (IOException e) {
                    subscriber.onError(e); // In case there are network errors
                }
            }
        });


        fetchFromGoogle = fetchFromGoogle.subscribeOn(Schedulers.newThread());
        fetchFromYahoo = fetchFromYahoo.subscribeOn(Schedulers.newThread());

        Observable<String[]> zipped = Observable.zip(fetchFromGoogle, fetchFromYahoo, new Func2<String, String, String[]>() {
            @Override
            public String[] call(String google, String yahoo) {

                return new String[]{google,yahoo};
            }
        });

        zipped.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<String[]>() {
            @Override
            public void call(String[] strings) {
                textView.setText(String.valueOf(strings[0].length()));
                textView2.setText(String.valueOf(strings[1].length()));

                Log.d("GGGGGGGGG",strings[1]);
            }
        });

        /*Subscription zippedSubscription = zipped.subscribe(new Action1<String[]>() {
            @Override
            public void call(String[] results) {
                *//*textView.setText(String.valueOf(results[0].length()));
                textView2.setText(String.valueOf(results[1].length()));*//*

                Log.d("TTTT",results[0].length()+"");
                Log.d("TTTT",results[1].length()+"");
            }
        });*/





       /* Observable<String> stringObservable =
                Observable.from(new String[] {"bal","sal","mal","kal"});

        stringObservable.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                append(s);
            }
        });


        Log.d("TTTT",bal);*/

    }

    private void append(String s){
        bal=bal+" "+s;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mySubscription.unsubscribe();
       // zippedSubscription.unsubscribe();
    }


    /* public Observable<Gist> getGistObservable(){
        return Observable.defer(new Func0<>)
    }*/


    public String getResponseFromUrl(String url) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
        String inputLine;
        String result="";

        while ((inputLine=br.readLine())!=null){
            result+=inputLine;
        }

        return result;
    }


    // HTTP POST request
    private void sendPost() throws Exception {

        //Your server URL
        String url = "https://selfsolve.apple.com/wcResults.do";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        //Request Parameters you want to send
        String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

        // Send post request
        con.setDoOutput(true);// Should be part of code only for .Net web-services else no need for PHP
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

    }


   private String getData(String myUrl){

       StringBuilder result=new StringBuilder();
       try {
           // Enter URL address where your php file resides
           URL url = new URL(myUrl);

           HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
           conn.setReadTimeout(READ_TIMEOUT);
           conn.setConnectTimeout(CONNECTION_TIMEOUT);
           conn.setRequestMethod("GET");

           // setDoOutput to true as we recieve data from json file
           conn.setDoOutput(true);

           int response_code = conn.getResponseCode();

           if (response_code == HttpURLConnection.HTTP_OK) {

               // Read data sent from server
               InputStream input = conn.getInputStream();
               BufferedReader reader = new BufferedReader(new InputStreamReader(input));
               String line;

               while ((line = reader.readLine()) != null) {
                   Log.d("Str",line);
                   result.append(line);
               }



           } else {

               result.append("UnSuccessFul");
           }

       }  catch (MalformedURLException e) {
           e.printStackTrace();
       } catch (ProtocolException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }

       return result.toString();
   }


}
