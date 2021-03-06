package mychatserver.globalutils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import static mychatserver.globalutils.KeyValues.*;
import static mychatserver.globalutils.KeyValues.KEY_USERNAME;
import static mychatserver.globalutils.Misc.customPrint;

public class DatabaseHandler {

    public static final int SIGNUP_SUCCESS = 1;
    public static final int INTERNAL_SERVER_ERROR = 0;
    public static final int ALIAS_EXISTS_ERROR = 2;
    public static final int EMAIL_EXISTS_ERROR = 3;
    public static final int ACCOUNT_DELETE_SUCCESSFUL = 5;
    public static final int ACCOUNT_EDIT_SUCCESSFUL = 7;

    static private Connection connection;

    // // Constructor
    // public CredentialDatabaseHandler(){
    //     this.getConnection();
    // }

    public static void getConnection(){
        String url = "jdbc:mysql://mychat.clisbfuk9q3h.ap-south-1.rds.amazonaws.com:3306/chat_server";
        String user = "admin";
        String password = "B6VF64EPM2GaTxAZ";
        connection = null;
        try{
            customPrint("Attempting to establish connection to the database server.");
//            connection = DriverManager.getConnection(url, user, password);
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat_server", "root", "root1234");
            customPrint("Connection established successfully.");
        }catch (Exception e){
            customPrint("Connection to chat database failed! Exiting...");
            System.exit(1);
        }
    }

    // Checks if the user name exists or not
    public static synchronized boolean isAliasValid(String alias){
        try{
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select uname from chat_server.user_details where uname = '"+alias+"'");
            while(rs.next()){
                if(rs.getString(1).equals(alias))
                    return true;
            }
            return false;
        }catch (SQLException e){
            System.out.println(e);
            return false;
        }
    }

    // Checks if the login is valid or not
    public static synchronized boolean isLoginValid(String alias, String password){
        try{
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select password from chat_server.user_details where uname = '"+alias+"'");
            while(rs.next()){
                if(rs.getString(1).equals(password))
                    return true;
            }
            return false;
        }catch(SQLException e){
            System.out.println(e);
            return false;
        }
    }

    public static synchronized int createAccount(String firstName, String lastName, String email, String password, String alias){
        if (isAliasValid(alias))
            return ALIAS_EXISTS_ERROR;
        try{
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select email from chat_server.user_details where email = '"+email+"'");
            while(rs.next()){
                if (rs.getString(1) == email)
                    return EMAIL_EXISTS_ERROR;
            }
            int uid = (int)(Math.random()*1000000);
            PreparedStatement ps = connection.prepareStatement("insert into chat_server.user_details values (?,?,?,?,?,?)");
            ps.setInt(1, uid);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, email);
            ps.setString(5, password);
            ps.setString(6, alias);
            ps.executeUpdate();
            return SIGNUP_SUCCESS;
        }catch (SQLException e){
            System.out.println(e);
            return INTERNAL_SERVER_ERROR;
        }
    }

    public static synchronized ArrayList<HashMap<String, Object>> getAllUsers(){
        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select fname, lname, email, uname from chat_server.user_details");
            ArrayList<HashMap<String, Object>> users = new ArrayList<>();
            while(resultSet.next()){
                HashMap<String, Object> user = new HashMap<>();
                user.put(KEY_FIRST_NAME, resultSet.getString(1));
                user.put(KEY_LAST_NAME, resultSet.getString(2));
                user.put(KEY_EMAIL, resultSet.getString(3));
                user.put(KEY_USERNAME, resultSet.getString(4));
                users.add(user);
            }
            return users;
        } catch (SQLException e){
            System.out.println(e);
            return null;
        }
    }

    // Returns a hashmap object with firstname, lastname, email and username encoded into it
    public static synchronized HashMap<String, Object> getDetailsFromUsername(String username){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("select fname, lname, email, uname from chat_server.user_details where uname = ?");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            HashMap<String, Object> user = new HashMap<>();
            while(resultSet.next()){
                user.put(KEY_FIRST_NAME, resultSet.getString(1));
                user.put(KEY_LAST_NAME, resultSet.getString(2));
                user.put(KEY_EMAIL, resultSet.getString(3));
                user.put(KEY_USERNAME, resultSet.getString(4));
            }
            return user;
        } catch (SQLException e){
            System.out.println(e);
            return null;
        }
    }

    public static synchronized int deleteAccount(String username){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("delete from chat_server.user_details where uname = ?");
            preparedStatement.setString(1, username);
            preparedStatement.execute();
            return ACCOUNT_DELETE_SUCCESSFUL;
        }catch (SQLException e){
            System.out.println(e);
            return INTERNAL_SERVER_ERROR;
        }
    }

    public static synchronized int editAccountWithPasswordAndWithUsername(String username, String firstName, String lastName, String password, String email){
        try{
            if (!isAliasValid(username)) {
                PreparedStatement preparedStatement = connection.prepareStatement("update chat_server.user_details set fname = ?, lname = ?, uname = ?, password = ? where email = ?");
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, username);
                preparedStatement.setString(4, password);
                preparedStatement.setString(5, email);
                preparedStatement.executeUpdate();
                return ACCOUNT_EDIT_SUCCESSFUL;
            }else{
                return ALIAS_EXISTS_ERROR;
            }
        }
        catch (SQLException e){
            System.out.println(e);
            return INTERNAL_SERVER_ERROR;
        }
    }

    public static synchronized int editAccountWithoutPasswordAndWithUsername(String username, String firstName, String lastName, String email){
        try{
            if (!isAliasValid(username)) {
                PreparedStatement preparedStatement = connection.prepareStatement("update chat_server.user_details set fname = ?, lname = ?, uname = ? where email = ?");
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, username);
                preparedStatement.setString(4, email);
                preparedStatement.executeUpdate();
                return ACCOUNT_EDIT_SUCCESSFUL;
            }else{
                return ALIAS_EXISTS_ERROR;
            }
        }
        catch (SQLException e){
            System.out.println(e);
            return INTERNAL_SERVER_ERROR;
        }
    }

    public static synchronized int editAccountWithPasswordAndWithoutUsername(String password, String firstName, String lastName, String email){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("update chat_server.user_details set fname = ?, lname = ?, password = ? where email = ?");
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, email);
            preparedStatement.executeUpdate();
            return ACCOUNT_EDIT_SUCCESSFUL;
        }
        catch (SQLException e){
            System.out.println(e);
            return INTERNAL_SERVER_ERROR;
        }
    }

    public static synchronized int editAccountWithoutPasswordAndWithoutUsername(String firstName, String lastName, String email){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("update chat_server.user_details set fname = ?, lname = ? where email = ?");
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, email);
            preparedStatement.executeUpdate();
            return ACCOUNT_EDIT_SUCCESSFUL;
        }
        catch (SQLException e){
            System.out.println(e);
            return INTERNAL_SERVER_ERROR;
        }
    }

    public static synchronized String getPassword(String username){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("select password from chat_server.user_details where uname = ?");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            String password = null;
            while(resultSet.next()){
                password = resultSet.getString(1);
            }
            return password;
        }catch (SQLException e){
            System.out.println(e);
            return null;
        }
    }

}
