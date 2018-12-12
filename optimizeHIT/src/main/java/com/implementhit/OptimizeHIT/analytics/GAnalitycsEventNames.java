package com.implementhit.OptimizeHIT.analytics;


public class GAnalitycsEventNames {
	
	public static class MENU_SELECT {
		public static final String CATEGORY = "Menu";
		public static final String ACTION = "Change";
//		public static final String LABEL = "Open";
	}
	
//	public static class CLOSE_MENU {
//		public static final String CATEGORY = "Menu";
//		public static final String ACTION = "Show";
//		public static final String LABEL = "Close";
//	}

	public static class DOMAIN_BUTTON_PRESSED {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Domain";
	}

	public static class DOMAIN_HELP_BUTTON_PRESSED {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Domain Help";
	}  // TODO: ADD DOMAIN HELP BUTTON

	public static class CHANGE_DOMAIN_BUTTON_PRESSED {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Change Domain";
	}

	public static class LOGIN_BUTTON_PRESSED {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Login";
	}
	
	public static class FORGOT_PASSWORD_BUTTON_PRESSED {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Forgot Password";
	}

	public static class CANCEL_FORGOT_PASSWORD_BUTTON_PRESSED {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Cancel Forgot Password";
	}
	
	public static class SUBMIT_RESET_BUTTON_PRESSED {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Reset Password";
	}
	
	public static class LOGOUT_BUTTON_PRESSED {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Logout";
	}
	
	public static class VOICE_SEARCH_BUTTON_PRESSED {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Voice Search Button";
	}
	
//	public static class EDIT_FAVORITES {
//		public static final String CATEGORY = "UI";
//		public static final String ACTION = "Button";
//		public static final String LABEL = "Edit Favorites";
//	}
//
//	public static class DELETE_FAVORITES {
//		public static final String CATEGORY = "UI";
//		public static final String ACTION = "Button";
//		public static final String LABEL = "Delete Favorites";
//	}
	
	public static class VOICE_SEARCH_START {
		public static final String CATEGORY = "Voice Search";
		public static final String ACTION = "Status";
		public static final String LABEL = "Start";
	}
	
	public static class VOICE_SEARCH_STOPPED_FINISHED {
		public static final String CATEGORY = "Voice Search";
		public static final String ACTION = "Status";
		public static final String LABEL = "Stop";
	}
	
	public static class VOICE_SEARCH_RESULT_RECEIVED {
		public static final String CATEGORY = "Voice Search";
		public static final String ACTION = "Result";
	}
	
	public static class VOICE_SEARCH_ERROR {
		public static final String CATEGORY = "Voice Search";
		public static final String ACTION = "Error";
	}
	
	public static class RATE_SOLUTION {
		public static final String CATEGORY = "Solution";
		public static final String ACTION = "Like";
	}

	public static class SOLUTION_TEXT_TO_SPEECH {
		public static final String CATEGORY = "Solution";
		public static final String ACTION = "TTS";
	}

	public static class SOLUTION_BOTTOM_NAVIGATION {
		public static final String CATEGORY = "Solution";
		public static final String ACTION = "Navigation";
	}

	public static class FIND_A_CODE_SEARCH {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Search for Code";
	}

	public static class EXPLORE_ICD_10 {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Explore ICD-10 Code";
	}

	public static class ADD_TO_SUPERBILL {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Add to Superbill";
	}

	public static class REMOVE_FROM_SUPERBILL {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Remove from Superbill";
	}

	public static class ADD_NEW_GROUP {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Add new Superbill Group";
	}

	public static class MOVE_TO_GROUP {
		public static final String CATEGORY = "UI";
		public static final String ACTION = "Button";
		public static final String LABEL = "Move to Superbill Group";
	}

}