package allinontech.anonyma;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import allinontech.anonyma.backend.MyCallback;
import allinontech.anonyma.backend.SharedPreference;
import allinontech.anonyma.backend.User;
import allinontech.anonyma.backend.Util;
import allinontech.anonyma.elements.AnonymaViewPager;
import allinontech.anonyma.elements.OnSwipeTouchListener;
import allinontech.anonyma.fragments.ConfessionsFragment;
import allinontech.anonyma.fragments.LatestFragment;
import allinontech.anonyma.fragments.LocalFragment;
import allinontech.anonyma.fragments.TagsFragment;


public class HomeScreen extends AppCompatActivity {
    int[] Icons;
    int[] selectedIcons;

    Button menuOpen, topMenu;
    Button chatActivity;
    Button bottomMenu1, bottomMenu2, bottomMenu3, bottomMenu4;
    Button bottom1, bottom2, bottom3, bottom4;
    ArrayList<Button> bottomButtons;
    ArrayList<Button> bottom;
    AnonymaViewPager mainViewPager;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView menuName, menuCity;

    RelativeLayout homeMainLayout;

    public static String UserId = "NULL";
    public static String userName = "NULL";
    public static String userCity = "NULL";
    public static String FCMToken = "NULL";
    public static FirebaseFirestore firestore;


    ViewPagerAdapter adapter;

    private SharedPreference sharedPreferenceObj;

    int screenWidth;
    String[] fragmentTags = {"Latest Secrets from the world", "Secrets in your locality", "Confessions", "Secret tags"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        Icons = new int[]{R.drawable.trending, R.drawable.ic_location_onselected_24dp, R.drawable.confessions, R.drawable.ic_loyalty_selected_24dp};
        selectedIcons = new int[]{R.drawable.trendingselected, R.drawable.ic_location_black_24dp, R.drawable.confessions_selected, R.drawable.ic_loyalty_black_24dp};

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;

        menuOpen = findViewById( R.id.centerMenu);
        topMenu = findViewById( R.id.topMenu);
        mainViewPager = findViewById( R.id.mainViewPager);
        bottomButtons = new ArrayList<>();
        bottom = new ArrayList<>();

        bottomMenu1 = findViewById( R.id.bottomMenu1);
        bottomMenu2 = findViewById( R.id.bottomMenu2);
        bottomMenu3 = findViewById( R.id.bottomMenu3);
        bottomMenu4 = findViewById( R.id.bottomMenu4);
        bottom1 = findViewById( R.id.bottom1);
        bottom2 = findViewById( R.id.bottom2);
        bottom3 = findViewById( R.id.bottom3);
        bottom4 = findViewById( R.id.bottom4);

        chatActivity = findViewById( R.id.chat_activity);
        homeMainLayout = findViewById( R.id.homeMainLayout);
        navigationView = findViewById( R.id.navigationView);
        drawerLayout = findViewById( R.id.drawerLayout);
        menuCity = navigationView.getHeaderView( 0).findViewById( R.id.city_menu);
        menuName = navigationView.getHeaderView( 0).findViewById( R.id.name_menu);

        firestore = Util.getFirestore();


		navigationView.setNavigationItemSelectedListener( new NavigationListener());


        bottomButtons.add( bottomMenu1);
        bottomButtons.add( bottomMenu2);
        bottomButtons.add( bottomMenu3);
        bottomButtons.add( bottomMenu4);

        bottom.add( bottom1);
        bottom.add( bottom2);
        bottom.add( bottom3);
        bottom.add( bottom4);



        menuOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiag();
            }
        });
        setupViewPager( mainViewPager);
        setUpBottomMenu();
        setupButtonColors( mainViewPager.getCurrentItem());



        sharedPreferenceObj = new SharedPreference(HomeScreen.this);
        UserId = sharedPreferenceObj.getUid();
        userCity = sharedPreferenceObj.getCity().toLowerCase();
        userName = sharedPreferenceObj.getUName();
        FCMToken = sharedPreferenceObj.getFCM();
        //if( FCMToken == null || FCMToken.equals( "NULL") || FCMToken.equals( "")){}
        updateToken();

		menuCity.setText( userCity.toUpperCase());
		menuName.setText( userName);


        topMenu.setOnClickListener( new openNavigationListener());

        chatActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createIntentForChat();
                openActivityFromRight();
            }
        });
        homeMainLayout.setOnTouchListener(new OnSwipeTouchListener(HomeScreen.this) {
            public void onSwipeLeft() {
                Log.d( "DADDY", "right");
                createIntentForChat();
                openActivityFromRight();
            }
            /*
            public void onSwipeTop() {
                Toast.makeText(MyActivity.this, "top", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeRight() {
                Toast.makeText(MyActivity.this, "right", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeBottom() {
                Toast.makeText(MyActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }
            */
        });


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void setUpBottomMenu(){
        for( int i = 0; i < 4; i++){
            final int value = i;
            bottom.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainViewPager.setCurrentItem( value, true);
                    Toast.makeText( HomeScreen.this, fragmentTags[value], Toast.LENGTH_SHORT).show();
                    setupButtonColors( value);
                }
            });
        }
        mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                setupButtonColors( position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }
    public void setupButtonColors( int value){
        for( int i = 0; i < 4; i++){
            if( i == value){
                bottomButtons.get(i).setBackground(ContextCompat.getDrawable( HomeScreen.this, selectedIcons[ i]));
            }
            else
                bottomButtons.get(i).setBackground(ContextCompat.getDrawable( HomeScreen.this, Icons[ i]));
        }
    }

    private void setupViewPager(ViewPager viewPager) {

        adapter = new ViewPagerAdapter( getSupportFragmentManager());
        adapter.addFragment(new LatestFragment(), "LATEST");
        adapter.addFragment(new LocalFragment(), "LOCAL");
        adapter.addFragment(new ConfessionsFragment(), "CONFESSIONS");
        adapter.addFragment(new TagsFragment(), "TAGS");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }



        private void showDiag() {
        final View dialogView = View.inflate(this,R.layout.menubg,null);
        final Dialog dialog = new Dialog(this,R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        Button close = (Button)dialog.findViewById(R.id.closeMenu);
        Button newPost = ( Button) dialog.findViewById( R.id.newpostbutton);
        RelativeLayout menuwhite = dialog.findViewById( R.id.menuCircle);

        menuwhite.setLayoutParams( new RelativeLayout.LayoutParams( screenWidth, screenWidth));
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) menuwhite.getLayoutParams();
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.setMargins(0,0,0,-(screenWidth/2));//-(screenWidth/2)
        menuwhite.setLayoutParams(lp);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealShow(dialogView, false, dialog);
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                revealShow(dialogView, true, null);
            }
        });
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK){
                    revealShow(dialogView, false, dialog);
                    return true;
                }
                return false;
            }
        });
        newPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent( HomeScreen.this, NewPostActivity.class));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        revealShow(dialogView, false, dialog);
                    }
                }, 800);
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    private void createIntentForChat() {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    /**
     * slide new activity from right
     */
    protected void openActivityFromRight() {
        //overridePendingTransition( 0, 0);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    private void revealShow(View dialogView, boolean b, final Dialog dialog) {
        final View view = dialogView.findViewById(R.id.menubg);
        int w = view.getWidth();
        int h = view.getHeight();
        int endRadius = (int) Math.hypot(w, h);
        int cx = (int) (menuOpen.getX() + (menuOpen.getWidth()/2));
        int cy = (int) (menuOpen.getY())+ menuOpen.getHeight() + 56;
        if(b){
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx,cy, 0, endRadius);
            view.setVisibility(View.VISIBLE);
            revealAnimator.setDuration(300);
            revealAnimator.start();
        } else {
            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, endRadius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dialog.dismiss();
                    view.setVisibility(View.INVISIBLE);
                }
            });
            anim.setDuration(300);
            anim.start();
        }
    }

    public class openNavigationListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
			drawerLayout.openDrawer( GravityCompat.START);
        }
    }
    class NavigationListener implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navItemLogout:
					menuItem.setChecked(true);
					drawerLayout.closeDrawers();
                    sharedPreferenceObj.setLogin( false);
                    Intent intent = new Intent(HomeScreen.this, Login.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.navItemSetting:
                    Intent i = new Intent(HomeScreen.this, Settings.class);
                    startActivity(i);
                    break;
                case R.id.navItemViewProfile:
                    Intent a = new Intent( HomeScreen.this, UserPosts.class);
                    a.putExtra("USER_ID", UserId);
                    a.putExtra("USER_NAME", userName);
                    startActivity(a);
                    break;
            }

            // Menu item clicked on, and close Drawerlayout
            menuItem.setChecked(true);
            drawerLayout.closeDrawers();
            return true;
        }
    }
	@Override
	public void onBackPressed() {
		if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
			this.drawerLayout.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

    public void updateToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w( "ABBA", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d( "ABBA", msg);

                        FCMToken = token;
                        if( sharedPreferenceObj != null)
                            sharedPreferenceObj.setFCM( FCMToken);

                        if( FCMToken != null && !FCMToken.equals("")){
                            if( firestore == null)
                                firestore = Util.getFirestore();

                            firestore.collection("users").document( HomeScreen.UserId)
                                    .update( "fcm_token", token.trim())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d( "ABBA", "FCM token updated");
                                        }
                                    });
                        }
                    }
                });
    }
}
