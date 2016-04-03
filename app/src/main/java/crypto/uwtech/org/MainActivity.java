package crypto.uwtech.org;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import crypto.uwtech.org.crypto.R;

public class MainActivity extends AppCompatActivity {

    private static int LENGTH = 1024;
    public static KeyDetails KEY;

    public static Animation slide_in;
    public static Animation slide_out;
    public static CardView progress;
    public static CardView genStep;
    public static LinearLayout encStep;
    public static LinearLayout hackStep;
    public static LinearLayout endStep;
    public static LinearLayout noneStep;

    public static TextView encryptedMessage;
    public static TextView originalMessage;
    public static TextView decryptedMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slide_in = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        slide_out = AnimationUtils.loadAnimation(this, R.anim.slide_out);

        progress = (CardView) findViewById(R.id.progress);
        genStep = (CardView) findViewById(R.id.genStep);
        encStep = (LinearLayout) findViewById(R.id.encStep);
        hackStep = (LinearLayout) findViewById(R.id.hackStep);
        endStep = (LinearLayout) findViewById(R.id.endStep);
        noneStep = (LinearLayout) findViewById(R.id.noneStep);

        Button keyGenerate = (Button) findViewById(R.id.keyGenerate);
        Button keyDetails = (Button) findViewById(R.id.keyDetails);
        Button encryptDo = (Button) findViewById(R.id.encryptDo);
        Button hackDo = (Button) findViewById(R.id.hackDo);

        RadioGroup keyGroup = (RadioGroup) findViewById(R.id.keyGroup);

        final EditText rLength = (EditText) findViewById(R.id.rLength);
        final EditText encryptMessage = (EditText) findViewById(R.id.encryptMessage);

        encryptedMessage = (TextView) findViewById(R.id.encryptedMessage);
        originalMessage = (TextView) findViewById(R.id.originalMessage);
        decryptedMessage = (TextView) findViewById(R.id.decryptedMessage);

        keyGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                LENGTH = 0;
                RadioButton button = (RadioButton) findViewById(checkedId);
                LENGTH = Integer.parseInt(button.getText().toString());
            }
        });
        keyGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genStep.startAnimation(slide_out);
                genStep.setVisibility(View.GONE);
                KEY = null;
                KeyGenTask task = new KeyGenTask();
                int r = Integer.parseInt(rLength.getText().toString());
                task.execute(LENGTH, r);
                progress.setVisibility(View.VISIBLE);
                progress.startAnimation(slide_in);
            }
        });

        keyDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), DetailActivity.class);
                startActivity(intent);
            }
        });

        encryptDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (encryptMessage.getText().toString().trim().equals("")) {
                    encryptMessage.setError(getString(R.string.encrypt_message_error));
                    return;
                }
                encStep.startAnimation(slide_out);
                encStep.setVisibility(View.GONE);
                originalMessage.setText(encryptMessage.getText());
                EncryptTask task = new EncryptTask();
                task.execute(encryptMessage.getText().toString());
                progress.setVisibility(View.VISIBLE);
                progress.startAnimation(slide_in);
            }
        });

        hackDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hackStep.startAnimation(slide_out);
                hackStep.setVisibility(View.GONE);
                HackTask task = new HackTask();
                task.execute(encryptedMessage.getText().toString(), encryptMessage.getText().toString());
                progress.setVisibility(View.VISIBLE);
                progress.startAnimation(slide_in);
            }
        });
    }

    class KeyGenTask extends AsyncTask<Integer, Void, KeyDetails> {

        final BigInteger TWO = new BigInteger("2");

        private BigInteger privateKey;
        private BigInteger publicKey;
        private BigInteger modulus;

        @Override
        protected KeyDetails doInBackground(Integer... params) {
            int length = params[0] / 2;
            int r = params[1];
            Log.d("KeyGenTask", "Length: " + length * 2);
            while (!gen(length, r)) ;
            return new KeyDetails(modulus, privateKey, publicKey);
        }

        @Override
        protected void onPostExecute(KeyDetails result) {
            super.onPostExecute(result);
            Log.d("KeyGenTask", "Key generated");
            Log.d("KeyGenTask", "Modulus: " + result.getModulus());
            Log.d("KeyGenTask", "Private key: " + result.getPrivateKey().subtract(TWO));
            Log.d("KeyGenTask", "Public key: " + result.getPublicKey());
            MainActivity.KEY = result;
            progress.startAnimation(slide_out);
            progress.setVisibility(View.GONE);
            encStep.startAnimation(slide_in);
            encStep.setVisibility(View.VISIBLE);
        }

        private boolean gen(int length, int r) {
            try {
                System.out.println("New try");
                BigInteger q = TWO.pow(length);
                q = q.nextProbablePrime();
                BigInteger p = q.multiply(TWO).add(new BigInteger(String.valueOf((int) (7 * Math.pow(10, 6)))));
                p = p.nextProbablePrime();
                BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
                modulus = p.multiply(q);

                Log.d("KeyGenTask", "P: " + p);
                Log.d("KeyGenTask", "Q: " + q);

                int iter = 0;
                BigInteger h = p.divide(q);

                BigDecimal alpha = new BigDecimal(h).add(BigDecimal.ONE).divide(sqrt(new BigDecimal(h)));

                BigDecimal dkp = BigDecimal.ONE.divide(sqrt(alpha.multiply(new BigDecimal(TWO))), 100, BigDecimal.ROUND_FLOOR).multiply(sqrt(sqrt(new BigDecimal(modulus))));
                BigDecimal Kkp = dkp.divide(BigDecimal.valueOf(r));
                boolean found = false;
                BigInteger d0 = dkp.multiply(BigDecimal.valueOf(r)).toBigInteger();
                if (d0.mod(TWO).equals(BigInteger.ZERO)) {
                    d0 = d0.add(BigInteger.ONE);
                }
                for (privateKey = d0; !found; privateKey = privateKey.add(TWO)) {
                    iter++;
                    Log.d("KeyGenTask", "Try: " + iter);
                    if (privateKey.gcd(phi).equals(BigInteger.ONE)) {
                        publicKey = privateKey.modInverse(phi);
                        BigDecimal K = new BigDecimal(publicKey.multiply(privateKey).subtract(BigInteger.ONE).divide(phi));
                        if (K.compareTo(Kkp) < 0) {
                            found = true;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public BigDecimal sqrt(BigDecimal number) {
            RoundingMode rounding = RoundingMode.HALF_UP;
            BigDecimal result = BigDecimal.ZERO;
            BigDecimal guess = BigDecimal.ONE;
            BigDecimal flipA = result;
            BigDecimal flipB = result;
            boolean first = true;
            while (result.compareTo(guess) != 0) {
                if (!first)
                    guess = result;
                else
                    first = false;

                result = number.divide(guess, rounding).add(guess).divide(new BigDecimal(TWO), rounding);
                if (result.equals(flipB))
                    return flipA;

                flipB = flipA;
                flipA = result;
            }
            return result;
        }
    }

    class EncryptTask extends AsyncTask<String, Void, BigInteger> {

        @Override
        protected BigInteger doInBackground(String... params) {
            Log.d("EncryptTask", "String to encrypt: " + params[0]);
            BigInteger message = new BigInteger(params[0]);
            message = message.modPow(KEY.getPublicKey(), KEY.getModulus());
            return message;
        }

        @Override
        protected void onPostExecute(BigInteger result) {
            super.onPostExecute(result);
            Log.d("EncryptTask", "String encrypted: " + result);
            progress.startAnimation(slide_out);
            progress.setVisibility(View.GONE);
            encryptedMessage.setText(result.toString());
            hackStep.startAnimation(slide_in);
            hackStep.setVisibility(View.VISIBLE);
        }

    }

    class HackTask extends AsyncTask<String, Void, BigInteger> {

        @Override
        protected BigInteger doInBackground(String... params) {
            Log.d("HackTask", "String to hack: " + params[0]);
            BigInteger message = new BigInteger(params[0]);
            BigInteger originalMessage = new BigInteger(params[1]);
            System.out.println("M: "+message);
            System.out.println("Original: "+originalMessage);
            KeyDetails key = MainActivity.KEY;
            BigInteger publicKey = key.getPublicKey();
            BigInteger modulus = key.getModulus();

            System.out.println("Test: "+message.modPow(key.getPrivateKey(), modulus));
            List<BigInteger> list = calc(publicKey, modulus);
            System.out.println("Find private key..");

            for(int i = 1; i < list.size(); i++) {
                BigInteger n = list.get(i);
                System.out.println("Try "+n);
                if(message.modPow(n, modulus).equals(originalMessage)) {
                    System.out.println("Decrypted: "+message.modPow(n, modulus));
                    System.out.println("Original: "+originalMessage);
                    System.out.println("Yeah, we found it! Private key is "+n);
                    return n;
                }
            }
            System.out.println("Cannot find private key :(");
            return null;
        }

        @Override
        protected void onPostExecute(BigInteger result) {
            super.onPostExecute(result);
            Log.d("HackTask", "Private key: " + result);
            progress.startAnimation(slide_out);
            progress.setVisibility(View.GONE);

            KeyDetails key = MainActivity.KEY;
            decryptedMessage.setText((new BigInteger(encryptedMessage.getText().toString()).modPow(result, key.getModulus())).toString());
            endStep.startAnimation(slide_in);
            endStep.setVisibility(View.VISIBLE);
        }

        private List<BigInteger> calc(BigInteger publicKey, BigInteger modulus) {
            BigInteger r0 = publicKey;
            BigInteger r1 = modulus;

            BigInteger Q0 = BigInteger.ONE;
            BigInteger Q1 = BigInteger.ZERO;
            BigInteger nil = BigInteger.ZERO;
            List<BigInteger> Q = new ArrayList<>();

            while(!r1.equals(nil)) {
                try {
                    BigInteger r2 = r0.mod(r1);
                    BigInteger q = r0.subtract(r2);
                    q = q.divide(r1);
                    BigInteger Q2 = q.multiply(Q1);
                    Q2 = Q2.add(Q0);
                    Q.add(Q2);
                    r0 = r1;
                    r1 = r2;
                    Q0 = Q1;
                    Q1 = Q2;
                } catch (Exception e) { e.printStackTrace();}
            }
            return Q;
        }
    }
}
