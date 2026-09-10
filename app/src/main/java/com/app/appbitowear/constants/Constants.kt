package com.app.appbitowear.constants

import androidx.compose.ui.graphics.Color
import com.app.appbitowear.BuildConfig

object Constants {
    const val BASE_URL = BuildConfig.BASE_URL
    const val DATASTORE = "auth_pref"
    const val COOKIES_STORE = "cookies_prefs"
    const val NETWORK_TIMEOUT = 30L
    const val TOAST_DELAY = 2500L
    const val HABIT_ID = "habitId"
    const val HABIT_NAME = "habitName"
    const val UPDATED_HABIT_ID = "updated_habit_id"
    const val PROGRESS_ID = "progress_id"
    const val PROGRESS_DATE = "progress_date"
    const val PROGRESS_TIMES = "progress_times"
    const val PROGRESS_NOTE = "progress_note"
    const val LIMIT_NOTE_LENGTH = 255
    const val DATE_FORMAT = "MMM dd"
}

object ApiRoutes {
    private const val API = "/api"
    private const val ME = "/me"
    const val HABIT = "/habit"

    const val AUTH = "/auth"
    const val USERS = "$API/users"
    const val HABITS = "$API/habits"
    const val HABITS_PROGRESS = "$API/habitsProgress"

    const val LOGIN = "/login"
    const val AUTH_REFRESH_TOKEN = "$AUTH/refresh"
    const val AUTH_LOGOUT = "$AUTH/logout"

    const val USERS_ME = "$USERS$ME"
    const val USERS_ME_IMAGE = "$USERS_ME/image"

    const val HABITS_TODAY = "$HABITS/today"
    const val STREAK = "/streak"
    const val RANGE= "/range"
}

object Auth {
    const val AUTHORIZATION_HEADER = "Authorization"
    const val BEARER_PREFIX = "Bearer "
    const val TOKEN_PREF_KEY = "jwt_token"
    const val REFRESH_TOKEN = "refresh_token"
}

object Screens {
    const val LOGIN = "login"
    const val DAILY_HABITS = "dailyHabits"
    const val EDIT_PROGRESS = "editProgress"
    const val USER_PROFILE = "userProfile"
}

object Login {
    const val LOGIN = "Login"
    const val EMAIL = "Email"
    const val PASSWORD = "Password"
    const val EMPTY_FIELDS = "Email and password required"
    const val EMAIL_PASSWORD_INCORRECT = "Email or password incorrect"
    const val SESSION_EXPIRED = "Your session has expired. Please log in again."
    const val BAD_REQUEST = "Verify your data"
}

object DailyHabits {
    const val EDIT_SUCCESS = "Success updated"
    const val THERE_ARENT_HABITS_TODAY = "There are no habits scheduled for today."
    const val HABIT_NOT_FOUND = "The habit doesn't exist"
    const val BAD_REQUEST = "Progress could not be updated"
    const val PROGRESS_NOT_FOUND = "The progress doesn't exist"
    const val OFFLINE_MODE = "Offline: Only read"
}

object UserProfile {
    const val PROFILE = "Profile"
    const val LOGOUT = "Logout"
    const val LOGOUT_CONFIRMATION = "Are you sure you want to leave?"
    const val USER_NOT_FOUND = "The user doesn't exist"
    const val BAD_REQUEST = "Profile data could not be loaded"
    const val ERROR_FETCHING_DATA = "Error to load user profile"
}

object Colors {
    val SUCCESS_GREEN = Color(0xFF4CAF50)
    val WARNING_RED = Color(0xFFB00020)
}

object General {
    const val EMPTY_STRING = ""
    const val HIDDEN = "Hidden"
    const val SHOW = "Show"
    const val DONE = "Done"
    const val HAS_NOTE = "Has note"
    const val EDIT_PROGRESS = "Edit Progress"
    const val DECREASE = "Decrease"
    const val INCREASE = "Increase"
    const val QUICK_NOTE = "Quick Note"
    const val SAVE = "Save"
    const val DELETE = "Delete"
    const val STREAK = "Streak"
    const val COLON = ":"
    const val YES = "Yes"
    const val NO = "NO"
    const val ERROR = "Error"
}

object ErrorInterceptors {
    const val MSG_ERROR_ZERO = "Connection error. Check your internet connection"
    const val MSG_ERROR_500 = "Unable to communicate with the server, please try again later"
    const val MSG_ERROR_FORBIDDEN = "Access denied"
    const val MSG_ERROR_BAD_REQUEST = "Invalid request"
    const val MSG_ERROR_NOT_FOUND = "Resource not found"
    const val MSG_ERROR_DEFAULT = "Unexpected error, please try again later"
    const val MSG_NOT_INTERNET = "No Internet connection"
    const val MSG_COULD_NOT_LOAD_HABITS = "The habits could not be loaded"
}

object App {
    const val NAME = "Appbito"
    const val DAILY_HABITS = "Daily Habits"
    const val USER_PROFILE = "Data Profile"
}

