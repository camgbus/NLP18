import java.util.List;

import org.jsoup.nodes.Element;

public class Review {
	
	String  title;
	String author;
	String[] sentences;

	public Review(Element review) {
		
		author = review.select("a[data-hook=\"review-author\"]").text();
		title = review.select("[data-hook=\"review-title\"]").text();
		sentences = review.select("[data-hook=\"review-body\"]").text().split("\\.");
	}
	
	public String toString() {
		String review = "";
		for(String sentence : sentences) {
			if(!sentence.isEmpty())
				review+= sentence+"\n";
		}
		
		return review;
	}

}
