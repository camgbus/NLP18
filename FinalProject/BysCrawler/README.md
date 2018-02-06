# BysCrawler 

**BysCrawler** is a web crawler that extracts book reviews from [Amazon](https://www.amazon.com/). The crawler will get as input a list of Amazon Book URLs and, as a result, return the reviews of all listed books.

The Java library [jsoup](https://github.com/jhy/jsoup) is used to implement the crawler. For each book, all "customer reviews" pages are visited and the reviews extracted. The crawler stops searching when the maximum number of reviews to extract has been reached or when there are no more ratings left.

