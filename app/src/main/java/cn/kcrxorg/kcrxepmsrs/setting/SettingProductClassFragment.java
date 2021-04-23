package cn.kcrxorg.kcrxepmsrs.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import com.tencent.mmkv.MMKV;

import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.widget.picker.XSeekBar;

import butterknife.BindView;
import cn.kcrxorg.kcrxepmsrs.MyApp;
import cn.kcrxorg.kcrxepmsrs.R;
import cn.kcrxorg.kcrxepmsrs.mbutil.XToastUtils;


@Page(name = "软件类型设置",anim = CoreAnim.fade)
public class SettingProductClassFragment extends XPageFragment {
    @BindView(R.id.rb_cbank)
    RadioButton rb_cbank;
    @BindView(R.id.rb_bbank)
    RadioButton rb_bbank;
    @BindView(R.id.rb_encrypt)
    RadioButton rb_encrypt;
    @BindView(R.id.rb_dencrypt)
    RadioButton rb_dencrypt;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    //加密设置
    public  boolean encrypt=true;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_setting_produckclass;
    }

    @Override
    protected void initViews() {
          int productid= MMKV.defaultMMKV().getInt("produckid",MyApp.DEFAULT_PRODUCKCLASS);
          switch (productid)
          {
              case 0:
                  rb_cbank.setChecked(true);
                  break;
              case 1:
                  rb_bbank.setChecked(true);
                  break;
              default:
                  break;
          }

        //初始化配置
        mSharedPreferences = getActivity().getSharedPreferences("UHF", Context.MODE_PRIVATE);

        encrypt=mSharedPreferences.getBoolean("encrypt",true);
        if(encrypt)
        {
            rb_encrypt.setChecked(true);
        }else
        {
            rb_dencrypt.setChecked(true);
        }
    }

    @Override
    protected void initListeners() {
        rb_cbank.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if(buttonView.getId()==R.id.rb_cbank&&isChecked)
                 {
                     MMKV.defaultMMKV().putInt("produckid",0);
                 }
            }
        });
        rb_bbank.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.getId()==R.id.rb_bbank&&isChecked)
                {
                    MMKV.defaultMMKV().putInt("produckid",1);
                }
            }
        });
        rb_encrypt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.getId()==R.id.rb_encrypt)
                {
                    encrypt=false;
                    mEditor=mSharedPreferences.edit();
                    mEditor.putBoolean("encrypt", encrypt);
                    mEditor.commit();
                    XToastUtils.info("进入解密模式");
                }
            }
        });

        rb_dencrypt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.getId()==R.id.rb_dencrypt)
                {
                    encrypt=true;
                    mEditor=mSharedPreferences.edit();
                    mEditor.putBoolean("encrypt", encrypt);
                    mEditor.commit();
                    XToastUtils.info("进入加密模式");
                }
            }
        });

    }
}
