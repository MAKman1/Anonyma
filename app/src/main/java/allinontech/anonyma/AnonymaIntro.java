package allinontech.anonyma;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;


public class AnonymaIntro extends AppCompatActivity {

    ViewPager slideViewPager;

    TextView textLine1, textLine2;

    Button skip, next;

    MyPagerAdapter adapter;

    public static final int THE_SIZE = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonyma_intro);

        slideViewPager = findViewById( R.id.slideViewPager);
        textLine1 = findViewById( R.id.textLine1);
        textLine2 = findViewById( R.id.textLine2);
        skip = findViewById( R.id.skip);
        next = findViewById( R.id.next);


        adapter = new MyPagerAdapter( AnonymaIntro.this);

        slideViewPager.setAdapter( adapter);
        slideViewPager.addOnPageChangeListener( new DetailOnPageChangeListener( textLine1, textLine2, next, skip));

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = slideViewPager.getCurrentItem() + 1;
                if( temp > THE_SIZE){
                    onBackPressed();
                }
                else if( temp == THE_SIZE){
                    slideViewPager.setCurrentItem( temp, true);
                    next.setText( "FINISH");
                    skip.setVisibility( View.GONE);
                }
                else{
                    slideViewPager.setCurrentItem( temp, true);
                    next.setText( "NEXT");
                    skip.setVisibility( View.VISIBLE);
                }
            }
        });
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    @Override
    public void onBackPressed(){
        finish();
    }
}

class MyPagerAdapter extends PagerAdapter {
    Context context;


    public static int[] layouts = { R.layout.intro_screen_welcome, R.layout.intro_screen_1, R.layout.intro_screen_2,R.layout.intro_screen_3,
            R.layout.intro_screen_4, R.layout.intro_screen_5, R.layout.intro_screen_6, R.layout.intro_screen_7, R.layout.intro_screen_8};

    public MyPagerAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return layouts.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view==(RelativeLayout)o;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {

        Log.d( "ABBA", "shit started");
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate( layouts[position], container, false);


        container.addView( v);

        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d( "ABBA", "shit ended");
        container.removeView((RelativeLayout)object);
    }
}
class DetailOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

    TextView text1;
    TextView text2;

    Button next, skip;

    public static String[] line1= {"LET'S GET YOU", "LET YOUR WILDEST", "NO EMAIL/ NO NAMES", "LIKE/ COMMENT ON", "CHAT WITH RANDOM",
            "EASILY POST", "SEE SECRETS FROM", "SEARCH SECRETS", "VIEW USER PROFILES/"};

    public static String[] line2= {"ACQUAINTED!", "SECRETS OUT!", "NO IDENTITY!", "SECRETS ANONYMOUSLY.", "STRANGERS", "ANY SECRET",
            "USERS AROUND YOU.", "SUBSCRIBE TO TAGS", "CHAT WITH THEM"};

    public static final int THE_SIZE = 8;

    public DetailOnPageChangeListener( TextView text1, TextView text2, Button next, Button skip){
        this.text1 = text1;
        this.text2 = text2;
        this.next = next;
        this.skip = skip;
    }

    @Override
    public void onPageSelected(int position) {
        text1.setText( line1[position]);
        text2.setText( line2[position]);


        if( position == THE_SIZE){
            next.setText( "FINISH");
            skip.setVisibility( View.GONE);
        }
        else{
            next.setText( "NEXT");
            skip.setVisibility( View.VISIBLE);
        }
    }
}

