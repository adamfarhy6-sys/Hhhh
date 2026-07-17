package com.adam.worldclock;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends Activity {
  static final int BG=Color.rgb(11,16,32), CARD=Color.rgb(18,26,45), TEXT=Color.rgb(246,248,255), MUTED=Color.rgb(170,182,211), ACCENT=Color.rgb(124,215,255);
  final Handler handler=new Handler(Looper.getMainLooper());
  final List<Zone> all=new ArrayList<>(), shown=new ArrayList<>();
  TextView localTime, localDate, count, toggle; Adapter adapter; boolean h24=true;
  final Runnable tick=new Runnable(){public void run(){refresh();handler.postDelayed(this,1000-(System.currentTimeMillis()%1000));}};

  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);loadZones();setContentView(screen());filter("");}
  @Override protected void onResume(){super.onResume();handler.post(tick);} 
  @Override protected void onPause(){handler.removeCallbacks(tick);super.onPause();}

  View screen(){
    LinearLayout root=box(LinearLayout.VERTICAL);root.setBackgroundColor(BG);root.setPadding(dp(16),dp(14),dp(16),0);
    LinearLayout head=box(LinearLayout.HORIZONTAL);head.setGravity(Gravity.CENTER_VERTICAL);
    TextView title=t("World Time",28,TEXT,Typeface.BOLD);head.addView(title,new LinearLayout.LayoutParams(0,-2,1));
    toggle=t("24H",15,ACCENT,Typeface.BOLD);toggle.setGravity(Gravity.CENTER);toggle.setPadding(dp(14),dp(8),dp(14),dp(8));toggle.setBackground(round(CARD,18,Color.rgb(42,56,90)));
    toggle.setOnClickListener(v->{h24=!h24;toggle.setText(h24?"24H":"12H");refresh();});head.addView(toggle);root.addView(head);
    TextView sub=t("Live clocks across the world",14,MUTED,0);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-2,-2);sp.bottomMargin=dp(14);root.addView(sub,sp);

    LinearLayout local=box(LinearLayout.VERTICAL);local.setPadding(dp(20),dp(18),dp(20),dp(18));local.setBackground(round(Color.rgb(28,49,88),24,0));
    local.addView(t("YOUR LOCAL TIME",12,ACCENT,Typeface.BOLD));localTime=t("--:--:--",39,TEXT,Typeface.BOLD);local.addView(localTime);localDate=t("",14,MUTED,0);local.addView(localDate);
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(14);root.addView(local,lp);

    EditText search=new EditText(this);search.setSingleLine();search.setHint("Search city or region…");search.setHintTextColor(Color.rgb(119,135,170));search.setTextColor(TEXT);search.setTextSize(16);search.setPadding(dp(16),dp(10),dp(16),dp(10));search.setBackground(round(CARD,18,Color.rgb(42,56,90)));
    search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){}public void onTextChanged(CharSequence s,int a,int b,int c){filter(s.toString());}public void afterTextChanged(Editable e){}});
    LinearLayout.LayoutParams qp=new LinearLayout.LayoutParams(-1,-2);qp.bottomMargin=dp(9);root.addView(search,qp);
    count=t("",13,MUTED,0);root.addView(count);
    ListView list=new ListView(this);list.setDivider(null);list.setDividerHeight(0);list.setPadding(0,dp(8),0,dp(18));list.setClipToPadding(false);adapter=new Adapter();list.setAdapter(adapter);root.addView(list,new LinearLayout.LayoutParams(-1,0,1));return root;
  }

  void loadZones(){long now=System.currentTimeMillis();for(String id: Arrays.asList(TimeZone.getAvailableIDs())){if(!id.contains("/")||id.startsWith("Etc/")||id.startsWith("SystemV/")||id.startsWith("US/")||id.startsWith("Canada/"))continue;all.add(new Zone(id));}all.sort(Comparator.comparingInt((Zone z)->z.zone.getOffset(now)).thenComparing(z->z.city));}
  void filter(String q){q=q.trim().toLowerCase(Locale.ROOT);shown.clear();for(Zone z:all)if(q.isEmpty()||z.search.contains(q))shown.add(z);if(count!=null)count.setText(shown.size()+" world time zones");if(adapter!=null)adapter.notifyDataSetChanged();}
  void refresh(){Date now=new Date();TimeZone local=TimeZone.getDefault();localTime.setText(time(now,local));SimpleDateFormat d=new SimpleDateFormat("EEEE, d MMMM yyyy",Locale.getDefault());d.setTimeZone(local);localDate.setText(d.format(now)+"  •  "+pretty(local.getID())+"  •  "+offset(local,now.getTime()));if(adapter!=null)adapter.notifyDataSetChanged();}
  String time(Date d,TimeZone z){SimpleDateFormat f=new SimpleDateFormat(h24?"HH:mm:ss":"h:mm:ss a",Locale.getDefault());f.setTimeZone(z);return f.format(d);} 
  String date(Date d,TimeZone z){SimpleDateFormat f=new SimpleDateFormat("EEE, d MMM",Locale.getDefault());f.setTimeZone(z);return f.format(d);} 
  static String offset(TimeZone z,long n){int m=z.getOffset(n)/60000,a=Math.abs(m);return String.format(Locale.ROOT,"UTC%c%02d:%02d",m>=0?'+':'-',a/60,a%60);} 
  static String pretty(String id){String[] p=id.split("/");return p[p.length-1].replace('_',' ');} 
  static String region(String id){String[] p=id.split("/");return p[0].replace('_',' ')+(p.length>2?" • "+p[1].replace('_',' '):"");}
  TextView t(String s,int size,int color,int style){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setTypeface(Typeface.create("sans",style));return v;}
  LinearLayout box(int orientation){LinearLayout l=new LinearLayout(this);l.setOrientation(orientation);return l;}
  GradientDrawable round(int color,int radius,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));if(stroke!=0)g.setStroke(dp(1),stroke);return g;}
  int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}

  static class Zone{final String id,city,region,search;final TimeZone zone;Zone(String id){this.id=id;zone=TimeZone.getTimeZone(id);city=pretty(id);region=region(id);search=(id+" "+city+" "+region).toLowerCase(Locale.ROOT);}}
  class Adapter extends BaseAdapter{
    public int getCount(){return shown.size();}public Zone getItem(int p){return shown.get(p);}public long getItemId(int p){return p;}
    public View getView(int p,View old,ViewGroup parent){Holder h;if(old==null){LinearLayout outer=box(LinearLayout.VERTICAL);outer.setPadding(0,0,0,dp(8));LinearLayout row=box(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(16),dp(13),dp(16),dp(13));row.setBackground(round(CARD,16,0));LinearLayout left=box(LinearLayout.VERTICAL);h=new Holder();h.city=t("",18,TEXT,Typeface.BOLD);h.region=t("",12,MUTED,0);left.addView(h.city);left.addView(h.region);row.addView(left,new LinearLayout.LayoutParams(0,-2,1));LinearLayout right=box(LinearLayout.VERTICAL);right.setGravity(Gravity.END);h.time=t("",20,ACCENT,Typeface.BOLD);h.time.setGravity(Gravity.END);h.detail=t("",12,MUTED,0);h.detail.setGravity(Gravity.END);right.addView(h.time);right.addView(h.detail);row.addView(right);outer.addView(row,new LinearLayout.LayoutParams(-1,-2));outer.setTag(h);old=outer;}else h=(Holder)old.getTag();Zone z=getItem(p);Date n=new Date();h.city.setText(z.city);h.region.setText(z.region+"  •  "+z.id);h.time.setText(time(n,z.zone));h.detail.setText(date(n,z.zone)+"  •  "+offset(z.zone,n.getTime()));return old;}
  }
  static class Holder{TextView city,region,time,detail;}
}
