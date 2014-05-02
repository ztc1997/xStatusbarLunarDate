package de.xiaoxia.xstatusbarlunardate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.widget.RelativeLayout;
import android.widget.TextView;

//����xposed������
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/* Main */
public class Main implements IXposedHookLoadPackage{

    /* ��ʼ���� */
    private String lunarText = "LUNAR"; //��¼������ʱ�������ַ���
    private String breaklineText = "\n"; //�Ƿ��е��ı�
    private String lDate = ""; //�ϴμ�¼������
    private String finalText; //��������ı�
    private String year; //��¼���
    private Boolean _layout_run = false; //�ж��Ƿ����ù�singleLine����
    private final static Pattern reg = Pattern.compile("\\n");

    /* ��ȡ���� */
    //ʹ��xposed�ṩ��XSharedPreferences��������ȡandroid���õ�SharedPreferences����
    private final static XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName());

    //���ñ�����¼��ȡ����
    public final static Boolean _remove = prefs.getBoolean("remove", true);
    public final static Boolean _term = prefs.getBoolean("term", true);
    public final static Boolean _fest = prefs.getBoolean("fest", true);
    public final static Boolean _breakline = prefs.getBoolean("breakline", true);
    public final static Boolean _layout_enable = prefs.getBoolean("layout_enable", false);
    public final static Boolean _lockscreen = prefs.getBoolean("lockscreen", false);
    public final static String _minor = prefs.getString("minor", "1");
    public final static int _lang = Integer.valueOf(prefs.getString("lang", "1")).intValue();
    public final static int _year = Integer.valueOf(prefs.getString("year", "1")).intValue();

    //��ʼ��Lunar��
    private Lunar lunar = new Lunar(_lang);
    
  
    //�滻���ں���
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui"))
            return;

        //�������Ƿ��е��ı�������ַ�����
        if(_breakline == false){
            breaklineText = "  ";
        }
        
        //������˵������֣�����������������ֲ���
        if(!_layout_enable){
            _layout_run = true;
        }
        

        //����com.android.systemui.statusbar.policy.DateView�����updateClock()֮��
        findAndHookMethod("com.android.systemui.statusbar.policy.DateView", lpparam.classLoader, "updateClock", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //��ȡԭ����
                TextView textview = (TextView) param.thisObject;    
                String nDate = textview.getText().toString();

                /* �жϵ�ǰ�������Ƿ�����ϴθ��º�������ı�
                 * 1 �������,��˵��ԭ��updateClock()û�б�ִ�У�������ȥ����
                 * 2 �������������˵����Ҫ����д��TextView
                 *  2.1 �����ǰ�����Ѿ��ı䣬��������¼���ũ��
                 *  2.2 �����ǰ����δ�ı䣬��ֻ��Ҫ�������Ѿ�������ı�д��TextView */
                if(!nDate.contains(lunarText)){
                    //�ж������Ƿ�ı䣬���ı��򲻸������ݣ��ı������¼���ũ��
                    if (!nDate.equals(lDate)) {
                        //��ȡʱ��
                        lunar.init(System.currentTimeMillis());

                        //����layout��singleLine����
                        if(_layout_run == false){
                            //ȥ��singleLine����
                            if(prefs.getBoolean("layout_line", false)){
                                textview.setSingleLine(false);
                            }
                            //ȥ��align_baseline������������Ϊcenter_vertical
                            if(prefs.getBoolean("layout_align", false)){
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)textview.getLayoutParams();
                                layoutParams.addRule(RelativeLayout.ALIGN_BASELINE,0);
                                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                                textview.setLayoutParams(layoutParams); 
                            }
                            //���ÿ���Ϊfill_parent
                            if(prefs.getBoolean("layout_width", false)){
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)textview.getLayoutParams();
                                layoutParams.width = -1;
                                textview.setLayoutParams(layoutParams);
                            }
                            _layout_run = true;
                        }
                        
                        //�ж��Ƿ���ũ������
                        String fest = " " + lunar.getLFestivalName();
                        if ((_fest == true) && (!"".equals(fest))){
                            if(fest.equals(" С��")){
                                if((lunar.getLunarDay() == 23 && "1".equals(_minor)) || (lunar.getLunarDay() == 24 && "2".equals(_minor))  || (lunar.getLunarDay() == 25 && "3".equals(_minor))){
                                }else{
                                    fest = " ";
                                }
                            }
                        }else{
                            fest = " ";
                        }

                        //�ж��Ƿ��Ƕ�ʮ�Ľ���
                        String term = " " + lunar.getTermString();
                        if ((_term == true) && (!"".equals(term))){
                            term = " " + lunar.getTermString();
                        }else{
                            term = " ";
                        }

                        //���������������
                        switch(_year){
                            case 1:  year = lunar.getAnimalString() + "��";
                                break;
                            case 2:  year = lunar.getLunarYearString() + "��";
                                break;
                            case 3:  year = "";
                                break;
                            case 4:  year = lunar.getLunarYearString() + "��" + lunar.getAnimalString() + "����";
                                break;
                            default: year = lunar.getAnimalString() + "��";
                        
                        }

                        //���ũ���ı�
                        if(_lang != 3){
                        	lunarText =  year + lunar.getLunarMonthString() + "��" + lunar.getLunarDayString() + fest + term;
                        }else{
                        	lunarText = "[" + lunar.getLunarDay() + "/" + lunar.getLunarMonth() + "]";
                        }
                        
                        //���¼�¼������
                        lDate = nDate;
                        //����������ַ���
                        finalText = nDate + breaklineText + lunarText;
                        //�����Ҫȥ����
                        if(_remove){
                            Matcher mat = reg.matcher(finalText);
                            finalText = mat.replaceFirst(" ");
                        }
                    }
                    //����TextView
                    textview.setText(finalText);
                }
            }
        });
    }
}