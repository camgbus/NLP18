import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class book {
	
	String  name;
	String author;
	List<Review> reviews;
	String BaseUrl = "https://www.amazon.com";     
	int MaxReviews = 50;
	int reviewsPageNumber;

	public book(String url) {
		try {
			reviews = new ArrayList<Review>();
			Document document = Jsoup.connect(url).get();
			name = document.select("#productTitle").text();
			author = document.select(".contributorNameID").text();
			String reviewsUrl = BaseUrl+document.select("[data-hook=\"see-all-reviews-link-foot\"]").first().attr("href");
			Document rewiewsDoc =  Jsoup.connect(reviewsUrl).get();
			Element reviewsPageButton = rewiewsDoc.select(".page-button").last();
			reviewsPageNumber = Integer.parseInt(reviewsPageButton.select("a").text());
			String reviewsPageUrl[] = reviewsPageButton.select("a").first().attr("href").split(""+reviewsPageNumber);
			for (int i=1; i <= reviewsPageNumber; i++) {
				if(reviews.size()==MaxReviews) break;
				reviewsUrl= BaseUrl+reviewsPageUrl[0]+i+reviewsPageUrl[1]+i+reviewsPageUrl[2];
				rewiewsDoc =  Jsoup.connect(reviewsUrl).get();
				Elements reviewsDiv = rewiewsDoc.select("#cm_cr-review_list").select("[data-hook=\"review\"]");
				for (Element element : reviewsDiv) {
					if(reviews.size()==MaxReviews) break;
					reviews.add(new Review(element));
				}
			}      
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return name+", "+author+": "+reviews.size()+" reviews";
	}
	
	public void Export(String path) {
		try {
			FileWriter fileWriter ;
			for (int i=0; i < reviews.size(); i++) {
				fileWriter = new FileWriter(path+StringUtil.join(name.split(" "),"")+"_"+i+".csv");
				fileWriter.append(name+ " " + author+"\n");
				fileWriter.append("\n");
				fileWriter.append("\n");
				fileWriter.append(reviews.get(i).toString());
				fileWriter.flush();
				fileWriter.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
