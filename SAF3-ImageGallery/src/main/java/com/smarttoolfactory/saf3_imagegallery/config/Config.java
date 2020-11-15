package com.smarttoolfactory.saf3_imagegallery.config;

public interface Config {

	// App Meta-Data
	public static final String APP_PUBLISHER = "Smart Tool Factory";
	public static final String APP_NAME = "Laser Level";
	public static final String APP_DIRECTORY = APP_PUBLISHER + "/" + APP_NAME;

	// Premium Versions of App
	public static int VERSION_FREE = 0;
	public static int VERSION_PAID_HALF = 1;
	public static int VERSION_PAID_FULL = 2;

	/*
	 * *** Ad Network Ids ***
	 */
	// ADMOB
	public static final String BANNER_AD_UNIT_ID_ST_LASER = "";
	public static final String INTERSTITIAL_AD_UNIT_ID_ST_LASER = "";
	// Facebook Ad Network
	// Heyzap

	public static final String DEGREE_SYMBOL = "d";

	// URIS
	// App Uris
	public static final String URI_SMART_TOOLS_LASER = "";
	public static final String URI_SMART_TOOLS_LASER_PRO = "";
	public static final String URI_SMART_TOOL_FACTORY_PLAY_STORE = "";
	public static final String URI_SMART_TOOL_FACTORY_AMAZON = "";
	public static final String URI_PRIVACY_POLICY = "";
	// Date Formats
	public static final String DATE_FORMAT_FILE = "yyyy_MM_dd_HH_mm_ss";
	public static final String DATE_FORMAT_DISPLAY = "yyyy/MM/dd z HH:mm:ss";
	// Image Formats
	public static final String IMAGE_FORMAT_JPEG = "jpg";
	public static final String IMAGE_FORMAT_PNG = "png";
	public static final String IMAGE_FORMAT_RAW = "raw";

	// Save Directory Settings
	// Image Saving Settings
	public static final int IMAGE_SAVE_OPTION_APP_DEFAULT = 0;
	public static final int IMAGE_SAVE_OPTION_DCIM = 1;
	public static final int IMAGE_SAVE_OPTION_PICTURES = 2;
	public static final int IMAGE_SAVE_OPTION_CUSTOM = 3;
	// File Saving Settings
	public static final int FILE_SAVE_OPTION_APP_DEFAULT = 0;
	public static final int FILE_SAVE_OPTION_CUSTOM = 1;

	/*
	 * *** Gallery ***
	 */
    public static final String GALLERY_IMAGE_PATH = "image_path-uri";
    public static final int REQUEST_IMAGE_DETAILS = 101;
    public static final int REQUEST_GALLERY_EDIT = 102;
    public static final int REQUEST_IMAGE_PREVIEW = 103;
    public static final int RESULT_IMAGE_DELETED = 104;


}
