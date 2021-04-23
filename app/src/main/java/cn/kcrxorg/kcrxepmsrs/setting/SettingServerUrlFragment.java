package cn.kcrxorg.kcrxepmsrs.setting;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.tencent.mmkv.MMKV;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.widget.textview.supertextview.SuperButton;


import butterknife.BindView;
import cn.kcrxorg.kcrxepmsrs.MyApp;
import cn.kcrxorg.kcrxepmsrs.R;
import cn.kcrxorg.kcrxepmsrs.mbutil.XToastUtils;


@Page(name = "服务器地址设置",anim = CoreAnim.fade)
public class SettingServerUrlFragment extends XPageFragment {


    @BindView(R.id.btn_savesetting)
    SuperButton btn_savesetting;
    @BindView(R.id.et_serverurl)
    EditText et_serverurl;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_setting_serverurl;
    }

    @Override
    protected void initViews() {
        et_serverurl.setText(MMKV.defaultMMKV().decodeString("serverurl", MyApp.DEFAULT_SERVER_URL));
    }

    @Override
    protected void initListeners() {
        et_serverurl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btn_savesetting.setEnabled(true);
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btn_savesetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MMKV.defaultMMKV().encode("serverurl",et_serverurl.getText().toString());
                XToastUtils.success("保存设置成功");
                btn_savesetting.setEnabled(false);
            }
        });
    }
}
