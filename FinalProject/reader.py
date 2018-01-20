import csv
import nltk


categories_old = ["Author", "Reviewer", "Summary", "Story", "Writing Style", "Characterization", "Other"]

def read_file(filename, categories):
	'''
	Reads a file, returns a list of observations, where an observation is
	a string list of tokens followed by the class.
	param str filename: the full pathname of the file to open
	returns [str[], str]: [tokens, class] 
	'''
	f = open(filename, 'r')
	reader = csv.reader(f, delimiter='\t')
	line = 0
	observations = list()
	for row in reader:
		if (line > 0):
			# Remove non-ASCII characters
			sentence = ''.join([i if ord(i) < 128 else ' ' for i in row[0]])
			tokens = nltk.word_tokenize(sentence.lower())
			s_class = row[1]
			if s_class not in categories:
				# To avoid having to change the development files
				if s_class in categories_old:
					s_class = categories[categories_old.index(s_class)]
				else:
					raise NameError('Category does not exit')
			observations.append((tokens, s_class))
		line += 1			
	return observations		
	
