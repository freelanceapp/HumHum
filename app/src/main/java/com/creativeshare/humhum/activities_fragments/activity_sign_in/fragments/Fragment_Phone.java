package com.creativeshare.humhum.activities_fragments.activity_sign_in.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.creativeshare.humhum.R;
import com.creativeshare.humhum.activities_fragments.activity_home.client_home.activity.ClientHomeActivity;
import com.creativeshare.humhum.activities_fragments.activity_sign_in.activity.SignInActivity;
import com.creativeshare.humhum.models.UserModel;
import com.creativeshare.humhum.remote.Api;
import com.creativeshare.humhum.share.Common;
import com.creativeshare.humhum.tags.Tags;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.listeners.OnCountryPickerListener;

import java.io.IOException;
import java.util.Locale;

import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Phone extends Fragment implements OnCountryPickerListener {
    private static final String TAG = "TYPE";
    private ImageView arrow;
    private LinearLayout ll_country;
    private TextView tv_country,tv_code,tv_note;
    private EditText edt_phone;
    private FloatingActionButton fab;
    private FragmentActivity activity;
    private CountryPicker picker;
    private String current_language="";
    private String code = "";
    private String country_code="sa";
    // from where , access from fragment chooser , fragment edit profile
    private String type;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone,container,false);
        initView(view);
        return view;
    }

    public static Fragment_Phone newInstance(String type)
    {
        Bundle bundle = new Bundle();
        bundle.putString(TAG,type);
        Fragment_Phone fragment_phone = new Fragment_Phone();
        fragment_phone.setArguments(bundle);
        return fragment_phone;
    }
    private void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle!=null)
        {
            type = bundle.getString(TAG);

            if (type.equals("signup"))
            {
                activity =  getActivity();

            }else if (type.equals("edit_profile"))
            {
                activity = getActivity();

            }

        }

        Paper.init(activity);
        current_language = Paper.book().read("lang", Locale.getDefault().getLanguage());
        arrow = view.findViewById(R.id.arrow);
        ll_country = view.findViewById(R.id.ll_country);
        tv_country = view.findViewById(R.id.tv_country);
        tv_code = view.findViewById(R.id.tv_code);
        tv_note = view.findViewById(R.id.tv_note);

        edt_phone = view.findViewById(R.id.edt_phone);
        fab = view.findViewById(R.id.fab);


        if (current_language.equals("ar"))
        {
            arrow.setImageResource(R.drawable.ic_right_arrow);
            arrow.setColorFilter(ContextCompat.getColor(activity,R.color.white), PorterDuff.Mode.SRC_IN);
        }else
            {
                arrow.setImageResource(R.drawable.ic_left_arrow);
                arrow.setColorFilter(ContextCompat.getColor(activity,R.color.white), PorterDuff.Mode.SRC_IN);

            }
        tv_note.setText(getString(R.string.never_share_phone_number)+"\n"+getString(R.string.your_privacy_guaranteed));

        CreateCountryDialog();

        ll_country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.showDialog(activity);
            }


        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckData();
            }
        });


    }


    private void CheckData() {
        String phone_regex = "^[+]?[0-9]{6,}$";

        String phone = edt_phone.getText().toString().trim();

        if (phone.startsWith("0"))
        {
            phone = phone.replaceFirst("0","");
        }

        if (!TextUtils.isEmpty(phone) && phone.matches(phone_regex)) {
            edt_phone.setError(null);
            Common.CloseKeyBoard(activity, edt_phone);
            if (type.equals("signup"))
            {

                checkFound(code,phone);
                //sendSMSCode(code,phone);
                //((SignInActivity)activity).signIn(phone,country_code,code);

            }else if (type.equals("edit_profile"))
            {
                ((ClientHomeActivity)activity).setPhoneData(code,country_code,phone);
            }
        } else {
            if (TextUtils.isEmpty(phone)) {
                edt_phone.setError(getString(R.string.field_req));
            } else if (!phone.matches(phone_regex)) {
                edt_phone.setError(getString(R.string.inv_phone));
            }else
                {
                    edt_phone.setError(null);
                }
        }
    }
    private void checkFound(String phone_code, final String phone) {
        final ProgressDialog dialog = Common.createProgressDialog(activity,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url)
                .checkfound(phone_code.replace("+","00"),phone)
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {


                        dialog.dismiss();

                        if (response.isSuccessful())
                        {
                            ((SignInActivity)activity).signIn(response.body());

                        }else if (response.code()==406)
                        {
                            Toast.makeText(activity, getString(R.string.user_bloked), Toast.LENGTH_SHORT).show();
                        }else
                        {
                            Log.e("error_code",response.code()+"_");

                            if (response.code()==401)
                            {
                                sendSMSCode(phone_code,phone);
                                //((SignInActivity)activity).DisplayFragmentCodeVerification(code.replace("+","00"),phone,country_code);
                            }else
                            {
                                Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            Toast.makeText(activity, getString(R.string.something), Toast.LENGTH_SHORT).show();
                            Log.e("Error",t.getMessage());


                        }catch (Exception e){}
                    }
                });
    }
    private void sendSMSCode(String phone_code, final String phone) {
        final ProgressDialog dialog = Common.createProgressDialog(activity,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url)
                .getSmsCode(phone_code.replace("+","00"),phone)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        dialog.dismiss();

                        if (response.isSuccessful())
                        {
                            ((SignInActivity)activity).DisplayFragmentCodeVerification(code.replace("+","00"),phone,country_code);
                        }else
                        {
                            try {
                                Log.e("error_code",response.code()+"_"+response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (response.code()==404)
                            {
                                Common.CreateSignAlertDialog(activity,getString(R.string.inc_code_verification));
                            }else
                            {
                                Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            Toast.makeText(activity, getString(R.string.something), Toast.LENGTH_SHORT).show();
                            Log.e("Error",t.getMessage());


                        }catch (Exception e){}
                    }
                });
    }


    private void CreateCountryDialog() {
        CountryPicker.Builder builder = new CountryPicker.Builder()
                .canSearch(true)
                .with(activity)
                .listener(this);
        picker = builder.build();

        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);


        if (picker.getCountryFromSIM() != null) {
            updateUi(picker.getCountryFromSIM());

        } else if (telephonyManager != null && picker.getCountryByISO(telephonyManager.getNetworkCountryIso()) != null)
        {
            updateUi(picker.getCountryByISO(telephonyManager.getNetworkCountryIso()));


        } else if (picker.getCountryByLocale(Locale.getDefault()) != null) {
            updateUi(picker.getCountryByLocale(Locale.getDefault()));

        }else
        {
            tv_code.setText("+966");
            tv_country.setText("Saudi Arabia");
            this.country_code = "sa";

        }


    }
    @Override
    public void onSelectCountry(Country country) {
        updateUi(country);
    }

    private void updateUi(Country country) {
        country_code = country.getCode();
        tv_country.setText(country.getName());
        tv_code.setText(country.getDialCode());
        code = country.getDialCode();




    }


}
