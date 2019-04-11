package barikoi.dscc.dsccholdingtax;

public class Api {
    public static final String url_base="https://barikoi.xyz/v1/";
    public static final String  loginurl=url_base+"auth/login";
    public static final String usercheckurl=url_base+"auth/user",logouturl=url_base+"auth/invalidate";
    public static final String USER_ID="user_id",PASSWORD="password",TOKEN="token",LANG="language",EMAIL="email",NAME="name",POINTS="points",SPENT_POINTS="redeemed_points",PHONE="phone",ISREFFERED="isReferred",REFCODE="ref_code",HOME_ID="home_pid",WORK_ID="work_pid";

    //viewdialog
    public static final  String resetpassUrl=url_base+"auth/password/reset";
    //signupActivity
    public static final String signupurl=url_base+"auth/register";

}
