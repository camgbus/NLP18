from obj_dataset import DataSet
#import freq_weights as fw
import evaluation as ev
import classifiers as cl
from senttovec import SentToVec
import sys

def get_train_files_cv(fold_index, books_eval_set, books_dev_set):
	'''
	Returns a list with the files names that make up the train set (reviews 
	of all but one books and book from the development set) and	one with 
	those that make up the test set (reviews form the left-out book).
	'''
	train_files = []
	test_files = []
	for i in range(len(books_eval_set)):
		if (i != fold_index):
			for j in range(10):
				train_files.append("data/"+books_eval_set[i]+"_"+str(j)+".csv")
	for dev_b in books_dev_set:
		for j in range(10):
			train_files.append("data/"+dev_b+"_"+str(j)+".csv")
	for j in range(10):
		test_files.append("data/"+books_eval_set[fold_index]+"_"+str(j)+".csv")
	return train_files,test_files


def run_experiment(Sent2vec_PATH, model_PATH, fold, eval_set, dev_set, categories, classifier = 0, nr_tfidf_vec = 0, nr_tfidf_cl = 50, print_results = False):
	'''
	param Sent2vec_PATH: str, path to sent2vec library
	param model_PATH: str, path to sent2vec model
	param fold: int, fold index
	param eval_set: [str], list of book names in evaluation set, as used in the data files
	param dev_set: [str], list of book names in development set, as used in the data files
	param categories: [str], list of class label names
	param classifier: int, 
		0: highest vector similarity
		1: CART
		2: CART with vector similarities as features
		3: Naive Bayes
		4: Naive Bayes with vector similarities as features
	param nr_tfidf_vec: int, nr of tokens with highest tfidf that are used to form the class vector 
		representations. If this number is = 0, the concatination of all class sentences is used.
	param nr_tfidf_cl: int, nr of tokens with highest tfidf that are used for the other classifiers
	param print_results: boolean, print result tables?
	'''
	
	exp_type = str(classifier)+"_V"+str(nr_tfidf_vec)+"_C"+str(nr_tfidf_cl)
	experiment_name = exp_type+"_F"+str(fold)

	# Get training and testing sets
	train_files, test_files = get_train_files_cv(fold, eval_set, dev_set)

	train = DataSet(categories, train_files)
	test = DataSet(categories, test_files)

	# Set paths and save vector representations
	exp_name="fold"+str(fold)+"V"+str(nr_tfidf_vec) # The vector class representations depend only on the fold and nr_tfidf_vec
	
	stv = SentToVec(Sent2vec_PATH, model_PATH, nr_tfidf_vec, exp_name)
	stv.save_vector_rep(train)
	stv.save_vector_rep(test)
	
	

	

'''	# Get predictions from classifier
	if classifier == 0:
		predictions = stv.get_predictions(train, test)
	elif classifier == 1:
		predictions = cl.get_predictions_CART(train, test, nr_tfidf_cl)
	elif classifier == 2:
		predictions = cl.get_predictions_CART(train, test, nr_tfidf_cl, stv)
	elif classifier == 1:
		predictions = cl.get_predictions_NB(train, test, nr_tfidf_cl)
	else:
		predictions = cl.get_predictions_NB(train, test, nr_tfidf_cl, stv)
		
	# Calculate results and print tables
	if (print_results):
		micro_f1, macro_f1, d = ev.get_results(test.data, predictions, categories)
		ev.print_results(micro_f1, macro_f1, d, categories, experiment_name)
'''

Sent2vec_PATH = "/home/cam/Documents/sent2vec-master/"
model_PATH = Sent2vec_PATH + "wiki_unigrams.bin"

fold_index = 0

categories = ["Rev", "Sum", "Sto", "WS", "Char", "O"]

books_eval_set = ['HistoryOfWolves', 'ExitWest', 'SolarBones', 'Reservoir13', 'Elmet', 'TheMinistryOfUtmostHappiness', 'LincolnInTheBardo', 'HomeFire', 'SwingTime', 'TheUndergroundRailroad']
books_dev_set = ['4321', 'DaysWithoutEnd']

run_experiment(Sent2vec_PATH, model_PATH, fold_index, books_eval_set, books_dev_set, 
	categories, classifier = 0, nr_tfidf_vec = 100, nr_tfidf_cl = 50, print_results = False)

