import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RunBysCrawler {
	
	static String ExportDirectory = "/Users/youssef/Desktop/NPLW/Books/";

	public RunBysCrawler() {
		
	}

	public static void main(String [ ] args) throws IOException
	{
		
		FileReader   books = new FileReader("/Users/youssef/Desktop/NPLW/books.csv");
		BufferedReader  booksReader = new BufferedReader(books);
		booksReader.readLine();
		String book;
		while((book = booksReader.readLine()) != null) {
			String url = book.split(",")[4];
			System.out.println(url);
			book test = new book(url);
			System.out.println(test);
			test.Export(ExportDirectory);
		}
	}
}