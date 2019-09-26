package allinontech.anonyma.backend;

import android.content.Context;

public class SharedPreference {

    android.content.SharedPreferences pref;
    android.content.SharedPreferences.Editor editor;
    Context _context;
    private static final String PREF_NAME = "Anonyma";
    // All Shared Preferences Keys Declare as #public
    public static final String KEY_CHECK_LOGIN="NO";
	public static final String KEY_UID = "THEUID";
	public static final String KEY_CITYNAME = "THECITY";
	public static final String KEY_UNAME = "THEUNAME";
	public static final String KEY_FCMTOKEN = "FCMTOKEN";
	public static final String KEY_FIRST_TIME = "KEYRUNFIRSTTIME";


    public SharedPreference(Context context) // Constructor
    {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, 0);
        editor = pref.edit();

    }

	public void setRunFirst( boolean temp)
	{
		if( temp){
			editor.remove(KEY_FIRST_TIME);
			editor.putString(KEY_FIRST_TIME, "F");
			editor.commit();
		}
		else{
			editor.remove(KEY_FIRST_TIME);
			editor.putString(KEY_FIRST_TIME, "S");
			editor.commit();
		}

	}

	public boolean getRunFirst()
	{
		String  App_runFirst= pref.getString( KEY_FIRST_TIME, "F");
		if( App_runFirst.equals( "F")){
			setRunFirst( false);
			return true;
		}
		else
			return false;
	}
	public void setCity(String city)
	{
		editor.remove(KEY_CITYNAME);
		editor.putString(KEY_CITYNAME, city.trim());
		editor.commit();
	}
	public String getCity(){
		String city= pref.getString(KEY_CITYNAME, "NULL");
		return city;
	}
	public void setFCM(String fcm)
	{
		editor.remove(KEY_FCMTOKEN);
		editor.putString(KEY_FCMTOKEN, fcm.trim());
		editor.commit();
	}
	public String getFCM(){
		String city= pref.getString(KEY_FCMTOKEN, "");
		return city;
	}
	public void setUName(String uname)
	{
		editor.remove(KEY_UNAME);
		editor.putString(KEY_UNAME, uname.trim());
		editor.commit();
	}
	public String getUName(){
		String uname= pref.getString(KEY_UNAME, "NULL");
		return uname;
	}
	public void setUid(String uid)
	{
		editor.remove(KEY_UID);
		editor.putString(KEY_UID, uid.trim());
		editor.commit();
	}
	public String getUid(){
		String uid= pref.getString(KEY_UID, "NULL");
		return uid;
	}

    public void setLogin(boolean login)
    {
        editor.remove(KEY_CHECK_LOGIN);
        if( login){
            editor.putString(KEY_CHECK_LOGIN, "YES");
        }
        else
            editor.putString(KEY_CHECK_LOGIN, "NO");

        editor.commit();
    }
    public boolean isLoggedIn(){
        String loggedIn= pref.getString(KEY_CHECK_LOGIN, "NO");
        if( loggedIn.equals( "YES")){
            return true;
        }
        else{
            return false;
        }
    }
}