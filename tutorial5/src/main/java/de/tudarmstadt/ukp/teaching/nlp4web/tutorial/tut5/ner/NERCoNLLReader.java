/**
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.teaching.nlp4web.tutorial.tut5.ner;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import type.*;

import org.dkpro.tc.api.io.TCReaderSequence;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.*;



/**
 * Reads the GermEval 2014 NER Shared Task dataset.
 * Can be used for Unit or Sequence Classification.
 *
 */
public class NERCoNLLReader
    extends JCasResourceCollectionReader_ImplBase
	implements TCReaderSequence

{
    private static final int TOKEN = 0;
    private static final int PENNPOS = 1;
    private static final int CHUNK = 2;
    private static final int IOB = 3;

    
    private static final boolean LOCATIONS = true;

    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    /**
     * The language.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
    private String language;
    
    /**
     * Load the chunk tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_CHUNK_MAPPING_LOCATION = ComponentParameters.PARAM_CHUNK_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_CHUNK_MAPPING_LOCATION, mandatory = false)
    protected String chunkMappingLocation;
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        
        Resource res = nextFile();
        initCas(aJCas, res);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(res.getInputStream(), encoding));
            convert(aJCas, reader);
        }
        finally {
            closeQuietly(reader);
        }
    }

    private void convert(JCas aJCas, BufferedReader aReader)
        throws IOException
    {
    	Set<String> locations = null;
    	if (LOCATIONS) {
    		locations = new HashSet<String>(FileUtils.readLines(new File("src/main/resources/locations.txt")));
    	}
    	
        JCasBuilder doc = new JCasBuilder(aJCas);
        
        List<String[]> words;
        while ((words = readSentence(aReader)) != null) {
            if (words.isEmpty()) {
                continue;
            }

            int sentenceBegin = doc.getPosition();
            int sentenceEnd = sentenceBegin;

            List<Token> tokens = new ArrayList<Token>();
            
            for (String[] word : words) {
                Token token = doc.add(word[TOKEN], Token.class);
                sentenceEnd = token.getEnd();
                doc.add(" ");
               
	            annotatePOS(aJCas, word[PENNPOS], token.getBegin(), token.getEnd());
	            annotateChunk(aJCas, word[CHUNK], token.getBegin(), token.getEnd());
	            markBeginning(aJCas, word[CHUNK], token.getBegin(), token.getEnd());
	            
	            
	            if (LOCATIONS) {
	            	if (locations.contains(word[TOKEN].toLowerCase())){
	            		Location l = new Location(aJCas);
	            		l.setBegin(token.getBegin());
	            		l.setEnd(token.getEnd());
	            		l.addToIndexes();
	            	}
	            }
	            
                TextClassificationTarget unit = new TextClassificationTarget(aJCas, token.getBegin(), token.getEnd());
                unit.addToIndexes();
                
                TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas, token.getBegin(), token.getEnd());
                outcome.setOutcome(word[IOB]);
                outcome.addToIndexes();
                tokens.add(token);
            }

            // Sentence
            Sentence sentence = new Sentence(aJCas, sentenceBegin, sentenceEnd);
            sentence.addToIndexes();
            
            TextClassificationSequence sequence = new TextClassificationSequence(aJCas, sentenceBegin, sentenceEnd);
            sequence.addToIndexes();

            // Once sentence per line.
            doc.add("\n");
        }

        doc.close();
    }
    
    /**
     * Collect the Penn Treebank pos features and convert them to 
     * de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos Annotations.
     */
    private void annotatePOS(JCas aJCas, String tag, int begin, int end) {
    	POS postag = null;
    	if (tag.equals("JJ")||tag.equals("JJS")||tag.equals("JJR")) {
    		postag = new ADJ(aJCas);
    	} else if (tag.contains("NN")){
    		postag = new N(aJCas);
    	} else if (tag.contains("RB")||tag.equals("RP")){
    		postag = new ADV(aJCas);
    	} else if (tag.equals("LS")){
    		postag = new PUNC(aJCas);
    	} else if (tag.contains("PRP")||tag.contains("WP")||tag.equals("POS")){
    		postag = new PR(aJCas);
    	} else if (tag.equals("DT")||tag.equals("WDT")){
    		postag = new ART(aJCas);
    	}else if (tag.equals("MD")||tag.contains("VB")){
    		postag = new V(aJCas);
    	} else if (tag.equals("CC")||tag.equals("IN")){
    		postag = new CONJ(aJCas);
    	} else if (Pattern.matches("\\p{Punct}", tag)||tag.equals("SYM")){
    		postag = new PUNC(aJCas);
    	} else if (tag.equals("CD")||tag.equals("PDT")){
    		postag = new CARD(aJCas);
    	} else {
    		postag = new O(aJCas);
    	}
    	postag.setBegin(begin);
    	postag.setEnd(end);
    	postag.addToIndexes();
    }
    
    /**
     * Collect the chunking features and annotate the tokens.
     */
    private void annotateChunk(JCas aJCas, String tag, int begin, int end) {
    	Chunk c = null;
    	if (tag.contains("CONJP")) {
    		c = new CONJP(aJCas);
    	} else if (tag.contains("ADJP")) {
    		c = new ADJP(aJCas);
    	} else if (tag.contains("ADVP")) {
    		c = new ADVP(aJCas);
    	} else if (tag.contains("INTJ")) {
    		c = new INTJ(aJCas);
    	} else if (tag.contains("LST")) {
    		c = new LST(aJCas);
    	} else if (tag.contains("NP")) {
    		c = new C_NP(aJCas);
    	} else if (tag.contains("VP")) {
    		c = new VP(aJCas);
    	} else if (tag.contains("PP")) {
    		c = new C_PP(aJCas);
    	} else if (tag.contains("PRT")) {
    		c = new C_PRT(aJCas);
    	} else if (tag.contains("SBAR")) {
    		c = new SBAR(aJCas);
    	} else {
    		c = new C_O(aJCas);
    	}
    	c.setBegin(begin);
    	c.setEnd(end);
    	c.addToIndexes();
    }
    
    /**
     * Annotate whether a chunking tag had a B- letter to signify it is the 
     * beginning of a chunk, when following a chunk of the same type.
     */
    private void markBeginning(JCas aJCas, String tag, int begin, int end) {
    	if (tag.contains("B-")){
    		Beginning b = new Beginning(aJCas);
    		b.setBegin(begin);
        	b.setEnd(end);
        	b.addToIndexes();
    	}
    }
    

    /**
     * Read a single sentence.
     */
    private static List<String[]> readSentence(BufferedReader aReader)
        throws IOException
    {
    	//System.out.println("START SENTENCE");
        List<String[]> words = new ArrayList<String[]>();
        String line;
        while ((line = aReader.readLine()) != null) {
            if (StringUtils.isBlank(line)) {
                break; // End of sentence
            }
            if (line.startsWith("#")) {
            	continue;
            }
            
            String[] fields = line.split(" ");
            //System.out.println(fields[0]);
            if (fields.length != 4) {
                throw new IOException(
                        "Invalid file format. Line needs to have 4 space-separted fields.");
            }
            words.add(fields);
        }
        //System.out.println("END SENTENCE");
        if (line == null && words.isEmpty()) {
            return null;
        }
        else {
            return words;
        }
    }

	//@Override
	public String getTextClassificationOutcome(JCas jcas,
			TextClassificationTarget unit) throws CollectionException
	{
		// without function here, as we do not represent this in the CAS
		return null;
	}
}
