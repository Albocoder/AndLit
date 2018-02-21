package com.github.albocoder.FBAPI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class App {
	static String EMAIL = "";
	static String PASSWORD = "";
	static String FACEBOOK = "https://mbasic.facebook.com";
	static String SAMPLE_CSV_FILE = "backup.csv";

	public static void main( String[] args ) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		//logging in!
		WebClient w = new WebClient();

		HtmlPage loginPage = (HtmlPage) w.getPage(FACEBOOK+"/login.php");
		HtmlForm loginForm = loginPage.getForms().get(0);
		loginForm.getInputByName("email").setValueAttribute(EMAIL);
		loginForm.getInputByName("pass").setValueAttribute(PASSWORD);
		loginForm.getInputByValue("Log In").click();
		
		// check if logged in
		HtmlPage homePage = w.getPage("https://mbasic.facebook.com/home.php");
		if(!homePage.asText().contains("Messages")) {
			System.err.println("Failed to login!");
			System.exit(1);
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(SAMPLE_CSV_FILE)));
		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
		// get friends
		HtmlPage friendPage;
		for(int i = 0;;i++) {
			
			// TODO DELETE THIS! ITS ONLY A LIMITING FACTOR IN LOOP CYCLES
			if(i == 1) {
				csvPrinter.close();
				System.exit(1);
			}
			
			friendPage = w.getPage("https://mbasic.facebook.com/friends/center/friends/?ppk="+i);
			Document parsed = Jsoup.parse(friendPage.asXml().toString());
			Elements friends = parsed.getElementsByClass("bk");
			friends = friends.first().children();
			
			for(Element f : friends) {
				Element link = f.getElementsByClass("bn").first();
				
				// get name
				String name = link.text();
				
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
				
				// getting albums
				Map<String, String> map = new HashMap<String, String>();
				try{
					HtmlPage albumsPage = w.getPage(profileLink+"/photos/albums/?owner_id="+uid+"&offset=0");
					while(true) {
						List<DomNode> albums = albumsPage.querySelectorAll("span.t");
						if(albums.size()<=0)
							break;
						for(DomNode n:albums) {
							n = n.getFirstChild();
							map.put(n.asText(),n.getAttributes().getNamedItem("href").getNodeValue());
						}
						albumsPage = w.getPage(profileLink+"/photos/albums/?owner_id="+uid+"&offset="+map.size());
					}
				} catch(RuntimeException e ) {}
				
				// getting username
				Set<String> keys = map.keySet();
				String username = map.get(keys.toArray()[0]);
				username = username.substring(1,username.indexOf("/",1));
				
				// TODO get all photos of the albums
				
				
				
				// just to write to file for visualization
				Iterator<String> ki = keys.iterator();
				csvPrinter.flush();
				while (ki.hasNext())
					csvPrinter.printRecord(name,uid,username,map.get(ki.next()));
			}
			if(friends.size()<=1)
				break;
		}
		w.close();
		csvPrinter.close();
	}
}
