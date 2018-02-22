package com.github.albocoder.FBAPI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class App {
	static String EMAIL = "";
	static String PASSWORD = "";
	static String FACEBOOK = "https://mbasic.facebook.com";
	static String FULL_FACEBOOK = "https://www.facebook.com";
	static String SAMPLE_CSV_FILE = "backup.csv";

	public static void main( String[] args ) {
		// logging in!
		WebClient w = new WebClient();
		if(!login(w,EMAIL,PASSWORD)) {
			System.err.println("Failed to log in!");
			System.exit(1);
		}
		List<String> friends = getListOfFriends(w);
		for (String f: friends) 
			System.out.println(f);
		w.close();
	}
	
	// better not to touch these functions dude!
	public static Map<String, String> getAlbumsAndLinks(WebClient w, String profileLink,String uid) throws MalformedURLException, IOException{
		Map<String, String> toReturn = new HashMap<String, String>();
		try{
			HtmlPage albumsPage = w.getPage(profileLink+"/photos/albums/?owner_id="+uid+"&offset=0");
			while(true) {
				List<DomNode> albums = albumsPage.querySelectorAll("span.t");
				if(albums.size()<=0)
					break;
				for(DomNode n:albums) {
					n = n.getFirstChild();
					toReturn.put(n.asText(),n.getAttributes().getNamedItem("href").getNodeValue());
				}
				albumsPage = w.getPage(profileLink+"/photos/albums/?owner_id="+uid+"&offset="+toReturn.size());
			}
		} catch(RuntimeException e ) {return null;}
		return toReturn;
	}
	public static Map<String, Set<String>> getPhotosOfAllAlbums(WebClient w, Map<String,String> albumsMap){
		Map<String, Set<String>> toReturn = new HashMap<String, Set<String>>();
		Set<String> keys = albumsMap.keySet();
		Iterator<String> ki = keys.iterator();
		while (ki.hasNext()) {
			int photosSoFar = 0;
			String albumName = ki.next();
			String albumLink = FACEBOOK+albumsMap.get(albumName);
			try {
				photosSoFar = getPhotosOfAlbum(w,photosSoFar,albumLink,albumName,toReturn);
			} catch (IOException|FailingHttpStatusCodeException e) {}
		}
		return toReturn;
	}
	public static int getPhotosOfAlbum(WebClient w, int photosSoFar, String albumLink, String albumName, Map<String, Set<String>> photosMap) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		// getting photos from albums
		while(true) {
			HtmlPage photosPage = w.getPage(albumLink+"?start_index="+photosSoFar);
			DomElement thumbnailArea = photosPage.querySelector("div#thumbnail_area");
			if(thumbnailArea.getChildElementCount() == 0)
				break;
			Iterable<DomElement> pics = thumbnailArea.getChildElements();
			Iterator<DomElement> picTerator = pics.iterator();

			Set<String> albumPhotoSet = new HashSet<String>();
			photosMap.put(albumName, albumPhotoSet);
			while(picTerator.hasNext()) {
				DomElement p = (DomElement) picTerator.next();
				String imageLink = p.getAttributes().getNamedItem("href").getNodeValue();
				imageLink = imageLink.replace(".php","/view_full_size/");
				UnexpectedPage redirect = w.getPage(FACEBOOK+imageLink);
				InputStream is = redirect.getWebResponse().getContentAsStream();
				try{
					String filename = writeStreamToFile(is,albumName);
					albumPhotoSet.add(filename);
					photosSoFar++;
				}catch(IOException e) {}
			}
		}
		return photosSoFar;
	}
	public static String writeStreamToFile(InputStream is,String albumName) throws IOException {
		// generate random filename
		String filename = System.currentTimeMillis()+albumName+".jpg";
		// get file stream
		FileOutputStream fos = new FileOutputStream(new File(filename));
		// create buffer
		byte[] buffer = new byte[1024];
		// write to file
		int length = is.read(buffer);
		while(length > 0) {
			fos.write(buffer, 0, length);
			length = is.read(buffer);
		}
		fos.close();
		return filename;
	}
	
	// directly usable functions!
	public static boolean getPhotosOfSelectedFriends(WebClient w,List<String> selectedPeople) {
		// initialize the mapping file
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(SAMPLE_CSV_FILE)));
		} catch (IOException e1) { return false; }
		CSVPrinter csvPrinter;
		try {
			csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
		} catch (IOException e1) { return false; }

		// number of selected people (optimization to stop right when our list of people is fulfilled)
		int numProcessedPpl = 0;
		
		// do this for all friend pages
		HtmlPage friendPage;
		for(int i = 0;;i++) {
			// get a page of friends
			try {
				friendPage = w.getPage("https://mbasic.facebook.com/friends/center/friends/?ppk="+i);
			} catch (FailingHttpStatusCodeException | IOException e1) { 
				try {
					csvPrinter.close();
				} catch (IOException e) { return false; }
				return false; 
			}
			Document parsed = Jsoup.parse(friendPage.asXml().toString());
			Elements friends = parsed.getElementsByClass("bk");
			friends = friends.first().children();
			if(friends.size()<=1)
				break;
			// get photos for each friend
			for(Element f : friends) {
				Element link = f.getElementsByClass("bn").first();

				// get name
				String name = link.text();
				if(selectedPeople != null && !selectedPeople.contains(name))
					continue;

				// get profile link + uid
				String profileLink = link.attr("href");
				String uid;
				try {
					uid = profileLink.substring(profileLink.indexOf("uid=")+4,profileLink.indexOf("&"));
				}catch(RuntimeException e) {
					e.printStackTrace();
					System.err.println("link \""+profileLink+"\" is invalid");
					continue;
				}
				profileLink = FACEBOOK+"/"+uid;

				// getting albums and links
				Map<String, String> albumsMap;
				try {
					albumsMap = getAlbumsAndLinks(w,profileLink,uid);
				} catch (IOException e) { 
					try {
						csvPrinter.close();
					} catch (IOException e1) { return false; }
					return false; 
				}

				// getting username
				Set<String> keys = albumsMap.keySet();
				String username = albumsMap.get(keys.toArray()[0]);
				username = username.substring(1,username.indexOf("/",1));

				// get all photos of the albums
				Map<String, Set<String>> photosMap = getPhotosOfAllAlbums(w,albumsMap);

				// Write to file for visualization purposes
				Iterator<String> ki = keys.iterator();
				while (ki.hasNext()) {
					String albumName = ki.next();
					Set<String> photos = photosMap.get(albumName);
					Iterator<String> pi = photos.iterator();
					while(pi.hasNext()) {
						String photoName = pi.next();
						try {
							csvPrinter.printRecord(name,uid,username,albumName,photoName);
						} catch (IOException e) { 
							try {
								csvPrinter.close();
							} catch (IOException e1) { return false; }
							return false; 
						}
					}
				}
				if(++numProcessedPpl>=selectedPeople.size())
					break;
			}
		}
		try {
			csvPrinter.close();
		} catch (IOException e) { return false; }
		return true;
	}
	public static List<String> getListOfFriends(WebClient w){
		List<String> toReturn = new ArrayList<String>(200);
		HtmlPage friendPage;
		for(int i = 0;;i++) {
			// get a page of friends
			try {
				friendPage = w.getPage("https://mbasic.facebook.com/friends/center/friends/?ppk="+i);
			} catch (FailingHttpStatusCodeException | IOException e1) { return null; }
			Document parsed = Jsoup.parse(friendPage.asXml().toString());
			Elements friends = parsed.getElementsByClass("bk");
			friends = friends.first().children();
			if(friends.size()<=1)
				break;
			// get name of each friend
			for(Element f : friends) {
				Element link = f.getElementsByClass("bn").first();
				toReturn.add(link.text());
			}
		}
		return toReturn;
	}
	public static boolean login(WebClient w,String un,String pw) {
		HtmlPage loginPage;
		try {
			loginPage = (HtmlPage) w.getPage(FACEBOOK+"/login.php");
		} catch (FailingHttpStatusCodeException | IOException e1) { return false; }
		HtmlForm loginForm = loginPage.getForms().get(0);
		loginForm.getInputByName("email").setValueAttribute(EMAIL);
		loginForm.getInputByName("pass").setValueAttribute(PASSWORD);
		try {
			loginForm.getInputByValue("Log In").click();
		} catch (ElementNotFoundException | IOException e) { return false; }

		// check if logged in
		HtmlPage homePage;
		try {
			homePage = w.getPage("https://mbasic.facebook.com/home.php");
		} catch (FailingHttpStatusCodeException | IOException e) { return false; }
		// if not logged
		if(!homePage.asText().contains("Messages")) { return false; }
		return true;
	}
}
