import os
import time
import sys
from subprocess import call
import numpy as np
from scipy import spatial
import pickle
from itertools import chain
import freq_weights as fw
import warnings

class SentToVec:
	def __init__(self, Sent2vec_PATH, model_PATH, nr_tfidf_vec, exp_name):
		self.Sent2vec_PATH = Sent2vec_PATH
		self.model_PATH = model_PATH
		self.nr_tfidf_vec = nr_tfidf_vec
		self.exp_name = exp_name
		
	def getSentenceVector(self, Tokens):
		"""
		param class tokens: list of most representative tokens for a category[str1, str2, ...]
		result: vector representation
		Test : 
		Sent2vec_PATH = "/Volumes/TOSHIBA_EXT/sent2vec-master/"
		model_PATH = Sent2vec_PATH + "wiki_unigrams.bin"
		Tokens = ["Auster", "delves", "deeply", "into", "this", "notion", "while", "leaving"]
		getSentenceVector(Tokens, Sent2vec_PATH, model_PATH)
		"""
		fasttext_exec_path = os.path.abspath(self.Sent2vec_PATH + "./fasttext")
		model = os.path.abspath(self.model_PATH)
		timestamp = str(time.time())
		Sentence =' '.join(str(s) for s in Tokens)
		Sentence_file = open(self.Sent2vec_PATH +'./'+timestamp+'_sentence.txt',"w")
		Sentence_file.write(Sentence)
		Sentence_file.close();
		Sentence_path = os.path.abspath(self.Sent2vec_PATH +'./'+timestamp+'_sentence.txt')
		Vector_path = os.path.abspath(self.Sent2vec_PATH +'./'+timestamp+'_vector.txt')
		call(fasttext_exec_path +' print-sentence-vectors '+
		self.model_PATH +
		' < ' + Sentence_path + 
		' > ' + Vector_path, shell=True)
		vector_reader = open(Vector_path,"r")
		line = vector_reader.readline();
		os.remove(Sentence_path)
		os.remove(Vector_path)
		SentenceVector = np.fromstring(line,sep=' ')
		return SentenceVector

	def SentenceVectorDistane(self, SentenceVector1, SentenceVector2):
		"""
		param SentenceVector1: vector rep
		param SentenceVector2: vector rep
		result: cosine similarty between vectors
		Test : 
		Sent2vec_PATH = "/Volumes/TOSHIBA_EXT/sent2vec-master/"
		model_PATH = Sent2vec_PATH + "wiki_unigrams.bin"
		Tokens1 = ["Auster", "delves", "deeply", "into", "this", "notion", "while", "leaving"]
		Tokens2 = ["At", "least", "that", "what", "happened", "in", "my", "case"]
		vec1 = getSentenceVector(Tokens1, Sent2vec_PATH, model_PATH)
		vec2 = getSentenceVector(Tokens2, Sent2vec_PATH, model_PATH)
		SentenceVectorDistane(vec1, vec2)
		"""
		# Class representation is vector of 0s because there were no instances
		# In this case, a similitude of 0 is returned
		if all(v == 0 for v in SentenceVector2):
			return 0
		else:
			return 1 - spatial.distance.cosine(SentenceVector1, SentenceVector2)

	def save_vector_rep(self, data_set):
		"""
		Save vector representations of a dataset to save time
		"""
		for s in data_set.ids:
			sent_id = data_set.ids[s]
			full_name = os.path.abspath('sent2vec')+"/"+sent_id+".p"
			if not os.path.isfile(full_name):
				print("Finding representation for "+sent_id)
				v = self.getSentenceVector(list(s))
				pickle.dump(v, open(full_name, "wb" ))
	

	def get_vector_rep(self, tok_sent, data_set):
		"""
		Restore vector representation
		param tok_sent: tokenized sentence
		param data_set: instance of obj_dataset that sentence belongs to
		"""
		sent_id = data_set.ids[tuple(tok_sent)]
		full_name = os.path.abspath('sent2vec')+"/"+sent_id+".p"
		return pickle.load( open(full_name, "rb" ) )

	def get_category_reps(self, data_set):
		"""
		Finds a vector representation for each category
		
		param data_set: the training set
		param nr_tfidf_vec: number of most common tokens for that category that will make up the 
			category representation. If the number is = 0, the text will be a which is a compilation 
			of all sentences of that category.
		param exp_name: optional string for the experiment name (one name would be given
		to each cross-validation fold). If present, the representations are stored and can be
		restored if already calculated.
		"""
		vec_reps = dict()
		if self.nr_tfidf_vec > 0:
			tfidf_cl = fw.representative(data_set.data, data_set.classDict, self.nr_tfidf_vec)
		
		for c in data_set.classDict:
			if self.nr_tfidf_vec > 0:
				text = tfidf_cl[c]
			else:
				text = list(chain.from_iterable(data_set.data[s][0] for s in data_set.classDict[c]))
			if self.exp_name:
				full_name = os.path.abspath('sent2vec')+"/exp_"+self.exp_name+"_"+c+".p"
				if os.path.isfile(full_name):
					v = pickle.load(open(full_name, "rb" ))
				else:
					print("Finding representation for "+c+" length: "+str(len(text)))
					v = self.getSentenceVector(text)
					pickle.dump(v, open(full_name, "wb" ))
			else:
				print("Finding representation for "+c)
				v = self.getSentenceVector(text)
			vec_reps[c] = v
		return vec_reps

	def get_predictions(self, train_set, test_set):
		"""
		Classifies sentences by assigning them the category which is closest in terms of the 
		cosine similarity of the vector representations.
		"""
		vec_reps = self.get_category_reps(train_set)
		predictions = list()
		for (s,t) in test_set.data:
			v = self.get_vector_rep(s, test_set)
			# Category with highest cosine similarity is selected
			c = max(vec_reps.keys(), key=lambda k: self.SentenceVectorDistane(vec_reps[k],v))
			predictions.append(c)
		return predictions

	def get_similarity_scores(self, train_set, data_set, s):
		"""
		Returns a similarity score of that sentence to each class.
		"""
		vec_reps = self.get_category_reps(train_set)
		v = self.get_vector_rep(s, data_set)
		scores = dict()
		for c in vec_reps:
			scores[c] = self.SentenceVectorDistane(vec_reps[c],v)
		return scores

