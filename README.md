# NLP18


## Workflow

![workflow](https://github.com/camgbus/NLP18/blob/master/FinalProject/Workflow.png)

## Class Diagram

![Class Diagram](https://github.com/camgbus/NLP18/blob/master/FinalProject/ClassDiagram.png)
# BysCrawler 

**BysCrawler** is a web crawler that extracts book reviews from [Amazon](https://www.amazon.com/). The crawler will get as input a list of Amazon Book URLs and, as a result, return the reviews of all listed books.

The Java library [jsoup](https://github.com/jhy/jsoup) is used to implement the crawler. For each book, all "customer reviews" pages are visited and the reviews extracted. The crawler stops searching when the maximum number of reviews to extract has been reached or when there are no more ratings left.


## Sent2vec

We use the [sent2vec](https://github.com/epfml/sent2vec) library to convert a sentence to a vector of features. Sent2vec is a library, that delivers numerical representations (features) for short texts or sentences, which can be used as input to any machine learning task later on. It is based on the [fastText](https://github.com/facebookresearch/fastText) library for efficient learning of word representations. Using the model sent2vec_toronto books_unigrams (700dim, trained on the [BookCorpus dataset](http://yknzhu.wixsite.com/mbweb)), the framework takes as input a list of token and return as a result a numeric values list.  To classify the sentences, we use the cosines similarity between the sentence features vectors. 
