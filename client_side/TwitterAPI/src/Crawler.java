
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.*;

public class Crawler {


	public static void main(String args[]) throws IOException {
		String [][] configs = APIKeys.getConfigs();
		int app = 0;

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey(       configs[0][app] );
		cb.setOAuthConsumerSecret(    configs[1][app] );
		cb.setOAuthAccessToken(       configs[2][app] );
		cb.setOAuthAccessTokenSecret( configs[3][app] );
		cb.setHttpConnectionTimeout(50000);

		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter tw = tf.getInstance();

//		try {
//			try {
//				System.out.println("-----");
//
//				// get request token.
//				// this will throw IllegalStateException if access token is already available
//				// this is oob, desktop client version
//				RequestToken requestToken = tw.getOAuthRequestToken(); 
//
//				System.out.println("Got request token.");
//				System.out.println("Request token: " + requestToken.getToken());
//				System.out.println("Request token secret: " + requestToken.getTokenSecret());
//
//				System.out.println("|-----");
//
//				AccessToken accessToken = null;
//
//				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//
//				while (null == accessToken) {
//					System.out.println("Open the following URL and grant access to your account:");
//					System.out.println(requestToken.getAuthorizationURL());
//					System.out.print("Enter the PIN(if available) and hit enter after you granted access.[PIN]:");
//					String pin = br.readLine();
//
//					try {
//						if (pin.length() > 0) {
//							accessToken = tw.getOAuthAccessToken(requestToken, pin);
//						} else {
//							accessToken = tw.getOAuthAccessToken(requestToken);
//						}
//					} catch (TwitterException te) {
//						if (401 == te.getStatusCode()) {
//							System.out.println("Unable to get the access token.");
//						} else {
//							te.printStackTrace();
//						}
//					}
//				}
//				System.out.println("Got access token.");
//				System.out.println("Access token: " + accessToken.getToken());
//				System.out.println("Access token secret: " + accessToken.getTokenSecret());
//
//			} catch (IllegalStateException ie) {
//				// access token is already available, or consumer key/secret is not set.
//				if (!tw.getAuthorization().isEnabled()) {
//					System.out.println("OAuth consumer key/secret is not set.");
//					System.exit(-1);
//				}
//			}
//		} catch (TwitterException te) {
//			te.printStackTrace();
//			System.out.println("Failed to get timeline: " + te.getMessage());
//			System.exit(-1);
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//			System.out.println("Failed to read the system input.");
//			System.exit(-1);
//		}
//		System.exit(0);
		

		
		
		
		
		
		
		String un = "ctfturkey";
		try {
            List<Status> statuses;
            statuses = tw.getUserTimeline(un);
            
            System.out.println("Showing @" + un + "'s user timeline.");
            for (Status status : statuses) {
            	MediaEntity[] medias = status.getMediaEntities();
            	for (MediaEntity m:medias) 
            		System.out.println(m.getMediaURL());
            }
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get timeline: " + te.getMessage());
            System.exit(-1);
        }

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		// 1) get users following IDs
//		long cursor = -1;
//		PagableResponseList<User> friends;
//		try {
//			friends = tw.getFriendsList(tw.getId(), cursor);
//		} catch(TwitterException e) { 
//			if(e.exceededRateLimitation()){
//				System.out.println("Application "+ app +" shall wait " 
//						+ e.getRateLimitStatus().getSecondsUntilReset() + " seconds!");
//				app = (app+1)%APIKeys.ACTIVE_APPS;
//				cb = new ConfigurationBuilder();
//				cb.setDebugEnabled(true);
//				cb.setOAuthConsumerKey(       configs[0][app] );
//				cb.setOAuthConsumerSecret(    configs[1][app] );
//				//				cb.setOAuthAccessToken(       configs[2][app] );
//				//				cb.setOAuthAccessTokenSecret( configs[3][app] );
//				cb.setHttpConnectionTimeout(50000);
//				//cb.setUseSSL(true);
//				tf = new TwitterFactory(cb.build());
//				tw = tf.getInstance();
//			}
//			else
//				e.printStackTrace(); return; 
//		}
//
//		for (User u: friends) {
//			if(!u.isDefaultProfileImage()) {
//				String profileImgUrl = u.getBiggerProfileImageURL().replace("bigger","400x400");
//				String extension = profileImgUrl.substring(profileImgUrl.lastIndexOf(".")+1);
//
//				System.out.println(u.getName()+"\t"+profileImgUrl);
//				downloadImage(profileImgUrl,u.getScreenName()+"."+extension);
//			}
//			//			System.out.println(u);
//		}

	}
	private static void downloadImage(String url, String filename) throws IOException {
		URL u = new URL(url);
		InputStream is = new BufferedInputStream(u.openConnection().getInputStream());
		byte [] buff = new byte[1024];
		try (FileOutputStream fos = new FileOutputStream(filename)) {
			int readData = is.read(buff);
			while(readData > 0) {
				fos.write(buff,0,readData);
				readData = is.read(buff);
			}
			fos.close();
		}
	}
}
