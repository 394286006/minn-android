package p.minn.security;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import p.minn.R;
import p.minn.http.HttpService;
import p.minn.main.MainActivity;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * @author minn
 * @QQ:3942986006
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {


    private static final int REQUEST_READ_CONTACTS = 0;

    private static final List<Map<String, Object>> localarr = new ArrayList<Map<String, Object>>();
    private static Map<Integer, String> langkey = new HashMap<Integer, String>();
    private static ArrayAdapter<String> localArrayAdapter;
    private static String[] localStrs;
    private UserLoginService userLoginService;


    // UI references.
    private AutoCompleteTextView mNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private static String localeLang = "zh";

    static {
        Map<String, Object> local = new HashMap<String, Object>();
        local.put("name", "中文");
        local.put("locale", "zh_CN");
        local.put("isLoad", false);
        local.put("image", R.drawable.cn);
        localarr.add(local);

        local = new HashMap<String, Object>();
        local.put("name", "English");
        local.put("locale", "en_US");
        local.put("isLoad", false);
        local.put("image", R.drawable.us);
        localarr.add(local);

        local = new HashMap<String, Object>();
        local.put("name", "German (Germany)");
        local.put("locale", "de_DE");
        local.put("isLoad", false);
        local.put("image", R.drawable.de);
        localarr.add(local);

        local = new HashMap<String, Object>();
        local.put("name", "French (France)");
        local.put("locale", "fr_FR");
        local.put("isLoad", false);
        local.put("image", R.drawable.fr);
        localarr.add(local);

        local = new HashMap<String, Object>();
        local.put("name", "Japanese (Japan)");
        local.put("locale", "ja_JP");
        local.put("isLoad", false);
        local.put("image", R.drawable.jp);
        localarr.add(local);

        local = new HashMap<String, Object>();
        local.put("name", "русский (Россия)");
        local.put("locale", "ru_RU");
        local.put("isLoad", false);
        local.put("image", R.drawable.ru);
        localarr.add(local);

        local = new HashMap<String, Object>();
        local.put("name", "한국의 (한국)");
        local.put("locale", "ko_KR");
        local.put("isLoad", false);
        local.put("image", R.drawable.kr);
        localarr.add(local);
        localStrs = new String[localarr.size()];
        Iterator<Map<String, Object>> it = localarr.iterator();
        for (int i = 0; i < localarr.size(); i++) {
            String name = localarr.get(i).get("name").toString();
            langkey.put(i, name);
            localStrs[i] = name;
            String locale = localarr.get(i).get("locale").toString();

        }
    }

    private int lastpos = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        handleChangeLanguage();

        mNameView = (AutoCompleteTextView) findViewById(R.id.name);

        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        //   handleChangeLanguage("en_US");
        String curLocal = getLocal();
        Iterator<Map<String, Object>> it = localarr.iterator();
        for (int i = 0; i < localarr.size(); i++) {
            String locale = localarr.get(i).get("locale").toString();
            if (locale.indexOf(curLocal) != -1) {
                lastpos = i;
            }
        }
        Spinner spin = (Spinner) findViewById(R.id.spinner);
        localArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, localStrs);
        spin.setAdapter(localArrayAdapter);

        spin.setSelection(lastpos);
        spin.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                System.out.println("pos:" + arg2 + "," + langkey.get(arg2));
                String lang = localarr.get(arg2).get("locale").toString();
                System.out.println("selectedLang:" + lang.split("_")[0]);

                localeLang = lang.split("_")[0];
                ImageView spin_img = (ImageView) findViewById(R.id.spinner_img);
                //spin_img.setImageURI("@drawable/en");
                int resId = Integer.valueOf(localarr.get(arg2).get("image").toString());
                spin_img.setImageResource(resId);

                if (lastpos != -1 && lastpos != arg2) {
                    lastpos = arg2;
                    restartSys();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mNameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    private void attemptLogin() {
        if (userLoginService != null) {
            return;
        }

        mNameView.setError(null);
        mPasswordView.setError(null);

        String name = mNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (!isNameValid(name)) {
            mNameView.setError(getString(R.string.error_invalid_name));
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            userLoginService=new UserLoginService();
            List <NameValuePair> params=new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("username",name));
            params.add(new BasicNameValuePair("password",password));
            userLoginService.post("j_spring_security_check",params);
        }
    }

    private boolean isNameValid(String name) {
        //TODO: Replace this with your own logic
        return name.length() > 3;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Nickname
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> names = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            names.add(cursor.getString(ProfileQuery.NAME));
            cursor.moveToNext();
        }

        addNamesToAutoComplete(names);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addNamesToAutoComplete(List<String> nameAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, nameAddressCollection);

        mNameView.setAdapter(adapter);
    }



    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Nickname.NAME,
                ContactsContract.CommonDataKinds.Nickname._ID,
        };

        int NAME = 0;
        int IS_PRIMARY = 1;
    }

    public class UserLoginService extends HttpService {


        @Override
        protected void onSuccess(JSONObject result) {
            LoginActivity.this.mLoginFormView.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            //  intent.putExtra("lang","zh_CN");
            startActivity(intent);
            showProgress(false);
        }
        @Override
        protected void onCancelled() {
            userLoginService = null;
            showProgress(false);
        }
    }



    private void handleChangeLanguage() {

        Locale myLocale = new Locale(localeLang);
        Locale.setDefault(myLocale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(myLocale);
        res.updateConfiguration(conf, dm);
    }

    private void restartSys() {

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    public String getLocal() {
        return Locale.getDefault().getLanguage();
    }

}

