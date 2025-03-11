package models;

public class UserSession {
   
   private static User loggedUser;

   public static User getLoggedUser() {
      return loggedUser;
   }

   public static void setLoggedUser(User user) {
      loggedUser = user;
   }

   public static boolean isUserLoggedIn() {
      return loggedUser != null;
   }
}
