from sklearn import tree
from sklearn.naive_bayes import GaussianNB
import nltk
from nltk.sentiment.vader import SentimentIntensityAnalyzer
import sys
import freq_weights as fw
import senttovec as stv
#nltk.download('vader_lexicon')

def question_mark(x):
	return int(any('?' in t for t in x))
	
def exclamation_mark(x):
	return int(any('!' in t for t in x))

def sentence_length(x):
	return len(x)

pos_tags = ['JJ', 'JJR', 'JJS', 'MD', 'NN', 'NNS', 'NNP', 'NNPS', 
'PRP', 'PRP$', 'RB', 'RBR', 'RBS', 'VB', 'VBZ', 'VBP', 'VBD', 'VBN', 
'VBG', 'WDT', 'WP', 'WP$', 'WRB']

def pos_tagger(x):
	"""Get number of occurences for selected POS tags"""
	s = nltk.pos_tag(x)
	return [[j for (i,j) in s].count(t) for t in pos_tags]

def sentiment(x):
	"""Get sentiment score"""
	sid = SentimentIntensityAnalyzer()
	ss = sid.polarity_scores(" ".join(x))
	return ss['neg']+ss['pos']

def number_rep_tokens(x, rep):
	"""Number of representative tokens of a category it contains"""
	return [sum(x.count(t) for t in rep[c]) for c in rep.keys()]

def vector_similarities(x, train_set, data_set, stv):
	'''Get similarity measure between sentence and category representations'''
	scores = stv.get_similarity_scores(train_set, data_set, x)
	return [scores[c] for c in scores.keys()]
		

def feature_set(x, rep, train_set, data_set, stv):
	fs = []
	#fs.append(question_mark(x))
	#fs.append(exclamation_mark(x))
	#fs.append(sentence_length(x))
	#fs.append(sentiment(x))
	#fs = fs + number_rep_tokens(x, rep)
	#fs = fs + pos_tagger(x)
	if stv:
		fs = fs + vector_similarities(x, train_set, data_set, stv)
	return fs

def get_predictions_CART(train_set, test_set, nr_tfidf_cl, stv = None):
	# Get class tags
	Y_train = [tag for (s,tag) in train_set.data]
	
	# Get highest tf-idf tokens for each category
	tfidf_cl = fw.representative(train_set.data, train_set.classDict, nr_tfidf_cl)
	
	# Get feature sets
	X_train = [feature_set(train_set.data[i][0], tfidf_cl, train_set, train_set, stv) for i in range(len(train_set.data))]
	
	# Build decision tree
	clf = tree.DecisionTreeClassifier()
	clf = clf.fit(X_train, Y_train)
	
	# Return predictions
	X_test = [feature_set(test_set.data[i][0], tfidf_cl, train_set, test_set, stv) for i in range(len(test_set.data))]
	return clf.predict(X_test)

def print_tree(model, exp_name, categories):
	fs = ['q?', 'e?', 'length', 'sentiment']
	for c in categories:
		fs.append("tfidf#"+c)
	for t in pos_tags:
		fs.append("#"+t)
	for c in categories:
		fs.append("vs_"+c)
	tree.export_graphviz(clf, out_file="models/"+exp_name+".dot", feature_names=fs, class_names=categories, filled=True)

def get_predictions_NB(train_set, test_set, nr_tfidf_cl, stv = None):
	# Get class tags
	Y_train = [tag for (s,tag) in train_set.data]
	
	# Get highest tf-idf tokens for each category
	tfidf_cl = fw.representative(train_set.data, train_set.classDict, nr_tfidf_cl)
	
	# Get feature sets
	X_train = [feature_set(train_set.data[i][0], tfidf_cl, train_set, train_set, stv) for i in range(len(train_set.data))]
	
	# Build decision tree
	gnb = GaussianNB()
	gnb = gnb.fit(X_train, Y_train)
	
	# Return predictions
	X_test = [feature_set(test_set.data[i][0], tfidf_cl, train_set, test_set, stv) for i in range(len(test_set.data))]
	return gnb.predict(X_test)
