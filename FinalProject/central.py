from obj_dataset import DataSet
import evaluation as ev
import classifiers as cl
from senttovec import SentToVec
import sys

def run_experiment(Sent2vec_PATH, model_PATH, fold, eval_set, dev_set, categories, classifier = 0, nr_tfidf_vec = 0, nr_tfidf_cl = 50, export_tree = False, print_results = False, cv_k = 0):
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
	param nr_tfidf_vec: int, nr of tokens with highest tfidf that are used to form the class vector 
		representations. If this number is = 0, the concatination of all class sentences is used.
	param nr_tfidf_cl: int, nr of tokens with highest tfidf that are used for the other classifiers
	param export_tree: boolean, print tree model in 'models' folder?
	param print_results: boolean, print result tables?
	'''
	
	exp_type = str(classifier)+"_V"+str(nr_tfidf_vec)+"_C"+str(nr_tfidf_cl)
	experiment_name = exp_type+"_F"+str(fold)

	# Get training and testing sets
	train_files, test_files = get_train_files_cv(fold, eval_set, dev_set, cv_k)
	train = DataSet(categories, train_files)
	test = DataSet(categories, test_files)

	# Set paths and save vector representations
	# The vector class representations depend only on the fold and nr_tfidf_vec
	exp_name="fold"+str(fold)+"V"+str(nr_tfidf_vec) 
	
	stv = SentToVec(Sent2vec_PATH, model_PATH, nr_tfidf_vec, exp_name)
	stv.save_vector_rep(train)
	stv.save_vector_rep(test)
	
	# Get predictions from classifier
	if classifier == 0:
		predictions = stv.get_predictions(train, test)
	elif classifier == 1:
		model, predictions = cl.get_predictions_CART(train, test, nr_tfidf_cl)
	else:
		model, predictions = cl.get_predictions_CART(train, test, nr_tfidf_cl, stv)
	
	# Print visual representation of decision tree in 'models' folder
	if (classifier > 0 and export_tree):
		cl.print_tree(model, experiment_name, categories, classifier)
	
	# Calculate results and print tables
	if (print_results):
		micro_f1, macro_f1, d = ev.get_results(test.data, predictions, categories)
		ev.print_results(micro_f1, macro_f1, d, categories, experiment_name)

def get_train_files_cv(fold_index, books_eval_set, books_dev_set, cv_k):
	'''
	Returns a list with the files names that make up the train set (reviews 
	of all but one books and book from the development set) and	one with 
	those that make up the test set (reviews form the left-out book).
	'''
	train_files = []
	test_files = []
	for i in range(len(books_eval_set)):
		if (i != fold_index):
			for j in range(cv_k):
				train_files.append("data/"+books_eval_set[i]+"_"+str(j)+".csv")
	for dev_b in books_dev_set:
		for j in range(cv_k):
			train_files.append("data/"+dev_b+"_"+str(j)+".csv")
	for j in range(len(books_eval_set)):
		test_files.append("data/"+books_eval_set[fold_index]+"_"+str(j)+".csv")
	return train_files,test_files

def perform_cross_validation(Sent2vec_PATH, model_PATH, eval_set, dev_set, categories, classifier, nr_tfidf_vec, nr_tfidf_cl, k = 10):
	'''
	This method perfroms a cross validation and prints the result tables per fold, as well as
	the averaged one. Note that green 0 cells means that the precision, recall or F1 scores are
	0 because there were no true positive examples,
	so 
	'''
	exp_type = str(classifier)+"_V"+str(nr_tfidf_vec)+"_C"+str(nr_tfidf_cl)
	file_list = []
	for fold_index in range(k):
		run_experiment(Sent2vec_PATH, model_PATH, fold_index, eval_set, dev_set, 
		categories, classifier, nr_tfidf_vec, nr_tfidf_cl, export_tree = False, print_results = True, cv_k = k)
		file_list.append(exp_type+"_F"+str(fold_index))
	ev.avg_result_tables(file_list, categories, exp_type)


Sent2vec_PATH = "/home/cam/Documents/sent2vec-master/"
model_PATH = Sent2vec_PATH + "wiki_unigrams.bin"

categories = ["Rev", "Sum", "Sto", "WS", "Char", "O"]

books_eval_set = ['HistoryOfWolves', 'ExitWest', 'SolarBones', 'Reservoir13', 'Elmet', 'TheMinistryOfUtmostHappiness', 'LincolnInTheBardo', 'HomeFire', 'SwingTime', 'TheUndergroundRailroad']
books_dev_set = ['4321', 'DaysWithoutEnd']

classifier = 2
nr_tfidf_vec = 0
nr_tfidf_cl = 100

perform_cross_validation(Sent2vec_PATH, model_PATH, books_eval_set, books_dev_set, categories, classifier, nr_tfidf_vec, nr_tfidf_cl)


